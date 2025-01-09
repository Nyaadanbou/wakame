package cc.mewcraft.wakame.serialization.configurate.typeserializer

import cc.mewcraft.wakame.core.Registry
import cc.mewcraft.wakame.core.RegistryEntry
import cc.mewcraft.wakame.util.MojangRegistry
import cc.mewcraft.wakame.util.MojangResourceLocation
import cc.mewcraft.wakame.util.getValueOrThrow
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.kyori.adventure.key.Key
import org.bukkit.Keyed
import org.spongepowered.configurate.serialize.ScalarSerializer
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.function.Predicate

//<editor-fold desc="Koish Registry">
internal inline fun <reified T : Any> Registry<T>.valueByNameTypeSerializer(): ScalarSerializer<T> {
    return RegistryValueEntrySerializer(this, typeTokenOf())
}

internal inline fun <reified T : Any> Registry<T>.holderByNameTypeSerializer(): ScalarSerializer<RegistryEntry<T>> {
    return RegistryHolderEntrySerializer(this, typeTokenOf())
}

@PublishedApi
internal class RegistryValueEntrySerializer<T : Any>(
    private val registry: Registry<T>, type: TypeToken<T>,
) : ScalarSerializer<T>(type) {
    override fun deserialize(type: Type, obj: Any): T {
        return registry.getOrThrow(obj.toString())
    }

    override fun serialize(item: T, typeSupported: Predicate<Class<*>>): Any {
        return registry.getId(item)?.toString()
            ?: throw SerializationException("No such value '$item' in registry '${registry.key}'")
    }
}

@PublishedApi
internal class RegistryHolderEntrySerializer<T : Any>(
    private val registry: Registry<T>, type: TypeToken<RegistryEntry<T>>,
) : ScalarSerializer<RegistryEntry<T>>(type) {
    override fun deserialize(type: Type, obj: Any): RegistryEntry<T> {
        return registry.createEntry(obj.toString())
    }

    override fun serialize(item: RegistryEntry<T>, typeSupported: Predicate<Class<*>>): Any {
        return item.getKeyOrThrow().value.toString()
    }
}
//</editor-fold>

//<editor-fold desc="Paper Registry">
internal inline fun <reified T : Keyed> RegistryKey<T>.valueByNameTypeSerializer(): ScalarSerializer<T> {
    return BukkitRegistryEntryValueSerializer(this, typeTokenOf<T>())
}

internal class BukkitRegistryEntryValueSerializer<T : Keyed>(
    private val registryKey: RegistryKey<T>, type: TypeToken<T>,
) : ScalarSerializer<T>(type) {
    private val registry by lazy { RegistryAccess.registryAccess().getRegistry(registryKey) }

    override fun deserialize(type: Type, obj: Any): T {
        // TODO 如果想要让 nms 的对象可以在测试环境中“加载”, 大概可以创建两种
        //  不同的 ScalarSerializer 实现, 一个是在运行时使用的 (现在这个),
        //  一个是在测试环境摹刻出来的 (前提是 T 必须允许被继承/实现).
        return registry.getOrThrow(Key.key(obj.toString()))
    }

    override fun serialize(item: T, typeSupported: Predicate<Class<*>>): Any {
        return item.key().toString()
    }
}
//</editor-fold>

//<editor-fold desc="Vanilla Registry">
internal inline fun <reified T : Any> MojangRegistry<T>.valueByNameTypeSerializer(): ScalarSerializer<T> {
    return MojangRegistryValueEntrySerializer(this, typeTokenOf<T>())
}

internal class MojangRegistryValueEntrySerializer<T : Any>(
    private val registry: MojangRegistry<T>, type: TypeToken<T>,
) : ScalarSerializer<T>(type) {
    override fun deserialize(type: Type, obj: Any): T {
        return registry.getValueOrThrow(MojangResourceLocation.parse(obj.toString()))
    }

    override fun serialize(item: T, typeSupported: Predicate<Class<*>>): Any {
        return registry.getKey(item)?.toString() ?: throw SerializationException("No such value '$item' in registry '${registry.key()}'")
    }
}
//</editor-fold>