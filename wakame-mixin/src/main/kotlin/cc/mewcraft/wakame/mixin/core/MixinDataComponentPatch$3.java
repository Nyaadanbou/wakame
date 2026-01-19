package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.mixin.support.KoishDataSanitizer;
import net.minecraft.core.component.DataComponentPatch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(targets = "net/minecraft/core/component/DataComponentPatch$3")
public abstract class MixinDataComponentPatch$3 {

    /**
     * 修改传入的 {@link DataComponentPatch} 参数, 移除 `koish:data_container` 物品组件, 以防止客户端收到未知的物品组件数据而崩溃.
     * <p>
     * 提示: 可以顺着调用 {@link io.papermc.paper.util.sanitizer.ItemComponentSanitizer} 的位置来确定应该需要修改物品封包的地方.
     *
     * @author Ciallo
     * @reason 见方法描述
     */
    @ModifyVariable(
            method = "encode(Lnet/minecraft/network/RegistryFriendlyByteBuf;Lnet/minecraft/core/component/DataComponentPatch;)V",
            at = @At(
                    value = "HEAD"
            ),
            ordinal = 1
    )
    private DataComponentPatch modifyPatch(DataComponentPatch patch) {
        return KoishDataSanitizer.sanitizeDataComponentPatch(patch);
    }
}
