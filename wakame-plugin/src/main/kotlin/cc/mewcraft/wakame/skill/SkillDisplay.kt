package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.util.krequire
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

sealed interface SkillDisplay {
    val display: String
    val tooltips: List<String>
}

data object EmptySkillDisplay : SkillDisplay {
    override val display: String = ""
    override val tooltips: List<String> = emptyList()
}

private data class SkillDisplayImpl(
    override val display: String,
    override val tooltips: List<String>
) : SkillDisplay

internal object SkillDisplaySerializer : SchemaSerializer<SkillDisplay> {
    override fun deserialize(type: Type, node: ConfigurationNode): SkillDisplay {
        return SkillDisplayImpl(
            display = node.node("display").krequire(),
            tooltips = node.node("tooltips").krequire()
        )
    }
}