package cc.mewcraft.wakame.bridge;

import cc.mewcraft.wakame.bridge.item.ServerItemDataContainer;
import cc.mewcraft.wakame.bridge.item.ServerItemKey;
import cc.mewcraft.wakame.mixin.InvokerDataComponents;
import net.minecraft.core.component.DataComponentType;

import java.util.function.UnaryOperator;

public class ExtraDataComponents {

    /// 用于储存 Koish 物品的唯一标识符.
    /// 该数据仅作为内部实现, 外部不应使用.
    public static final DataComponentType<ServerItemKey> ITEM_KEY = register(
            "koish:item_key", builder -> builder.persistent(ServerItemKey.getCodec())
    );

    /// 用于储存 Koish 添加的自定义数据.
    /// 该数据仅作为内部实现, 外部不应使用.
    public static final DataComponentType<ServerItemDataContainer> DATA_CONTAINER = register(
            "koish:data_container", builder -> builder.persistent(ServerItemDataContainer.getCodec())
    );

    public static void bootstrap() {
        // 用于初始化静态变量
    }

    private static <T> DataComponentType<T> register(String id, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return InvokerDataComponents.register(id, builderOperator);
    }
}
