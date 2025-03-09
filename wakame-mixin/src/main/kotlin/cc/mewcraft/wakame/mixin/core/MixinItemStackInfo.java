package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.mixin.support.DataComponentsPatch;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HoverEvent.ItemStackInfo.class)
public abstract class MixinItemStackInfo {

    @Final
    @Shadow
    public Holder<Item> item;

    @Final
    @Shadow
    public int count;

    @Final
    @Shadow
    public DataComponentPatch components;

    @ModifyExpressionValue(
            method = "getItemStack",
            at = @At(
                    value = "NEW",
                    target = "Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private ItemStack modifyNewItemStack(ItemStack original) {
        return new ItemStack(item, count, components.forget(DataComponentsPatch::isCustomType));
    }
}
