package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.item.HotfixItemName;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/// 为了让 Koish 物品在聊天框可以正常渲染.
@Mixin(ItemStack.class)
public class MixinItemStack {

    @WrapOperation(
            method = "getHoverName",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;getItemName()Lnet/minecraft/network/chat/Component;"
            )
    )
    private Component getHoverName(ItemStack itemStack, Operation<Component> original) {
        Component itemName = HotfixItemName.INSTANCE.getItemName(itemStack);
        if (itemName != null) {
            return itemName;
        } else {
            return original.call(itemStack);
        }
    }
}
