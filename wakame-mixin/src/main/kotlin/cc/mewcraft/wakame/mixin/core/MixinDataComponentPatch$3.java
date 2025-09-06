package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.mixin.support.DataComponentPatchExtras;
import cc.mewcraft.wakame.mixin.support.ExtraDataComponents;
import net.minecraft.core.component.DataComponentPatch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(targets = "net/minecraft/core/component/DataComponentPatch$3")
public abstract class MixinDataComponentPatch$3 {

    /**
     * 修改传入的 {@link DataComponentPatch} 参数, 移除由 Koish 添加的物品组件, 以防止客户端收到未知的物品组件数据而崩溃.
     * <p>
     * 个人经验: 我们可以顺着 Paper 的 {@link io.papermc.paper.util.sanitizer.ItemComponentSanitizer}
     * 调用的位置来确定应该在哪些地方修改物品相关封包.
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
        return ((DataComponentPatchExtras) (Object) patch).koish$intrusiveRemove(ExtraDataComponents::isCustomType);
    }
}
