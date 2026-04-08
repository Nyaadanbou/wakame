package cc.mewcraft.wakame.mixin;

import cc.mewcraft.wakame.bridge.KoishItemBridge;
import net.minecraft.network.chat.Component;
import net.minecraft.server.dialog.body.PlainMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlainMessage.class)
public class MixinPlainMessage {

    @ModifyVariable(
            method = "<init>",
            at = @At("HEAD"),
            name = "contents",
            argsOnly = true
    )
    private static Component redirected(Component contents) {
        return KoishItemBridge.Impl.createShowItemComponent(contents.copy());
    }
}
