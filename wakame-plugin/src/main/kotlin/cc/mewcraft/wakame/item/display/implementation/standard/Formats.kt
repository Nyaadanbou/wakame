package cc.mewcraft.wakame.item.display.implementation.standard

import cc.mewcraft.wakame.MM
import cc.mewcraft.wakame.entity.player.AttackSpeed
import cc.mewcraft.wakame.item.data.impl.*
import cc.mewcraft.wakame.item.display.*
import cc.mewcraft.wakame.item.display.implementation.common.*
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.registry.entry.RegistryEntry
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
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
            val resolver = Placeholder.unparsed("is_adult", if (info.isAdult) "是" else "否")
            lines += unparsed.map { MM.deserialize(it, resolver) }
        }
        if (info is EntityBucketInfo.CollarColorable) {
            val unparsed = abstract[AbstractType.COLLAR_COLORABLE] ?: emptyList()
            val resolver = Placeholder.unparsed("collar_color", info.collarColor)
            lines += unparsed.map { MM.deserialize(it, resolver) }
        }
        if (info is EntityBucketInfo.Shearable) {
            val unparsed = abstract[AbstractType.SHEARABLE] ?: emptyList()
            val resolver = Placeholder.unparsed("ready_to_be_sheared", if (info.readyToBeSheared) "是" else "否")
            lines += unparsed.map { MM.deserialize(it, resolver) }
        }
        if (info is EntityBucketInfo.Tameable) {
            val ownerName = info.ownerName
            if (ownerName != null) {
                val unparsed = abstract[AbstractType.TAMEABLE] ?: emptyList()
                val resolver = Placeholder.unparsed("owner_name", ownerName)
                lines += unparsed.map { MM.deserialize(it, resolver) }
            }
        }
        if (info is EntityBucketInfo.Variable) {
            val unparsed = abstract[AbstractType.VARIABLE] ?: emptyList()
            val resolver = Placeholder.unparsed("variant", info.variant)
            lines += unparsed.map { MM.deserialize(it, resolver) }
        }

        // 再把具体的部分加上
        when (info) {
            is ArmadilloEntityBucketInfo -> {
                val unparsed = specific[EntityType.ARMADILLO] ?: emptyList()
                val resolver = Placeholder.component("state", Component.text(info.state))
                lines += unparsed.map { MM.deserialize(it, resolver) }
            }

            is GoatEntityBucketInfo -> {
                val unparsed = specific[EntityType.GOAT] ?: emptyList()
                val resolver = TagResolver.builder()
                    .resolver(Placeholder.unparsed("has_left_horn", if (info.hasLeftHorn) "有" else "无"))
                    .resolver(Placeholder.unparsed("has_right_horn", if (info.hasRightHorn) "有" else "无"))
                    .build()
                lines += unparsed.map { MM.deserialize(it, resolver) }
            }

            is OcelotEntityBucketInfo -> {
                val unparsed = specific[EntityType.OCELOT] ?: emptyList()
                val resolver = Placeholder.unparsed("trusting", if (info.trusting) "是" else "否")
                lines += unparsed.map { MM.deserialize(it, resolver) }
            }

            is TurtleEntityBucketInfo -> {
                val unparsed = specific[EntityType.TURTLE] ?: emptyList()
                val resolver = Placeholder.unparsed("has_egg", if (info.hasEgg) "是" else "否")
                lines += unparsed.map { MM.deserialize(it, resolver) }
            }

            is AllayEntityBucketInfo -> {
                val unparsed = specific[EntityType.ALLAY] ?: emptyList()
                val resolver = TagResolver.builder()
                    .resolver(Placeholder.unparsed("item_in_mainhand", info.itemInMainhand ?: "无"))
                    .resolver(Placeholder.unparsed("item_in_offhand", info.itemInOffhand ?: "无"))
                    .build()
                lines += unparsed.map { MM.deserialize(it, resolver) }
            }

            is IronGolemEntityBucketInfo -> {
                val unparsed = specific[EntityType.IRON_GOLEM] ?: emptyList()
                val resolver = Placeholder.unparsed("player_created", if (info.isPlayerCreated) "是" else "否")
                lines += unparsed.map { MM.deserialize(it, resolver) }
            }

            is SnowGolemEntityBucketInfo -> {
                val unparsed = specific[EntityType.SNOW_GOLEM] ?: emptyList()
                val resolver = Placeholder.unparsed("pumpkin_type", if (info.hasPumpkin) "有" else "无")
                lines += unparsed.map { MM.deserialize(it, resolver) }
            }

            is VillagerEntityBucketInfo -> {
                val unparsed = specific[EntityType.VILLAGER] ?: emptyList()
                val resolver = TagResolver.builder()
                    .resolver(Placeholder.unparsed("level", info.level.toString()))
                    .resolver(Placeholder.unparsed("region", info.region))
                    .resolver(Placeholder.unparsed("profession", info.profession))
                    .build()
                lines += unparsed.map { MM.deserialize(it, resolver) }
            }

            is WanderingTraderEntityBucketInfo -> {
                lines += info.offers.map { Component.text(it) }
            }

            is ZombieVillagerEntityBucketInfo -> {
                val unparsed = specific[EntityType.ZOMBIE_VILLAGER] ?: emptyList()
                val resolver = TagResolver.builder()
                    .resolver(Placeholder.unparsed("region", info.region))
                    .resolver(Placeholder.unparsed("profession", info.profession))
                    .build()
                lines += unparsed.map { MM.deserialize(it, resolver) }
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