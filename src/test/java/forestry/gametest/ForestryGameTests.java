package forestry.gametest;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.gametest.GameTestHooks;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforgespi.language.ModFileScanData;

import org.objectweb.asm.Type;

import forestry.api.ForestryConstants;

/**
 * Registers every {@link ForestryGameTest} method. Only active when NeoForge enables GameTests (gametest server, IDE,
 * {@code -Dneoforge.enableGameTest=true}), so the dev jar stays inert in normal play. Also registers
 * {@link ChunkLifecycle}'s chunk ticket type (26.x ticket types are registry entries).
 */
@Mod(ForestryGameTests.MOD_ID)
public class ForestryGameTests {
	public static final String MOD_ID = "forestrygametest";
	private static final Type ANNOTATION = Type.getType(ForestryGameTest.class);
	private static final Identifier EMPTY_STRUCTURE = Identifier.withDefaultNamespace("empty");

	private record TestMethod(Identifier id, Method method, int timeoutTicks) {}

	private final List<TestMethod> tests = new ArrayList<>();

	public ForestryGameTests(IEventBus modBus) {
		if (!GameTestHooks.isGametestEnabled()) {
			return;
		}
		this.tests.addAll(findTests());
		modBus.addListener(this::registerFunctions);
		modBus.addListener(this::registerTests);
	}

	private void registerFunctions(RegisterEvent event) {
		event.register(Registries.TICKET_TYPE, helper -> helper.register(ForestryConstants.forestry("gametest"), ChunkLifecycle.TICKET));
		event.register(Registries.TEST_FUNCTION, helper -> {
			for (TestMethod test : this.tests) {
				helper.register(test.id(), invoker(test.method()));
			}
		});
	}

	private void registerTests(RegisterGameTestsEvent event) {
		Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
			ForestryConstants.forestry("default"), new TestEnvironmentDefinition.AllOf(List.of()));
		for (TestMethod test : this.tests) {
			ResourceKey<Consumer<GameTestHelper>> function = ResourceKey.create(Registries.TEST_FUNCTION, test.id());
			event.registerTest(test.id(), new FunctionGameTestInstance(function,
				new TestData<>(environment, EMPTY_STRUCTURE, test.timeoutTicks(), 0, true)));
		}
	}

	private static Consumer<GameTestHelper> invoker(Method method) {
		return helper -> {
			try {
				method.invoke(null, helper);
			} catch (java.lang.reflect.InvocationTargetException e) {
				// Assertion failures must reach the GameTest runner unwrapped.
				if (e.getCause() instanceof RuntimeException runtime) {
					throw runtime;
				}
				if (e.getCause() instanceof Error error) {
					throw error;
				}
				throw new RuntimeException(e.getCause());
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
		};
	}

	private static List<TestMethod> findTests() {
		List<TestMethod> found = new ArrayList<>();
		ModFileScanData scanData = ModList.get().getModFileById(MOD_ID).getFile().getScanResult();
		for (ModFileScanData.AnnotationData data : scanData.getAnnotations()) {
			if (!data.annotationType().equals(ANNOTATION)) {
				continue;
			}
			String className = data.clazz().getClassName();
			String methodName = data.memberName().substring(0, data.memberName().indexOf('('));
			try {
				Method method = Class.forName(className).getDeclaredMethod(methodName, GameTestHelper.class);
				if (!Modifier.isStatic(method.getModifiers())) {
					throw new IllegalStateException("@ForestryGameTest method must be static: " + method);
				}
				method.setAccessible(true);
				String simpleName = className.substring(className.lastIndexOf('.') + 1);
				Identifier id = ForestryConstants.forestry((simpleName + "." + methodName).toLowerCase(Locale.ROOT));
				int timeout = method.getAnnotation(ForestryGameTest.class).timeoutTicks();
				found.add(new TestMethod(id, method, timeout));
			} catch (ReflectiveOperationException e) {
				throw new IllegalStateException("Failed to load GameTest " + className + "#" + methodName, e);
			}
		}
		found.sort(Comparator.comparing(test -> test.id().toString()));
		return found;
	}
}
