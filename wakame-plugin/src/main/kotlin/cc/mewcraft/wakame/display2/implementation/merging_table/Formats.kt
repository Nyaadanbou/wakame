package cc.mewcraft.wakame.display2.implementation.merging_table

import cc.mewcraft.wakame.display2.*
import cc.mewcraft.wakame.item.components.PortableCore
import cc.mewcraft.wakame.util.adventure.plain
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.format.Style
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.NodeKey
import java.util.regex.Pattern


@ConfigSerializable
internal data class MergeOutputRendererFormat(
    override val namespace: String,
    @NodeKey
    override val id: String,
    private val attributeFormat: AttributeFormat,
) : RendererFormat.Simple {
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: TextMetaFactory = TextMetaFactory()
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate(namespace, id)

    fun render(data: PortableCore): IndexedText {
        return SimpleIndexedText(
            index, data.description
                .map(Component::plain) // 先转成纯文本, 使得 regex 可以正常匹配
                .map(Component::text)
                .map { it.replaceText(attributeFormat.replacementConfig) }
                .map { it.style(attributeFormat.style) }
        )
    }

    @ConfigSerializable
    data class AttributeFormat(
        val style: Style,
        private val pattern: Pattern,
        private val replacement: Component,
    ) {
        val replacementConfig = TextReplacementConfig.builder().match(pattern).replacement(replacement).build()
    }
}