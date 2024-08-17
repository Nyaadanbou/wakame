package cc.mewcraft.wakame.reforge.common

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.item.components.cells.Curse
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type
import java.util.stream.Stream

/**
 * 词条栏的诅咒的匹配规则, 用于测试一个诅咒是否符合某种规则.
 */
interface CurseMatchRule : Examinable {
    companion object {
        /**
         * 创建一个匹配所有诅咒的 [CurseMatchRule] 实例.
         */
        fun any(): CurseMatchRule = CurseMatchRuleAny
    }

    fun test(curse: Curse): Boolean
}

/**
 * [CurseMatchRule] 的序列化器.
 */
internal object CurseMatchRuleSerializer : TypeSerializer<CurseMatchRule> {
    override fun deserialize(type: Type, node: ConfigurationNode): CurseMatchRule {
        return CurseMatchRuleAny
    }
}


/* Implementations */


private data object CurseMatchRuleAny : CurseMatchRule {
    override fun test(curse: Curse): Boolean = true
    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of()
    override fun toString(): String = toSimpleString()
}