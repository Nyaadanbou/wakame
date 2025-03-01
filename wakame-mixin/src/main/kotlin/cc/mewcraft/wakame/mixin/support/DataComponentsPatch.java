package cc.mewcraft.wakame.mixin.support;

import cc.mewcraft.wakame.mixin.core.InvokerDataComponents;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;

public class DataComponentsPatch {
    public static final DataComponentType<Boolean> IS_G22 = InvokerDataComponents.register(
            "koish:is_g22", builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
    );

    public static void bootstrap() {
        // 用于初始化静态变量
    }

    public static boolean isCustomType(DataComponentType<?> type) {
        ResourceLocation key = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type);
        if (key == null)
            return false;
        return key.getNamespace().equals("koish");
    }
}
