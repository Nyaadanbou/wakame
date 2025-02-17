package cc.mewcraft.wakame.mixin.core;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RegistryDataLoader.RegistryData.class)
public interface InvokerRegistryData {

    @Invoker("<init>")
    static <T> RegistryDataLoader.RegistryData<T> create(ResourceKey<? extends Registry<T>> key, Codec<T> codec) {
        throw new AssertionError();
    }

    @Invoker("<init>")
    static <T> RegistryDataLoader.RegistryData<T> create(ResourceKey<? extends Registry<T>> key, Codec<T> codec, boolean requiredNonEmpty) {
        throw new AssertionError();
    }

}
