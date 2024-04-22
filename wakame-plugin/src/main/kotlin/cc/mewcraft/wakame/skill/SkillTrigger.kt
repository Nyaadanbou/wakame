package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.adventure.Keyed
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

interface SkillTrigger : Keyed {
    companion object {
        private val TRIGGERS: Map<Key, SkillTrigger> = mapOf(
            Jump.key to Jump,
            Noop.key to Noop
        )

        fun fromStringOrNull(string: String): SkillTrigger? {
            return TRIGGERS[Key(string)]
        }

        fun fromString(string: String): SkillTrigger {
            return fromStringOrNull(string) ?: throw IllegalArgumentException("Unknown trigger: $string")
        }

        fun values(): Collection<SkillTrigger> {
            return TRIGGERS.values
        }
    }

    data object Jump : SkillTrigger {
        override val key: Key = Key(Namespaces.TRIGGER, "generic/jump")
    }
    data object Noop : SkillTrigger {
        override val key: Key = Key(Namespaces.TRIGGER, "generic/noop")
    }
}

object SkillTriggerSerializer : SchemaSerializer<SkillTrigger> {
    override fun deserialize(type: Type, node: ConfigurationNode): SkillTrigger {
        return SkillTrigger.fromString(node.string.orEmpty())
    }
}