package cc.mewcraft.wakame.skill

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

internal interface SkillSerializer<T : Skill> : TypeSerializer<T> {
    override fun serialize(type: Type, obj: T?, node: ConfigurationNode) {
        throw UnsupportedOperationException("Serialization is not supported")
    }
}