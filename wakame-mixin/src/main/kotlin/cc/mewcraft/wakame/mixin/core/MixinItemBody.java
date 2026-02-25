package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.item.display.ItemStackRenderer;
import cc.mewcraft.wakame.mixin.support.KoishDataSanitizer;
import net.minecraft.server.dialog.body.ItemBody;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemBody.class)
public abstract class MixinItemBody {

    @ModifyVariable(
            method = "<init>",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true)
    private static ItemStack redirected(ItemStack item) {
        ItemStack copy = item.copy();
        ItemStackRenderer.getInstance().render(copy);
        KoishDataSanitizer.sanitizeItemStack(copy);
        return copy;
    }
}
