package forestry.core.content.tools;

import forestry.core.platform.registration.RegistrationIds;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class ForestersManualItem extends Item {
	public ForestersManualItem() {
		super(RegistrationIds.item().stacksTo(1));
	}

	@Override
	public InteractionResult use(Level world, Player player, InteractionHand hand) {

		// 26.3 port: the manual is a Patchouli book and there is no 26.3 Patchouli; the item does nothing
		return InteractionResult.PASS;
	}
}
