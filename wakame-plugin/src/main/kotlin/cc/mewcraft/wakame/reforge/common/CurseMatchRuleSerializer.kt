package cc.mewcraft.wakame.reforge.common

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type


internal object CurseMatchRuleSerializer : TypeSerializer<CurseMatchRule> {
    override fun deserialize(type: Type, node: ConfigurationNode): CurseMatchRule {
        return CurseMatchRuleAny
    }
}