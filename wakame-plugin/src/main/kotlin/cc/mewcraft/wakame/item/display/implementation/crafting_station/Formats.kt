package cc.mewcraft.wakame.item.display.implementation.crafting_station

import cc.mewcraft.wakame.MM
import cc.mewcraft.wakame.item.data.impl.AttributeCore
import cc.mewcraft.wakame.item.data.impl.Core
import cc.mewcraft.wakame.item.display.*
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
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
}

@ConfigSerializable
internal data class FuzzyCoreRendererFormat(
    override val namespace: String,
) : RendererFormat.Dynamic<Core> {
    override val textMetaFactory = TextMetaFactory()
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate(namespace, "core")

    private val unknownIndex = Key.key(namespace, "unknown")

    fun render(data: Core): IndexedText {
        val core = data as? AttributeCore
            ?: return SimpleIndexedText(unknownIndex, listOf())
        val index = Key.key(namespace, core.wrapped.id)
        return SimpleIndexedText(index, core.description)
    }

    override fun computeIndex(data: Core): Key {
        throw UnsupportedOperationException() // 直接在 render(...) 函数中处理
    }
}
