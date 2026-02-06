package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.mixin.support.PreWorldStageTasks;
import org.bukkit.Server;
import org.bukkit.craftbukkit.CraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftServer.class)
public abstract class MixinCraftServer implements Server {

    @Inject(
            method = "loadPlugins()V",
            at = @At("HEAD")
    )
    private void inject(CallbackInfo ci) {
        PreWorldStageTasks.INSTANCE.run();
    }
}
