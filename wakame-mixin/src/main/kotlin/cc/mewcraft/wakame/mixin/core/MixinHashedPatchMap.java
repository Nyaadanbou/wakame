package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.mixin.support.ExtraDataComponents;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.HashedPatchMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Set;

@Mixin(HashedPatchMap.class)
public class MixinHashedPatchMap {

    /**
     * 让 {@link HashedPatchMap} 不考虑 `minecraft:lore` 物品组件.
     *
     * @param addedComponents   客户端侧添加的物品组件
     * @param removedComponents 客户端侧移除的物品组件
     */
    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void injected0(
            Map<DataComponentType<?>, Integer> addedComponents,
            Set<DataComponentType<?>> removedComponents,
            CallbackInfo ci
    ) {
        addedComponents.remove(DataComponents.LORE);
        removedComponents.remove(DataComponents.LORE);
    }

    /**
     * 使服务端在比较物品哈希时, 忽略 `minecraft:lore` 和 `koish:data_container` 物品组件.
     *
     * @param patch 服务端侧的 {@link DataComponentPatch}
     */
    // 这里的 matches 原本逻辑:
    //   计算服务端侧数据(DataComponentPatch)的哈希, 与客户端发过来的哈希进行比较.
    //   如果哈希不一致, 那么 matches 函数则返回 false, 之后服务端会重新发送 Set Container Slot 以纠正客户端上的“错误数据”.
    //   如果哈希一致, 则不进行纠正.
    // 解决办法:
    //   在 matches 函数一开始就先移除服务端侧 patch 中的 `minecraft:lore` 和 `koish:data_container` 物品组件,
    //   然后在已经忽略客户端侧 `minecraft:lore` 组件哈希的前提下, 比较服务端侧和客户端侧的物品哈希.
    // 潜在问题:
    //   未知
    @ModifyVariable(
            method = "matches",
            at = @At("HEAD"),
            argsOnly = true
    )
    private DataComponentPatch injected1(
            DataComponentPatch patch
    ) {
        return patch.forget(t -> t == DataComponents.LORE || t == ExtraDataComponents.DATA_CONTAINER);
    }
}
