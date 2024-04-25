package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.adventure.Keyed
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

interface SkillTrigger : Keyed {
    companion object {
        val GENERIC: List<SkillTrigger> = listOf(LeftClick, RightClick, Attack, Jump)
        val COMBO: List<SkillTrigger> = listOf(LeftClick, RightClick)
    }

    /**
     * The identifier of the trigger. This is used for concatenation of triggers in a combo.
     */
    val id: String

    data object LeftClick : SkillTrigger {
        override val id: String = '0'.toString()
        override val key: Key = Key(Namespaces.TRIGGER, "generic/left_click")
    }

    data object RightClick : SkillTrigger {
        override val id: String = '1'.toString()
        override val key: Key = Key(Namespaces.TRIGGER, "generic/right_click")
    }

    data object Attack : SkillTrigger {
        override val id: String = '2'.toString()
        override val key: Key = Key(Namespaces.TRIGGER, "generic/attack")
    }

    data object Jump : SkillTrigger {
        override val id: String = '3'.toString()
        override val key: Key = Key(Namespaces.TRIGGER, "generic/jump")
    }

    data object Noop : SkillTrigger {
        override val id: String = '4'.toString()
        override val key: Key = Key(Namespaces.TRIGGER, "generic/noop")
    }

    data class Combo(
        val triggers: List<SkillTrigger>,
    ) : SkillTrigger {
        override val id: String = triggers.joinToString("") { it.id }
        override val key: Key = Key(Namespaces.TRIGGER, "combo/$id")
    }
}

object SkillTriggerSerializer : SchemaSerializer<SkillTrigger> {
    override fun deserialize(type: Type, node: ConfigurationNode): SkillTrigger {
        val key = Key(node.string.orEmpty())
        return SkillRegistry.TRIGGERS[key]
    }
}