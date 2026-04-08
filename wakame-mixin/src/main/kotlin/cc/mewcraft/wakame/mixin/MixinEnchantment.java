package cc.mewcraft.wakame.mixin;

import cc.mewcraft.wakame.bridge.KoishItemBridge;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
abstract class MixinEnchantment {

    @Inject(method = "isPrimaryItem", at = @At("HEAD"), cancellable = true)
    private void isPrimaryItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!KoishItemBridge.Impl.isKoish(stack))
            return;
        var ench = (Enchantment) (Object) this;
        cir.setReturnValue(KoishItemBridge.Impl.isPrimaryEnchantment(stack, ench));
    }

    @Inject(method = "isSupportedItem", at = @At("HEAD"), cancellable = true)
    private void isSupportedItem(ItemStack item, CallbackInfoReturnable<Boolean> cir) {
        if (!KoishItemBridge.Impl.isKoish(item))
            return;
        var ench = (Enchantment) (Object) this;
        cir.setReturnValue(KoishItemBridge.Impl.isSupportedEnchantment(item, ench));
    }

    @Inject(method = "canEnchant", at = @At("HEAD"), cancellable = true)
    private void canEnchant(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!KoishItemBridge.Impl.isKoish(stack))
            return;
        var ench = (Enchantment) (Object) this;
        cir.setReturnValue(KoishItemBridge.Impl.isSupportedEnchantment(stack, ench));
    }
}
