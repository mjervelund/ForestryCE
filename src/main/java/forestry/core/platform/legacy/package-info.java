/**
 * The pre-transfer-API item, fluid and energy handler classes (IItemHandler, IFluidHandler,
 * IEnergyStorage, their templates, wrappers and adapters), copied from NeoForge 21.11.42, where they
 * were deprecated but still shipped next to the new {@code net.neoforged.neoforge.transfer} API.
 * NeoForge 26.x removed them. Forestry's machines, tanks and GUIs are built on them, so the 26.3 port
 * keeps them here and talks to other mods through the adapters ({@code IItemHandler.of},
 * {@code ItemResourceHandlerAdapter}, ...) on the new capabilities.
 * <p>
 * Original copyright Forge Development LLC and contributors, LGPL-2.1-only (see each file's header).
 */
package forestry.core.platform.legacy;
