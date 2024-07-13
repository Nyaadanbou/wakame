package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.skill.Caster
import cc.mewcraft.wakame.skill.PassiveSkill
import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.SkillBase
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.tick.AbstractSkillTick
import cc.mewcraft.wakame.skill.tick.SkillTick
import cc.mewcraft.wakame.tick.TickResult
import cc.mewcraft.wakame.user.toUser
import net.kyori.adventure.key.Key
import java.util.UUID

/**
 * FIXME: 玩家重进的时候无法添加属性.
 */
interface Bloodrage : Skill, PassiveSkill {
    companion object Factory : SkillFactory<Bloodrage> {
        override fun create(key: Key, config: ConfigProvider): Bloodrage {
            val uniqueId = config.entry<UUID>("uuid")
            return DefaultImpl(key, config, uniqueId)
        }
    }

    private class DefaultImpl(
        key: Key,
        config: ConfigProvider,
        uniqueId: Provider<UUID>
    ) : Bloodrage, SkillBase(key, config) {
        private val uniqueId: UUID by uniqueId

        override fun cast(context: SkillContext): SkillTick {
            return Tick(context)
        }

        private inner class Tick(
            context: SkillContext
        ) : AbstractSkillTick(this@DefaultImpl, context) {
            private val attribute = Attributes.MAX_HEALTH

            override fun tick(tickCount: Long): TickResult {
                val player = CasterUtil.getCaster<Caster.Single.Player>(context)?.bukkitPlayer ?: return TickResult.INTERRUPT
                // When player health is below 50%
                val user = player.toUser()
                val attributeMap = user.attributeMap
                val maxHealth = attributeMap.getValue(Attributes.MAX_HEALTH)

                if (player.health <= maxHealth / 2) {
                    val modifier = AttributeModifier(uniqueId, "bloodrage", 1.5, AttributeModifier.Operation.MULTIPLY_TOTAL)
                    if (!attributeMap.hasModifier(attribute, uniqueId))
                        attributeMap.getInstance(attribute)?.addModifier(modifier)
                } else {
                    attributeMap.getInstance(attribute)?.removeModifier(uniqueId)
                }

                return TickResult.CONTINUE_TICK
            }

            override fun whenRemove() {
                val player = CasterUtil.getCaster<Caster.Single.Player>(context)?.bukkitPlayer ?: return
                val user = player.toUser()
                val attributeMap = user.attributeMap
                attributeMap.getInstance(attribute)?.removeModifier(uniqueId)
            }
        }
    }
}