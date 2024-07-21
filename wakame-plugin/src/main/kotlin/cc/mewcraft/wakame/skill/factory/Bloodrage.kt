package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.skill.*
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.tick.AbstractSkillTick
import cc.mewcraft.wakame.skill.tick.SkillTick
import cc.mewcraft.wakame.tick.TickResult
import cc.mewcraft.wakame.user.toUser
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import java.util.*

/**
 * 玩家血量低于一定值的时候触发效果.
 */
interface Bloodrage : Skill, PassiveSkill {

    val uniqueId: UUID

    companion object Factory : SkillFactory<Bloodrage> {
        override fun create(key: Key, config: ConfigurationNode): Bloodrage {
            val uniqueId = config.node("uuid").krequire<UUID>()
            return DefaultImpl(key, config, uniqueId)
        }
    }

    private class DefaultImpl(
        key: Key,
        config: ConfigurationNode,
        override val uniqueId: UUID
    ) : Bloodrage, SkillBase(key, config) {
        override fun cast(context: SkillContext): SkillTick<Bloodrage> {
            return BloodrageTick(context, this)
        }
    }
}

private class BloodrageTick(
    context: SkillContext,
    private val bloodrage: Bloodrage
) : AbstractSkillTick<Bloodrage>(bloodrage, context) {
    private val attribute = Attributes.MAX_HEALTH

    override fun tick(): TickResult {
        if (!checkConditions())
            return TickResult.ALL_DONE
        val player = CasterUtils.getCaster<Caster.Single.Player>(context)?.bukkitPlayer ?: return TickResult.INTERRUPT
        // When player health is below 50%
        val user = player.toUser()
        val attributeMap = user.attributeMap
        val maxHealth = attributeMap.getValue(Attributes.MAX_HEALTH)

        if (player.health <= maxHealth / 2) {
            val modifier = AttributeModifier(bloodrage.uniqueId, "bloodrage", 1.5, AttributeModifier.Operation.MULTIPLY_TOTAL)
            if (!attributeMap.hasModifier(attribute, bloodrage.uniqueId))
                attributeMap.getInstance(attribute)?.addModifier(modifier)
        } else {
            attributeMap.getInstance(attribute)?.removeModifier(bloodrage.uniqueId)
        }

        return TickResult.CONTINUE_TICK
    }

    override fun whenRemove() {
        val player = CasterUtils.getCaster<Caster.Single.Player>(context)?.bukkitPlayer ?: return
        val user = player.toUser()
        val attributeMap = user.attributeMap
        attributeMap.getInstance(attribute)?.removeModifier(bloodrage.uniqueId)
    }
}