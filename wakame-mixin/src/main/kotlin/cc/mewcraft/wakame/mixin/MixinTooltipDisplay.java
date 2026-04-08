package cc.mewcraft.wakame.mixin;

import cc.mewcraft.wakame.bridge.KoishDataSanitizer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.component.TooltipDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.SequencedSet;

@Mixin(TooltipDisplay.class)
public abstract class MixinTooltipDisplay {
    @Inject(
            method = "<init>",
            at = @At(
                    value = "TAIL"
            )
    )
    private void onInit(boolean hideTooltip, SequencedSet<DataComponentType<?>> hiddenComponents, CallbackInfo ci) {
        hiddenComponents.removeIf(KoishDataSanitizer::isExtra);
    }
}
