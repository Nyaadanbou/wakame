package cc.mewcraft.wakame.display2.implementation.standard

import cc.mewcraft.wakame.MM
import cc.mewcraft.wakame.display2.*
import cc.mewcraft.wakame.display2.implementation.common.*
import cc.mewcraft.wakame.entity.player.AttackSpeed
import cc.mewcraft.wakame.item2.data.impl.*
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.entity.EntityType
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
internal data class CoreAttributeRendererFormat(
    override val namespace: String,
    private val ordinal: AttributeCoreOrdinalFormat,
) : RendererFormat.Dynamic<AttributeCore> {
    override val textMetaFactory: TextMetaFactory = AttributeCoreTextMetaFactory(namespace, ordinal.operation, ordinal.element)
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate(namespace) { id: String -> BuiltInRegistries.ATTRIBUTE_FACADE.containsId(id) }

    fun render(data: AttributeCore): IndexedText {
        return SimpleIndexedText(computeIndex(data), data.description)
    }

    /**
     * 实现要求: 返回值必须是 [AttributeCoreTextMeta.derivedIndexes] 的子集.
     */
    override fun computeIndex(data: AttributeCore): Key {
        return data.computeIndex(namespace)
    }
}

@ConfigSerializable
internal data class CoreEmptyRendererFormat(
    override val namespace: String,
    private val tooltip: List<Component>,
) : RendererFormat.Simple {
    override val id: String = "core/empty"
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: TextMetaFactory = CyclicTextMetaFactory(namespace, id, CyclicIndexRule.SLASH)
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate(namespace, id)

    private val tooltipCycle = IndexedTextCycle(limit = CyclicTextMeta.MAX_DISPLAY_COUNT) { i ->
        SimpleIndexedText(CyclicIndexRule.SLASH.make(index, i), tooltip)
    }

    fun render(data: EmptyCore): IndexedText {
        return tooltipCycle.next()
    }
}

@ConfigSerializable
internal data class AttackSpeedRendererFormat(
    override val namespace: String,
    private val tooltip: Tooltip = Tooltip(),
) : RendererFormat.Simple {
    override val id: String = "attack_speed"
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: TextMetaFactory = TextMetaFactory()
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate(namespace, id)

    fun render(data: RegistryEntry<AttackSpeed>): IndexedText {
        val resolver = Placeholder.component("value", data.unwrap().displayName)
        return SimpleIndexedText(index, listOf(MM.deserialize(tooltip.line, resolver)))
    }

    @ConfigSerializable
    data class Tooltip(
        val line: String = "Attack Speed: <value>",
    )

    companion object Shared {
        private val UNKNOWN_LEVEL = Component.text("???")
    }
}

@ConfigSerializable
internal data class EntityBucketInfoRendererFormat(
    override val namespace: String,
    private val content: List<String>,
    private val abstract: Map<AbstractType, List<String>>,
    private val specific: Map<EntityType, List<String>>,
) : RendererFormat.Simple {
    override val id: String = "entity_bucket_info"
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: TextMetaFactory = TextMetaFactory.fixed()
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate.literal(namespace, id)

    fun render(info: EntityBucketInfo): IndexedText {
        val lines = arrayListOf<Component>()

        // 内容物加上
        lines += content.map { MM.deserialize(it, Placeholder.component("entity_type", info.typeName)) }

        // 先把抽象的部分加上
        if (info is EntityBucketInfo.Ageable) {
            val unparsed = abstract[AbstractType.AGEABLE] ?: emptyList()
            val resolver = Placeholder.component("is_adult", if (info.isAdult) Component.text("是") else Component.text("否"))
            lines += unparsed.map { MM.deserialize(it, resolver) }
        }
        if (info is EntityBucketInfo.CollarColorable) {
            val unparsed = abstract[AbstractType.COLLAR_COLORABLE] ?: emptyList()
            // TODO #404
        }
        if (info is EntityBucketInfo.Shearable) {
            val unparsed = abstract[AbstractType.SHEARABLE] ?: emptyList()
            // TODO #404
        }
        if (info is EntityBucketInfo.Tameable) {
            val unparsed = abstract[AbstractType.TAMEABLE] ?: emptyList()
            // TODO #404
        }
        if (info is EntityBucketInfo.Variable) {
            val unparsed = abstract[AbstractType.VARIABLE] ?: emptyList()
            // TODO #404
        }

        // 再把具体的部分加上
        when (info) {
            is ArmadilloEntityBucketInfo -> {
                val unparsed = specific[EntityType.ARMADILLO] ?: emptyList()
                val resolver = Placeholder.component("state", Component.text(info.state))
                lines += unparsed.map { MM.deserialize(it, resolver) }
            }

            is GoatEntityBucketInfo -> {
                // TODO #404
            }

            is OcelotEntityBucketInfo -> {
                // TODO #404
            }

            is TurtleEntityBucketInfo -> {
                // TODO #404
            }

            is AllayEntityBucketInfo -> {
                // TODO #404
            }

            is IronGolemEntityBucketInfo -> {
                // TODO #404
            }

            is SnowGolemEntityBucketInfo -> {
                // TODO #404
            }

            is VillagerEntityBucketInfo -> {
                // TODO #404
            }

            is WanderingTraderEntityBucketInfo -> {
                // TODO #404
            }

            is ZombieVillagerEntityBucketInfo -> {
                // TODO #404
            }

            else -> {}
        }

        // 返回最终结果
        return SimpleIndexedText(index, lines)
    }

    enum class AbstractType {
        AGEABLE,
        COLLAR_COLORABLE,
        SHEARABLE,
        TAMEABLE,
        VARIABLE
    }
}