package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.mixin.support.KoishDataSanitizer;
import net.minecraft.server.dialog.body.ItemBody;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(ItemBody.class)
public abstract class MixinItemBody {

    @Inject(
            method = "<init>",
            at = @At(
                    value = "TAIL"
            )
    )
    private void onInit(ItemStack item, Optional description, boolean showDecorations, boolean showTooltip, int width, int height, CallbackInfo ci) {
        KoishDataSanitizer.sanitizeItemStack(item);
    }
}
