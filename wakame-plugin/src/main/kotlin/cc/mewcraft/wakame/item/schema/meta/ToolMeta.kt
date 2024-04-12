package cc.mewcraft.wakame.item.schema.meta

import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.annotation.ConfigPath
import cc.mewcraft.wakame.item.ItemMetaConstants
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import java.lang.reflect.Type
import java.util.stream.Stream

data class Tool(
    val rules: List<ToolRule>,
    val defaultMiningSpeed: Float? = null,
    val damagePerBlock: Int? = null,
) : Examinable {
    init {
        defaultMiningSpeed?.run { require(this >= 0) { "defaultMiningSpeed >= 0" } }
        damagePerBlock?.run { require(this >= 0) { "damagePerBlock >= 0" } }
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("defaultMiningSpeed", defaultMiningSpeed),
        ExaminableProperty.of("damagePerBlock", damagePerBlock),
        ExaminableProperty.of("rules", rules)
    )

    override fun toString(): String = toSimpleString()
}

data class ToolRule(
    val blocks: List<String>,
    val speed: Float? = null,
    val correctForDrops: Boolean? = null,
) : Examinable {
    init {
        speed?.run { require(this >= 0F) { "speed >= 0" } }
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("blocks", blocks),
        ExaminableProperty.of("speed", speed),
        ExaminableProperty.of("correctForDrops", correctForDrops),
    )

    override fun toString(): String = toSimpleString()
}

@ConfigPath(ItemMetaConstants.TOOL)
sealed interface SToolMeta : SchemaItemMeta<Tool> {
    override val key: Key get() = ItemMetaConstants.createKey { TOOL }
}

private class NonNullToolMeta(
    private val rules: List<ToolRule>,
    private val defaultMiningSpeed: Float? = null,
    private val damagePerBlock: Int? = null,
) : SToolMeta {
    init {
        defaultMiningSpeed?.run { require(this >= 0) { "defaultMiningSpeed >= 0" } }
        damagePerBlock?.run { require(this >= 0) { "damagePerBlock >= 0" } }
    }

    override val isEmpty: Boolean = false

    override fun generate(context: SchemaGenerationContext): GenerationResult<Tool> {
        return GenerationResult(Tool(rules, defaultMiningSpeed, damagePerBlock))
    }
}

private data object DefaultToolMeta : SToolMeta {
    override val isEmpty: Boolean = true
    override fun generate(context: SchemaGenerationContext): GenerationResult<Tool> = GenerationResult.empty()
}

internal data object ToolMetaSerializer : SchemaItemMetaSerializer<SToolMeta> {
    override val defaultValue: SToolMeta = DefaultToolMeta
    override fun deserialize(type: Type, node: ConfigurationNode): SToolMeta {
        val rules = node.node("rules").get<List<ToolRule>>(emptyList())
        val defaultMiningSpeed = node.node("default_mining_speed").get<Float>()
        val damagePerBlock = node.node("damage_per_block").get<Int>()
        return NonNullToolMeta(rules, defaultMiningSpeed, damagePerBlock)
    }
}

internal data object ToolRuleSerializer : SchemaSerializer<ToolRule> {
    override fun deserialize(type: Type, node: ConfigurationNode): ToolRule {
        val blocks = node.node("blocks").krequire<List<String>>()
        val speed = node.node("speed").get<Float>()
        val correctForDrops = node.node("correctForDrops").get<Boolean>()
        return ToolRule(blocks, speed, correctForDrops)
    }
}