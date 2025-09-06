package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.mixin.support.ExtraDataComponents;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.HashedPatchMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(HashedPatchMap.class)
public class MixinHashedPatchMap {

    /**
     * 使得服务端在计算物品哈希时, 忽略掉 `koish:data_container` 物品组件.
     *
     * @param patch 服务端上的 {@link DataComponentPatch}
     * @return 忽略掉 `koish:data_container` 物品组件后的 {@link DataComponentPatch}
     */
    // 总结:
    // 这里的 matches 原本的逻辑是计算服务端侧数据(DataComponentPatch)的哈希, 与客户端发过来的哈希进行比较.
    // 如果哈希不一致, 那么 matches 函数则返回 false, 之后服务端会重新发送 Set Container Slot 以纠正客户端上的“错误数据”.
    // 解决办法就是在 matches 函数一开始就先把服务端上的 `koish:data_container` 物品组件给移除掉,
    // 这样剩下的服务端侧数据的哈希应该就与客户端发过来的是一致的了, 避免了服务端强行纠正.
    @ModifyVariable(
            method = "matches",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private DataComponentPatch modifyPatch(DataComponentPatch patch) {
        var optional = patch.get(ExtraDataComponents.DATA_CONTAINER);
        //noinspection OptionalAssignedToNull
        if (optional != null) { // 这里 "不为 null" 的语义是存在 add 或 remove 该物品组件的数据
            return patch.forget(ExtraDataComponents::isCustomType);
        } else {
            return patch;
        }
    }
}
