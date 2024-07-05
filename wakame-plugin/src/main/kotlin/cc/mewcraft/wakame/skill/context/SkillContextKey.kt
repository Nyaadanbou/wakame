package cc.mewcraft.wakame.skill.context

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.skill.Caster
import cc.mewcraft.wakame.skill.Target
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.audience.Audience
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import team.unnamed.mocha.MochaEngine
import java.util.stream.Stream

class SkillContextKey<T : Any>
private constructor(
    private val key: String,
) : Examinable {

    /**
     * 所有可用的 [SkillContextKey] 实例.
     */
    companion object {
        val CASTER: SkillContextKey<Caster> = SkillContextKey("caster")
        val CASTER_ENTITY: SkillContextKey<Caster.Single.Entity> = SkillContextKey("caster_entity")
        val CASTER_PLAYER: SkillContextKey<Caster.Single.Player> = SkillContextKey("caster_player")
        val CASTER_SKILL: SkillContextKey<Caster.Single.Skill> = SkillContextKey("caster_skill")
        val CASTER_COMPOSITE_NODE: SkillContextKey<Caster.CompositeNode> = SkillContextKey("caster_composite_node")
        val ITEM_STACK: SkillContextKey<ItemStack> = SkillContextKey("item_stack")
        val MOCHA_ENGINE: SkillContextKey<MochaEngine<*>> = SkillContextKey("mocha_engine")
        val NEKO_STACK: SkillContextKey<NekoStack> = SkillContextKey("neko_stack")
        val TARGET: SkillContextKey<Target> = SkillContextKey("target")
        val TARGET_LIVING_ENTITY: SkillContextKey<Target.LivingEntity> = SkillContextKey("target_living_entity")
        val TARGET_LOCATION: SkillContextKey<Target.Location> = SkillContextKey("target_location")
        val USER: SkillContextKey<User<Player>> = SkillContextKey("user")
        val CASTER_AUDIENCE: SkillContextKey<Audience> = SkillContextKey("caster_audience")
        val TARGET_AUDIENCE: SkillContextKey<Audience> = SkillContextKey("target_audience")
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("key", key)
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}
