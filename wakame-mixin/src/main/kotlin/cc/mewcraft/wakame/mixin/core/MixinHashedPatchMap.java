package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.mixin.support.ExtraDataComponents;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.HashedPatchMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
     * @param patch         服务端侧的 {@link DataComponentPatch}
     * @param hashGenerator 哈希生成器
     */
    // 这里的 matches 原本逻辑:
    //   计算服务端侧数据(DataComponentPatch)的哈希, 与客户端发过来的哈希进行比较.
    //   如果哈希不一致, 那么 matches 函数则返回 false,
    //   之后服务端会重新发送 Set Container Slot 以纠正客户端上的“错误数据”.
    // 解决办法:
    //   在 matches 函数一开始就先把服务端侧的 patch 是否包含 `koish:data_container` 物品组件,
    //   如果包含 (即为 Koish 物品), 则直接返回 true (即假装哈希一致), 让服务端始终不纠正该数据.
    // 潜在问题:
    //   未知
    @Inject(
            method = "matches",
            at = @At("HEAD")
    )
    private void injected1(
            DataComponentPatch patch,
            HashedPatchMap.HashGenerator hashGenerator,
            CallbackInfoReturnable<Boolean> cir
    ) {
        patch.forget(t -> t == DataComponents.LORE || t == ExtraDataComponents.DATA_CONTAINER);
    }
}
