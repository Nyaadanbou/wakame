package cc.mewcraft.wakame.display2.implementation.common

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.display2.DerivedIndex
import cc.mewcraft.wakame.display2.IndexedText
import cc.mewcraft.wakame.display2.RendererFormat
import cc.mewcraft.wakame.display2.SimpleIndexedText
import cc.mewcraft.wakame.display2.TextMetaFactory
import cc.mewcraft.wakame.display2.implementation.SingleSimpleTextMetaFactory
import cc.mewcraft.wakame.item.components.PortableCore
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import cc.mewcraft.wakame.rarity.Rarity
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.koin.core.component.get
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.NodeKey
import org.spongepowered.configurate.objectmapping.meta.Required
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class RarityRendererFormat(
    override val namespace: String,
    private val simple: String,
    private val complex: String,
) : RendererFormat.Simple {
    override val id: String = "rarity"
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: TextMetaFactory = SingleSimpleTextMetaFactory(namespace, id)

    fun renderSimple(rarity: Rarity): IndexedText {
        return SimpleIndexedText(
            index, listOf(
                MM.deserialize(
                    simple,
                    Placeholder.component("rarity_display_name", rarity.displayName)
                )
            )
        )
    }

    fun renderComplex(rarity: Rarity, modCount: Int): IndexedText {
        return SimpleIndexedText(
            index, listOf(
                MM.deserialize(
                    complex,
                    Placeholder.component("rarity_display_name", rarity.displayName),
                    Placeholder.component("reforge_mod_count", Component.text(modCount.toString()))
                )
            )
        )
    }

    companion object {
        private val MM = Injector.get<MiniMessage>()
    }
}

@ConfigSerializable
internal data class PortableCoreRendererFormat(
    @Setting @Required
    override val namespace: String,
    @Setting @NodeKey
    override val id: String,
) : RendererFormat.Simple {
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: SingleSimpleTextMetaFactory = SingleSimpleTextMetaFactory(namespace, id)
    private val unknownIndex = Key.key(namespace, "unknown")

    fun render(data: PortableCore): IndexedText {
        val core = (data.wrapped as? AttributeCore)
            ?: return SimpleIndexedText(unknownIndex, emptyList())
        return SimpleIndexedText(index, core.description)
    }
}