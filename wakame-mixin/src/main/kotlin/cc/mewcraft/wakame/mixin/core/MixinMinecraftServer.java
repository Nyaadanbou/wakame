package cc.mewcraft.wakame.mixin.core;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/// 修改 Minecraft 服务器加载世界时的维度名称处理逻辑.
///
/// 对于命名空间为 `koish` 的维度, 会将其前缀 `"<levelId>_koish_"` 移除, 只留下原始的维度名称.
@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer {

    @ModifyVariable(
            method = "loadWorld0(Ljava/lang/String;)V",
            at = @At("STORE"),
            name = "name"
    )
    private static String injected(String name, @Local(name = "levelId") String levelId) {
        if (name.equals(levelId)) {
            return name;
        }

        String koishPrefix = levelId + "_koish_";
        if (name.startsWith(koishPrefix)) {
            return name.substring(koishPrefix.length());
        }

        return name;
    }
}
