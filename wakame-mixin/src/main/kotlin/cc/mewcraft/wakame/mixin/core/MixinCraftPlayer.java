package cc.mewcraft.wakame.mixin.core;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftPlayer.class)
public abstract class MixinCraftPlayer {

    @Inject(
            method = "setHealthScale",
            at = @At("HEAD")
    )
    private void injected1(double scale, CallbackInfo ci) {
        koish$printStackTrace("setHealthScale");
    }

    @Inject(
            method = "setHealthScaled",
            at = @At("HEAD")
    )
    private void injected2(boolean scale, CallbackInfo ci) {
        koish$printStackTrace("setHealthScaled");
    }

    /**
     * 打印完整的堆栈信息, 用于找出调用该方法的插件.
     */
    @Unique
    private void koish$printStackTrace(String methodName) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StringBuilder sb = new StringBuilder();
        sb.append("[Koish Trace] ").append(methodName).append(" called from:\n");

        for (StackTraceElement element : stackTrace) {
            sb.append("  at ").append(element.getClassName())
              .append(".").append(element.getMethodName())
              .append("(").append(element.getFileName())
              .append(":").append(element.getLineNumber()).append(")\n");
        }

        System.out.println(sb);
    }
}
