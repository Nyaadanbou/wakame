package cc.mewcraft.wakame.mixin.support;

import cc.mewcraft.wakame.item2.data.ItemDataContainer;
import cc.mewcraft.wakame.mixin.core.InvokerDataComponents;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;

import java.util.function.UnaryOperator;

// FIXME #365: 改名 ExtraDataComponents
public class DataComponentsPatch {

    /**
     * 用于储存 Koish 添加的自定义数据.
     * 该数据仅作为内部实现, 外部不应使用.
     */
    public static final DataComponentType<ItemDataContainer> DATA_CONTAINER = register(
            "koish:data_container", builder -> builder.persistent(
                    Codec.lazyInitialized(ItemDataContainer::makeCodec) // Koish 的 class 似乎(?)会晚于 DataComponents 加载, 所以使用 Codec.lazyInitialized
            )
    );

    public static void bootstrap() {
        // 用于初始化静态变量
    }

    public static boolean isCustomType(DataComponentType<?> type) {
        return type == DATA_CONTAINER;
    }

    private static <T> DataComponentType<T> register(String id, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return InvokerDataComponents.register(id, builderOperator);
    }

}
