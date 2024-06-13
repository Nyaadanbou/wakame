package cc.mewcraft.wakame.skill.context

import cc.mewcraft.wakame.skill.Caster
import cc.mewcraft.wakame.skill.Target
import org.bukkit.inventory.ItemStack
import java.util.HashMap

object SkillCastContextKeys {
    @JvmField
    val CASTER: SkillCastContextKey<Caster> =
        SkillCastContextKey("caster", Caster::class.java)
    @JvmField
    val CASTER_PLAYER: SkillCastContextKey<Caster.Player> =
        SkillCastContextKey("caster_player", Caster.Player::class.java)
    @JvmField
    val CASTER_ENTITY: SkillCastContextKey<Caster.Entity> =
        SkillCastContextKey("caster_entity", Caster.Entity::class.java)
    @JvmField
    val TARGET: SkillCastContextKey<Target> =
        SkillCastContextKey("target_living_entity", Target::class.java)
    @JvmField
    val TARGET_LIVING_ENTITY: SkillCastContextKey<Target.LivingEntity> =
        SkillCastContextKey("target_living_entity", Target.LivingEntity::class.java)
    @JvmField
    val TARGET_LOCATION: SkillCastContextKey<Target.Location> =
        SkillCastContextKey("target_location", Target.Location::class.java)
    @JvmField
    val ITEM_STACK: SkillCastContextKey<ItemStack> =
        SkillCastContextKey("item_stack", ItemStack::class.java)
}

class SkillCastContextKey<T : Any> private constructor(
    private val key: String,
    private val clazz: Class<T>
) {
    companion object Factory {
        private val REGISTRY_MAP: MutableMap<String, SkillCastContextKey<*>> = HashMap()

        internal operator fun <T : Any> invoke(key: String, clazz: Class<T>): SkillCastContextKey<T> {
            @Suppress("UNCHECKED_CAST")
            return REGISTRY_MAP.computeIfAbsent(key) { SkillCastContextKey(key, clazz) } as SkillCastContextKey<T>
        }

        fun <T : Any> get(key: String): SkillCastContextKey<T>? {
            @Suppress("UNCHECKED_CAST")
            return REGISTRY_MAP[key] as SkillCastContextKey<T>?
        }
    }

    override fun toString(): String {
        return key
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SkillCastContextKey<*>) return false
        if (key != other.key) return false
        if (clazz != other.clazz) return false
        return true
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + clazz.hashCode()
        return result
    }
}