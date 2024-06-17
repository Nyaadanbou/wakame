package cc.mewcraft.wakame.skill.context

import cc.mewcraft.wakame.item.binary.NekoStack
import cc.mewcraft.wakame.skill.Caster
import cc.mewcraft.wakame.skill.Target
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.stream.Stream

class SkillCastContextKey<T : Any>
private constructor(
    private val key: String,
) : Examinable {

    /**
     * 所有可用的 [SkillCastContextKey] 实例.
     */
    companion object {
        val CASTER: SkillCastContextKey<Caster> = SkillCastContextKey("caster")
        val CASTER_ENTITY: SkillCastContextKey<Caster.Entity> = SkillCastContextKey("caster_entity")
        val CASTER_PLAYER: SkillCastContextKey<Caster.Player> = SkillCastContextKey("caster_player")
        val ITEM_STACK: SkillCastContextKey<ItemStack> = SkillCastContextKey("item_stack")
        val NEKO_STACK: SkillCastContextKey<NekoStack> = SkillCastContextKey("neko_stack")
        val TARGET: SkillCastContextKey<Target> = SkillCastContextKey("target")
        val TARGET_LIVING_ENTITY: SkillCastContextKey<Target.LivingEntity> = SkillCastContextKey("target_living_entity")
        val TARGET_LOCATION: SkillCastContextKey<Target.Location> = SkillCastContextKey("target_location")
        val USER: SkillCastContextKey<User<Player>> = SkillCastContextKey("user")
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
