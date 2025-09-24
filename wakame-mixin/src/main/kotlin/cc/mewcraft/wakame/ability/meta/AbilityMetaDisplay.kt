package cc.mewcraft.wakame.ability.meta

import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.adventure.toSimpleString
import cc.mewcraft.wakame.util.require
import net.kyori.adventure.text.Component
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.kotlin.extensions.get
import java.lang.reflect.Type
import java.util.stream.Stream

/**
 * 代表了一个技能显示给玩家的信息.
 */
interface AbilityMetaDisplay {
    companion object {
        @JvmField
        val EMPTY: AbilityMetaDisplay = EmptyAbilityMetaDisplay

        @JvmField
        val SERIALIZER: TypeSerializer2<AbilityMetaDisplay> = Serializer
    }

    val displayName: Component
    val displayDescription: List<Component>

    private object Serializer : TypeSerializer2<AbilityMetaDisplay> {
        override fun deserialize(type: Type, node: ConfigurationNode): AbilityMetaDisplay? {
            val displayName = node.node("name").require<Component>()
            val displayDescription = node.node("description").get<List<Component>>(emptyList())

            return SimpleAbilityMetaDisplay(
                displayName = displayName,
                displayDescription = displayDescription,
            )
        }

        override fun emptyValue(specificType: Type, options: ConfigurationOptions): AbilityMetaDisplay? {
            return EMPTY
        }
    }
}

private data object EmptyAbilityMetaDisplay : AbilityMetaDisplay {
    override val displayName: Component = Component.text("未命名技能")
    override val displayDescription: List<Component> = emptyList()
}

private data class SimpleAbilityMetaDisplay(
    override val displayName: Component,
    override val displayDescription: List<Component>,
) : AbilityMetaDisplay, Examinable {

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("displayName", displayName),
            ExaminableProperty.of("displayDescription", displayDescription),
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}