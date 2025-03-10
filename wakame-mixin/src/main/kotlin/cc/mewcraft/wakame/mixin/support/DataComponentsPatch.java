package cc.mewcraft.wakame.mixin.support;

import cc.mewcraft.wakame.item2.data.ItemDataContainer;
import cc.mewcraft.wakame.mixin.core.InvokerDataComponents;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;

public class DataComponentsPatch {

    public static final DataComponentType<ItemDataContainer> ITEM_DATA_CONTAINER = InvokerDataComponents.register(
            "koish:item_data_container", builder -> builder.persistent(
                    Codec.lazyInitialized(ItemDataContainer::makeCodec) // Koish 的 class 似乎(?)会晚于 DataComponents 加载, 所以使用 Codec.lazyInitialized
            )
    );

    public static void bootstrap() {
        // 用于初始化静态变量
    }

    public static boolean isCustomType(DataComponentType<?> type) {
        return type == ITEM_DATA_CONTAINER;
    }

}
