package cc.mewcraft.wakame.ability.display

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.util.krequire
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

sealed interface AbilityDisplay {
    val name: String
    val tooltips: List<String>

    companion object {
        fun empty(): AbilityDisplay = EmptyAbilityDisplay
    }
}

private data object EmptyAbilityDisplay : AbilityDisplay {
    override val name: String = ""
    override val tooltips: List<String> = emptyList()
}

private data class AbilityDisplayImpl(
    override val name: String,
    override val tooltips: List<String>,
) : AbilityDisplay


internal object AbilityDisplaySerializer : TypeSerializer<AbilityDisplay> {
    override fun deserialize(type: Type, node: ConfigurationNode): AbilityDisplay {
        return AbilityDisplayImpl(
            name = node.node("name").krequire(),
            tooltips = node.node("tooltips").krequire()
        )
    }
}
