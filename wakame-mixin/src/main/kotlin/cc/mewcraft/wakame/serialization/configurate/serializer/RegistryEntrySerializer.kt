package cc.mewcraft.wakame.serialization.configurate.serializer

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.lazyconfig.configurate.require
import cc.mewcraft.wakame.registry.Registry
import cc.mewcraft.wakame.registry.entry.RegistryEntry
import cc.mewcraft.wakame.util.KoishKey
import cc.mewcraft.wakame.util.MojangRegistry
import cc.mewcraft.wakame.util.MojangResourceLocation
import cc.mewcraft.wakame.util.adventure.asMinimalStringKoish
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import org.bukkit.Keyed
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

//<editor-fold desc="Koish Registry">
/*internal*/ fun <T : Any> Registry<T>.valueByNameTypeSerializer(): SimpleSerializer<T> {
    return RegistryValueEntrySerializer(this)
}

/*internal*/ fun <T : Any> Registry<T>.holderByNameTypeSerializer(): SimpleSerializer<RegistryEntry<T>> {
    return RegistryHolderEntrySerializer(this)
}

@PublishedApi
internal class RegistryValueEntrySerializer<T : Any>(
    private val registry: Registry<T>,
) : SimpleSerializer<T> {
    override fun deserialize(type: Type, node: ConfigurationNode): T {
        return registry.getOrThrow(node.require<String>())
    }

    override fun serialize(type: Type, obj: T?, node: ConfigurationNode) {
        if (obj == null) return
        val id = registry.getId(obj)?.asMinimalStringKoish() ?: throw SerializationException("No such value '$obj' in registry '${registry.key}'")
        node.set(id)
    }
}

@PublishedApi
internal class RegistryHolderEntrySerializer<T : Any>(
    private val registry: Registry<T>,
) : SimpleSerializer<RegistryEntry<T>> {
    override fun deserialize(type: Type, node: ConfigurationNode): RegistryEntry<T> {
        return registry.createEntry(node.require<KoishKey>())
    }

    override fun serialize(type: Type, obj: RegistryEntry<T>?, node: ConfigurationNode) {
        if (obj == null) return
        val id = obj.getKeyOrThrow().value.asMinimalStringKoish()
        node.set(id)
    }
}
//</editor-fold>

//<editor-fold desc="Paper Registry">
/*internal*/ fun <T : Keyed> RegistryKey<T>.valueByNameTypeSerializer(): SimpleSerializer<T> {
    return BukkitRegistryEntryValueSerializer(this)
}

@PublishedApi
internal class BukkitRegistryEntryValueSerializer<T : Keyed>(
    private val registryKey: RegistryKey<T>,
) : SimpleSerializer<T> {
    private val registry by lazy { RegistryAccess.registryAccess().getRegistry(registryKey) }

    override fun deserialize(type: Type, node: ConfigurationNode): T {
        // TODO 如果想要让 nms 的对象可以在测试环境中“加载”, 大概可以创建两种
        //  不同的 ScalarSerializer 实现, 一个是在运行时使用的 (现在这个),
        //  一个是在测试环境摹刻出来的 (前提是 T 必须允许被继承/实现).
        return node.get<String>()
            ?.let { KoishKey.key(it) }
            ?.let { registry.get(it) }
            ?: registry.first()
    }

    override fun serialize(type: Type, obj: T?, node: ConfigurationNode) {
        if (obj == null) return
        val id = obj.key().toString()
        node.set(id)
    }
}
//</editor-fold>

//<editor-fold desc="Vanilla Registry">
/*internal*/ fun <T : Any> MojangRegistry<T>.valueByNameTypeSerializer(): SimpleSerializer<T> {
    return MojangRegistryValueEntrySerializer(this)
}

@PublishedApi
internal class MojangRegistryValueEntrySerializer<T : Any>(
    private val registry: MojangRegistry<T>,
) : SimpleSerializer<T> {
    override fun deserialize(type: Type, node: ConfigurationNode): T {
        return registry.getValueOrThrow(MojangResourceLocation.parse(node.require<String>()))
    }

    override fun serialize(type: Type, obj: T?, node: ConfigurationNode) {
        if (obj == null) return
        val id = registry.getKey(obj)?.toString() ?: throw SerializationException("No such value '$obj' in registry '${registry.key()}")
        node.set(id)
    }
}
//</editor-fold>