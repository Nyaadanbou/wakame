package cc.mewcraft.wakame.mixin.support;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;

import java.util.function.Predicate;

public interface DataComponentPatchExtras {

    /**
     * 直接移除 {@link DataComponentPatch} 中符合条件的 {@link DataComponentType}.
     * <p>
     * 与 {@link DataComponentPatch#forget(Predicate)} 不同, 该方法会直接修改当前实例, 而非创建新的实例.
     * 这在一些性能敏感的场景下会大幅减少 GC 的次数, 例如在发送物品堆叠封包时.
     *
     * @return 修改后的 {@link DataComponentPatch} 实例 (原实例, 非新实例)
     */
    default DataComponentPatch koish$intrusiveRemove(Predicate<DataComponentType<?>> predicate) {
        throw new UnsupportedOperationException();
    }
}
