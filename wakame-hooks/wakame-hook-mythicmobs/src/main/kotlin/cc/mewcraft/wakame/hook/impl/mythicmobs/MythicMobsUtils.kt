package cc.mewcraft.wakame.hook.impl.mythicmobs

import com.google.common.collect.Maps
import io.lumine.mythic.api.skills.SkillMetadata
import org.bukkit.event.entity.EntityDamageEvent
import io.lumine.mythic.api.skills.damage.DamageMetadata as MythicDamageMetadata

/**
 * 请务必注意:
 * MythicMobs 可能随时修改其 api.
 * 包括但不限于构造器, 方法参数列表, 元数据使用的字符串.
 */
object MythicMobsUtils {
    private const val MAGIC_DAMAGE_AMOUNT = 4.95

    fun createMythicDamageMetadata(data: SkillMetadata): MythicDamageMetadata {
        return MythicDamageMetadata(
            /* damager = */ data.caster,
            /* metadata = */ data,
            /* damagerItem = */ data.casterItem,
            /* amount = */ MAGIC_DAMAGE_AMOUNT,
            /* bonusDamage = */ Maps.newTreeMap(),
            /* bonusDamageModifiers = */ Maps.newTreeMap(),
            /* element = */ null,
            /* multiplier = */ 1.0,
            /* ignoresArmor = */ false,
            /* preventsImmunity = */ false,
            /* preventsKnockback = */ false,
            /* ignoreEnchantments = */ false,
            /* damageCause = */ EntityDamageEvent.DamageCause.CUSTOM
        ).apply {
            putBoolean("trigger_skills", false)
        }
    }

}