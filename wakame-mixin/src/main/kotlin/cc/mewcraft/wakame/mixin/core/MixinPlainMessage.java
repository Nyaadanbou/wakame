package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.item.display.ShowItemRenderer;
import io.papermc.paper.adventure.PaperAdventure;
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
            ordinal = 0,
            argsOnly = true)
    private static Component redirected(Component component) {
        net.kyori.adventure.text.Component adventureComponent = PaperAdventure.asAdventure(component);
        net.kyori.adventure.text.Component changedAdventureComponent = ShowItemRenderer.Impl.render(adventureComponent);
        return PaperAdventure.asVanilla(changedAdventureComponent);
    }
}
