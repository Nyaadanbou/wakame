package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.mixin.support.WatchedArmorList;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.eclipse.sisu.space.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = Inventory.class)
public abstract class MixinPlayerInventory implements Container, Nameable {
    @Shadow @Final @Mutable
    public NonNullList<ItemStack> armor;

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.PUTFIELD,
                    target = "Lnet/minecraft/world/entity/player/Inventory;armor:Lnet/minecraft/core/NonNullList;"
            )
    )
    private void redirect(Inventory instance, NonNullList<ItemStack> originalArmor, Player player) {
        // 替换为 WatchedArmorList
        this.armor = new WatchedArmorList(player);
    }
}
