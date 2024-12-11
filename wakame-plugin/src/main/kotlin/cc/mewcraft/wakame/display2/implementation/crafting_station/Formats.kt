package cc.mewcraft.wakame.display2.implementation.crafting_station

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.display2.IndexedText
import cc.mewcraft.wakame.display2.RendererFormat
import cc.mewcraft.wakame.display2.SimpleIndexedText
import cc.mewcraft.wakame.display2.SimpleTextMeta
import cc.mewcraft.wakame.display2.SourceIndex
import cc.mewcraft.wakame.display2.SourceOrdinal
import cc.mewcraft.wakame.display2.TextMetaFactory
import cc.mewcraft.wakame.display2.implementation.SingleSimpleTextMeta
import cc.mewcraft.wakame.display2.implementation.SingleSimpleTextMetaFactory
import cc.mewcraft.wakame.item.components.PortableCore
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.enchantments.Enchantment
import org.koin.core.component.get
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Required
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
internal data class FuzzyEnchantmentRendererFormat(
    @Setting @Required
    override val namespace: String,
    @Setting
    private val tooltip: String = "<name> <level>",
) : RendererFormat.Simple {
    override val id = "enchantments"
    override val index = Key.key(namespace, id)
    override val textMetaFactory = SingleSimpleTextMetaFactory(namespace, id)

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
    @Setting @Required
    override val namespace: String,
) : RendererFormat.Dynamic<PortableCore> {
    private val unknownIndex = Key.key(namespace, "unknown")
    override val textMetaFactory = FuzzyPortableCoreTextMetaFactory(namespace)

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

internal data class FuzzyPortableCoreTextMetaFactory(
    override val namespace: String,
) : TextMetaFactory {
    override fun test(sourceIndex: SourceIndex): Boolean {
        return sourceIndex.namespace() == namespace && sourceIndex.value() == "portable_core"
    }

    override fun create(sourceIndex: SourceIndex, sourceOrdinal: SourceOrdinal, defaultText: List<Component>?): SimpleTextMeta {
        return SingleSimpleTextMeta(sourceIndex, sourceOrdinal, defaultText)
    }
}