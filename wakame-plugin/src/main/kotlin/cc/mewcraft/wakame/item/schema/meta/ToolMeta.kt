package cc.mewcraft.wakame.item.schema.meta

import cc.mewcraft.wakame.annotation.ConfigPath
import cc.mewcraft.wakame.item.ItemMetaConstants
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
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
    val defaultMiningSpeed: Float = 1F,
    val damagePerBlock: Int = 1,
) : Examinable {
    init {
        require(defaultMiningSpeed >= 0) { "defaultMiningSpeed >= 0" }
        require(damagePerBlock >= 0) { "damagePerBlock >= 0" }
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("defaultMiningSpeed", defaultMiningSpeed),
        ExaminableProperty.of("damagePerBlock", damagePerBlock),
    )

    override fun toString(): String = toSimpleString()
}

@ConfigPath(ItemMetaConstants.TOOL)
sealed interface SToolMeta : SchemaItemMeta<Tool> {
    override val key: Key get() = ItemMetaConstants.createKey { TOOL }
}

private class NonNullToolMeta(
    private val rules: List<ToolRule>,
    private val defaultMiningSpeed: Float = 1F,
    private val damagePerBlock: Int = 1,
) : SToolMeta {
    override val isEmpty: Boolean = false

    init {
        require(defaultMiningSpeed >= 0) { "defaultMiningSpeed >= 0" }
        require(damagePerBlock >= 0) { "damagePerBlock >= 0" }
    }

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
        val defaultMiningSpeed = node.node("default_mining_speed").getFloat(1F)
        val damagePerBlock = node.node("damage_per_block").getInt(1)
        return NonNullToolMeta(rules, defaultMiningSpeed, damagePerBlock)
    }
}

data class ToolRule(
    val blocks: List<String>,
    val speed: Float,
    val correctForDrops: Boolean
)