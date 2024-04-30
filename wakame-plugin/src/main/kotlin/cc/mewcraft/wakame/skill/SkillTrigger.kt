package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.adventure.Keyed
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

sealed interface SkillTrigger : Keyed {
    /**
     * The identifier of the trigger. This is used for concatenation of triggers in a combo.
     */
    val id: Char

    data object LeftClick : SkillTrigger {
        override val id: Char = '0'
        override val key: Key = Key(Namespaces.TRIGGER, "generic/left_click")
    }

    data object RightClick : SkillTrigger {
        override val id: Char = '1'
        override val key: Key = Key(Namespaces.TRIGGER, "generic/right_click")
    }

    data object Attack : SkillTrigger {
        override val id: Char = '2'
        override val key: Key = Key(Namespaces.TRIGGER, "generic/attack")
    }

    data object Jump : SkillTrigger {
        override val id: Char = '3'
        override val key: Key = Key(Namespaces.TRIGGER, "generic/jump")
    }

    data object Noop : SkillTrigger {
        override val id: Char = '4'
        override val key: Key = Key(Namespaces.TRIGGER, "generic/noop")
    }

    data class Combo(
        val triggers: List<SkillTrigger>,
    ) : SkillTrigger {
        private val idString: String = triggers.joinToString("") { it.id.toString() }
        override val id: Char = Char.MIN_VALUE
        override val key: Key = Key(Namespaces.TRIGGER, "combo/$idString")
    }
}

internal object SkillTriggerSerializer : SchemaSerializer<SkillTrigger> {
    override fun deserialize(type: Type, node: ConfigurationNode): SkillTrigger {
        val key = Key(node.string.orEmpty())
        return SkillRegistry.TRIGGER_INSTANCES[key]
    }
}