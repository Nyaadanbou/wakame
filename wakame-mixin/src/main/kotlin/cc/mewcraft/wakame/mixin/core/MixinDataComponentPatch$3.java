package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.mixin.support.ExtraDataComponents;
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
        var optional = patch.get(ExtraDataComponents.DATA_CONTAINER);
        //noinspection OptionalAssignedToNull
        if (optional != null) { // 这里不为 null 的语义是该组件可能为 add 也可能为 remove - 只有这种情况需要 forget
            return patch.forget(t -> t == ExtraDataComponents.DATA_CONTAINER);
        } else {
            return patch;
        }
    }
}
