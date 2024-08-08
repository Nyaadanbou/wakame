package cc.mewcraft.wakame.reforge.mod

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.item.components.cells.Curse
import net.kyori.examination.Examinable
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

/**
 * 词条栏的诅咒的匹配规则, 用于测试一个诅咒是否符合某种规则.
 */
interface CurseMatchRule : Examinable {
    fun test(curse: Curse): Boolean
}

internal object CurseMatchRuleSerializer : TypeSerializer<CurseMatchRule> {
    override fun deserialize(type: Type, node: ConfigurationNode): CurseMatchRule {
        return CurseMatchRuleAny
    }
}