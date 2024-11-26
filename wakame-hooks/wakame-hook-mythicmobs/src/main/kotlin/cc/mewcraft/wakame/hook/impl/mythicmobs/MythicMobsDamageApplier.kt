package cc.mewcraft.wakame.hook.impl.mythicmobs

import cc.mewcraft.wakame.damage.DamageApplier
import com.google.common.collect.Maps
import io.lumine.mythic.api.adapters.SkillAdapter
import io.lumine.mythic.api.mobs.GenericCaster
import io.lumine.mythic.api.skills.damage.DamageMetadata
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.bukkit.adapters.item.ItemComponentBukkitItemStack
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.inventory.ItemStack

/**
 * 一个用 MythicMobs 实现的 [DamageApplier].
 */
// 本实现是为了解决MM技能造成的伤害会无限递归问题.
// 开发日记 2024/11/26 小米
// 这就是一坨狗屎.
object MythicMobsDamageApplier : DamageApplier {
    private const val MAGIC_DAMAGE_AMOUNT = 4.95

    override fun damage(victim: LivingEntity, source: LivingEntity?, amount: Double) {
        val shitVictim = BukkitAdapter.adapt(victim)
        val shitDamager = if (source is Player) {
            // 如果是玩家, 则封装成*看似*更合适的实例
            MythicBukkit.inst().playerManager.getProfile(source)
        } else {
            // 不是玩家的话一律按 GenericCaster 处理
            GenericCaster(BukkitAdapter.adapt(source))
        }

        val shitDamageMetadata = DamageMetadata(
            /* damager = */ shitDamager,
            /* damagerItem = */ ItemComponentBukkitItemStack(ItemStack.empty()),
            /* amount = */ MAGIC_DAMAGE_AMOUNT,
            /* bonusDamage = */ Maps.newTreeMap<String, Double>(),
            /* bonusDamageModifiers = */ Maps.newTreeMap<String, Double>(),
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

        // 该函数会触发一个 MythicDamageEvent, 其中包含了发起攻击的实体和受到攻击的实体.
        // 我们必须监听这个 MythicDamageEvent 来修改伤害的结果 (例如遵循领地和保护区).
        //
        // 虽然这个事件也会触发一个 EntityDamageEvent, 但那个里面的 DamageSource
        // 是一个 MM 自己实现的 DamageSource, 完全不包含发起攻击的实体.
        // 也就是说, 这个 EntityDamageEvent 是没法正常使用的.
        // 这种狗屎设计也只有 MM 能做出来了.
        SkillAdapter.get().doDamage(shitDamageMetadata, shitVictim)
    }
}