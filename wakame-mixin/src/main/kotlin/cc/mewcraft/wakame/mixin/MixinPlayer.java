package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.item.KoishStackData;
import cc.mewcraft.wakame.item.property.ItemPropTypes;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
abstract class MixinPlayer {

    /// @author Flandreqwq
    /// @reason 实现"物品是否能够发动横扫攻击"由 Koish 控制.
    @Redirect(
            method = "isSweepAttack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/tags/TagKey;)Z"
            )
    )
    private boolean redirectIsSword(ItemStack instance, TagKey<Item> tag) {
        return KoishStackData.hasProp(instance, ItemPropTypes.MINECRAFT_SWEEP);
    }
}
