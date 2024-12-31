package cc.mewcraft.wakame.display2.implementation.crafting_station

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.display2.IndexedText
import cc.mewcraft.wakame.display2.RendererFormat
import cc.mewcraft.wakame.display2.SimpleIndexedText
import cc.mewcraft.wakame.display2.TextMetaFactory
import cc.mewcraft.wakame.display2.TextMetaFactoryPredicate
import cc.mewcraft.wakame.item.components.PortableCore
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.enchantments.Enchantment
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
internal data class FuzzyEnchantmentRendererFormat(
    override val namespace: String,
    private val tooltip: String,
) : RendererFormat.Simple {
    override val id: String = "enchantments"
    override val index: Key = Key.key(namespace, id)
    override val textMetaFactory: TextMetaFactory = TextMetaFactory()
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate(namespace, id)

    /**
     * @param data 魔咒和等级的映射
     */
    fun render(data: Map<Enchantment, Int>): IndexedText {
        val tooltip = data.map { (enchantment, level) ->
            MM.deserialize(
                tooltip,
                Placeholder.component("name", enchantment.description()),
                Placeholder.component("level", Component.text(level)),
            )
        }
        return SimpleIndexedText(index, tooltip)
    }

    companion object {
        private val MM = Injector.get<MiniMessage>()
    }
}

@ConfigSerializable
internal data class FuzzyPortableCoreRendererFormat(
    override val namespace: String,
) : RendererFormat.Dynamic<PortableCore> {
    override val textMetaFactory = TextMetaFactory()
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate(namespace, "portable_core")

    private val unknownIndex = Key.key(namespace, "unknown")

    fun render(data: PortableCore): IndexedText {
        val core = data.wrapped as? AttributeCore
            ?: return SimpleIndexedText(unknownIndex, listOf())
        val index = Key.key(namespace, core.attribute.id)
        return SimpleIndexedText(index, core.description)
    }

    override fun computeIndex(data: PortableCore): Key {
        throw UnsupportedOperationException() // 直接在 render(...) 函数中处理
    }
}
