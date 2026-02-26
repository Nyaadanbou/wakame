package cc.mewcraft.wakame.item.display.implementation.standard

import cc.mewcraft.wakame.entity.player.AttackSpeed
import cc.mewcraft.wakame.item.ItemStackEffectiveness
import cc.mewcraft.wakame.item.data.impl.*
import cc.mewcraft.wakame.item.display.*
import cc.mewcraft.wakame.item.display.implementation.common.*
import cc.mewcraft.wakame.item.property.impl.*
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.registry.entry.RegistryEntry
import cc.mewcraft.wakame.util.MojangStack
import net.kyori.adventure.extra.kotlin.join
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
internal data class CoreAttributeRendererFormat(
    override val namespace: String,
    private val ordinal: AttributeCoreOrdinalFormat,
) : RendererFormat.Dynamic<AttributeCore> {
    override val textMetaFactory: TextMetaFactory = AttributeCoreTextMetaFactory(namespace, ordinal.operation, ordinal.element)
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate.predicate(namespace) { id: String -> BuiltInRegistries.ATTRIBUTE_FACADE.containsId(id) }

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
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate.literal(namespace, id)

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
    override val textMetaFactory: TextMetaFactory = TextMetaFactory.fixed()
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate.literal(namespace, id)

    fun render(data: RegistryEntry<AttackSpeed>): IndexedText {
        val resolver = Placeholder.component("value", data.unwrap().displayName)
        return SimpleIndexedText(index, listOf(MiniMessage.miniMessage().deserialize(tooltip.line, resolver)))
    }

    @ConfigSerializable
    data class Tooltip(
        val line: String = "Attack Speed: <value>",
    )
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
        lines += content.map { MiniMessage.miniMessage().deserialize(it, Placeholder.component("entity_type", info.typeName)) }

        // 先把抽象的部分加上
        if (info is EntityBucketInfo.Ageable) {
            val unparsed = abstract[AbstractType.AGEABLE] ?: emptyList()
            val resolver = Placeholder.unparsed("is_adult", if (info.isAdult) "是" else "否")
            lines += unparsed.map { MiniMessage.miniMessage().deserialize(it, resolver) }
        }
        if (info is EntityBucketInfo.CollarColorable) {
            val unparsed = abstract[AbstractType.COLLAR_COLORABLE] ?: emptyList()
            val resolver = Placeholder.unparsed("collar_color", info.collarColor)
            lines += unparsed.map { MiniMessage.miniMessage().deserialize(it, resolver) }
        }
        if (info is EntityBucketInfo.Shearable) {
            val unparsed = abstract[AbstractType.SHEARABLE] ?: emptyList()
            val resolver = Placeholder.unparsed("ready_to_be_sheared", if (info.readyToBeSheared) "是" else "否")
            lines += unparsed.map { MiniMessage.miniMessage().deserialize(it, resolver) }
        }
        if (info is EntityBucketInfo.Tameable) {
            val ownerName = info.ownerName
            if (ownerName != null) {
                val unparsed = abstract[AbstractType.TAMEABLE] ?: emptyList()
                val resolver = Placeholder.unparsed("owner_name", ownerName)
                lines += unparsed.map { MiniMessage.miniMessage().deserialize(it, resolver) }
            }
        }
        if (info is EntityBucketInfo.Variable) {
            val unparsed = abstract[AbstractType.VARIABLE] ?: emptyList()
            val resolver = Placeholder.unparsed("variant", info.variant)
            lines += unparsed.map { MiniMessage.miniMessage().deserialize(it, resolver) }
        }

        // 再把具体的部分加上
        when (info) {
            is ArmadilloEntityBucketInfo -> {
                val unparsed = specific[EntityType.ARMADILLO] ?: emptyList()
                val resolver = Placeholder.component("state", Component.text(info.state))
                lines += unparsed.map { MiniMessage.miniMessage().deserialize(it, resolver) }
            }

            is GoatEntityBucketInfo -> {
                val unparsed = specific[EntityType.GOAT] ?: emptyList()
                val resolver = TagResolver.builder()
                    .resolver(Placeholder.unparsed("has_left_horn", if (info.hasLeftHorn) "有" else "无"))
                    .resolver(Placeholder.unparsed("has_right_horn", if (info.hasRightHorn) "有" else "无"))
                    .build()
                lines += unparsed.map { MiniMessage.miniMessage().deserialize(it, resolver) }
            }

            is OcelotEntityBucketInfo -> {
                val unparsed = specific[EntityType.OCELOT] ?: emptyList()
                val resolver = Placeholder.unparsed("trusting", if (info.trusting) "是" else "否")
                lines += unparsed.map { MiniMessage.miniMessage().deserialize(it, resolver) }
            }

            is TurtleEntityBucketInfo -> {
                val unparsed = specific[EntityType.TURTLE] ?: emptyList()
                val resolver = Placeholder.unparsed("has_egg", if (info.hasEgg) "是" else "否")
                lines += unparsed.map { MiniMessage.miniMessage().deserialize(it, resolver) }
            }

            is AllayEntityBucketInfo -> {
                val unparsed = specific[EntityType.ALLAY] ?: emptyList()
                val resolver = TagResolver.builder()
                    .resolver(Placeholder.unparsed("item_in_mainhand", info.itemInMainhand ?: "无"))
                    .resolver(Placeholder.unparsed("item_in_offhand", info.itemInOffhand ?: "无"))
                    .build()
                lines += unparsed.map { MiniMessage.miniMessage().deserialize(it, resolver) }
            }

            is IronGolemEntityBucketInfo -> {
                val unparsed = specific[EntityType.IRON_GOLEM] ?: emptyList()
                val resolver = Placeholder.unparsed("player_created", if (info.isPlayerCreated) "是" else "否")
                lines += unparsed.map { MiniMessage.miniMessage().deserialize(it, resolver) }
            }

            is SnowGolemEntityBucketInfo -> {
                val unparsed = specific[EntityType.SNOW_GOLEM] ?: emptyList()
                val resolver = Placeholder.unparsed("pumpkin_type", if (info.hasPumpkin) "有" else "无")
                lines += unparsed.map { MiniMessage.miniMessage().deserialize(it, resolver) }
            }

            is VillagerEntityBucketInfo -> {
                val unparsed = specific[EntityType.VILLAGER] ?: emptyList()
                val resolver = TagResolver.builder()
                    .resolver(Placeholder.unparsed("level", info.level.toString()))
                    .resolver(Placeholder.unparsed("region", info.region))
                    .resolver(Placeholder.unparsed("profession", info.profession))
                    .build()
                lines += unparsed.map { MiniMessage.miniMessage().deserialize(it, resolver) }
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
                lines += unparsed.map { MiniMessage.miniMessage().deserialize(it, resolver) }
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

@ConfigSerializable
internal data class CastableRendererFormat(
    override val namespace: String,
    @Setting("content")
    private val content: List<String>,
    @Setting("trigger")
    private val trigger: Trigger,
) : RendererFormat.Simple {
    override val id: String = "castable"
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: TextMetaFactory = TextMetaFactory.fixed()
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate.literal(namespace, id)

    fun render(data: Map<String, Castable>): IndexedText {
        val lines = data.flatMap { (_, v) -> render(v) }
        return SimpleIndexedText(index, lines)
    }

    private fun render(data: Castable): List<Component> {
        val skillTrigger = data.trigger.unwrap()
        val triggerConfig = this.trigger
        val lines = when (skillTrigger) {
            is SpecialCastableTrigger -> {
                val line = triggerConfig.special.generate(skillTrigger)
                content.map { MiniMessage.miniMessage().deserialize(it, Placeholder.component("trigger", line)) }
            }

            is GenericCastableTrigger -> {
                val line = triggerConfig.generic.generate(skillTrigger)
                content.map { MiniMessage.miniMessage().deserialize(it, Placeholder.component("trigger", line)) }
            }

            is SequenceCastableTrigger -> {
                val line = triggerConfig.sequence.generate(skillTrigger)
                content.map { MiniMessage.miniMessage().deserialize(it, Placeholder.component("trigger", line)) }
            }

            is InputCastableTrigger -> {
                val line = triggerConfig.input.generate(skillTrigger)
                content.map { MiniMessage.miniMessage().deserialize(it, Placeholder.component("trigger", line)) }
            }
        }
        return lines
    }

    @ConfigSerializable
    data class Trigger(
        @Setting("special")
        val special: Special,
        @Setting("generic")
        val generic: Generic,
        @Setting("sequence")
        val sequence: Sequence,
        @Setting("input")
        val input: Input,
    )

    @ConfigSerializable
    data class Special(
        val onEquip: Component,
        val onUnequip: Component,
        val onConsume: Component,
    ) {
        fun generate(trigger: SpecialCastableTrigger): Component {
            return when (trigger) {
                SpecialCastableTrigger.ON_EQUIP -> onEquip
                SpecialCastableTrigger.ON_UNEQUIP -> onUnequip
                SpecialCastableTrigger.ON_CONSUME -> onConsume
            }
        }
    }

    @ConfigSerializable
    data class Generic(
        val leftClick: Component,
        val rightClick: Component,
    ) {
        fun generate(trigger: GenericCastableTrigger): Component {
            return when (trigger) {
                GenericCastableTrigger.LEFT_CLICK -> leftClick
                GenericCastableTrigger.RIGHT_CLICK -> rightClick
            }
        }
    }

    @ConfigSerializable
    data class Sequence(
        val left: Component,
        val right: Component,
        val separator: Component,
    ) {
        fun generate(trigger: SequenceCastableTrigger): Component {
            return trigger.sequence.map {
                when (it) {
                    GenericCastableTrigger.LEFT_CLICK -> left
                    GenericCastableTrigger.RIGHT_CLICK -> right
                }
            }.join(
                JoinConfiguration.separator(separator)
            )
        }
    }

    @ConfigSerializable
    data class Input(
        val forward: Component,
        val backward: Component,
        val left: Component,
        val right: Component,
        val jump: Component,
        val sneak: Component,
        val sprint: Component,
    ) {
        fun generate(trigger: InputCastableTrigger): Component {
            return when (trigger) {
                InputCastableTrigger.FORWARD -> forward
                InputCastableTrigger.BACKWARD -> backward
                InputCastableTrigger.LEFT -> left
                InputCastableTrigger.RIGHT -> right
                InputCastableTrigger.JUMP -> jump
                InputCastableTrigger.SNEAK -> sneak
                InputCastableTrigger.SPRINT -> sprint
            }
        }
    }
}

@ConfigSerializable
internal data class NetworkPositionRendererFormat(
    override val namespace: String,
    @Setting("content")
    private val content: List<String>,
    @Setting("server_map")
    private val serverMap: Map<String, String>,
    @Setting("dimension_map")
    private val dimensionMap: Map<String, String>,
) : RendererFormat.Simple {
    override val id: String = "network_position"
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: TextMetaFactory = TextMetaFactory.fixed()
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate.literal(namespace, id)

    fun render(data: NetworkPosition): IndexedText {
        val resolver = TagResolver.builder()
            .resolver(Formatter.number("x", data.x))
            .resolver(Formatter.number("y", data.y))
            .resolver(Formatter.number("z", data.z))
            .resolver(Formatter.number("yaw", data.yaw))
            .resolver(Formatter.number("pitch", data.pitch))
            .resolver(Placeholder.unparsed("world", dimensionMap[data.world] ?: data.world))
            .resolver(Placeholder.unparsed("server", serverMap[data.server] ?: data.server))
            .build()
        val lines = content.map { MiniMessage.miniMessage().deserialize(it, resolver) }
        return SimpleIndexedText(index, lines)
    }
}

@ConfigSerializable
internal data class EffectivenessRendererFormat(
    override val namespace: String,
    private val ordinal: List<Ordinal>,
    private val badSlot: Component,
    private val badLevel: Component,
    private val badDamage: Component,
) : RendererFormat.Simple {
    override val id: String = "effectiveness"
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: TextMetaFactory = TextMetaFactory.fixed()
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate.literal(namespace, id)

    fun render(player: Player?, item: MojangStack): IndexedText {
        val components = ArrayList<Component>()
        for (ord in ordinal) {
            when (ord) {
                Ordinal.BAD_LEVEL -> {
                    if (player != null && ItemStackEffectiveness.testLevel(player, item).not()) {
                        components += badLevel
                    }
                }

                Ordinal.BAD_DAMAGE -> {
                    if (ItemStackEffectiveness.testDamaged(item).not()) {
                        components += badDamage
                    }
                }
            }
        }
        return if (components.isEmpty()) {
            IndexedText.NO_OP
        } else {
            SimpleIndexedText(index, components)
        }
    }

    enum class Ordinal {
        BAD_LEVEL, BAD_DAMAGE
    }
}