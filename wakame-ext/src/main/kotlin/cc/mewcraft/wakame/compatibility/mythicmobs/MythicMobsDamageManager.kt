package cc.mewcraft.wakame.compatibility.mythicmobs

import cc.mewcraft.wakame.damage.DamageManagerApi
import cc.mewcraft.wakame.damage.DamageMetadata
import com.google.common.collect.Maps
import io.lumine.mythic.api.adapters.SkillAdapter
import io.lumine.mythic.api.mobs.GenericCaster
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.adapters.item.ItemComponentBukkitItemStack
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.inventory.ItemStack
import io.lumine.mythic.api.skills.damage.DamageMetadata as MythicDamageMetadata

object MythicMobsDamageManager : DamageManagerApi {
    override fun hurt(victim: LivingEntity, damageMetadata: DamageMetadata, source: LivingEntity?, knockback: Boolean) {
        val mythicSource = BukkitAdapter.adapt(source)
        val mythicVictim = BukkitAdapter.adapt(victim)
        val genericDamager = GenericCaster(mythicSource)

        val mythicDamageMetadata = MythicDamageMetadata(
            /* damager = */ genericDamager,
            /* damagerItem = */ ItemComponentBukkitItemStack(ItemStack.empty()),
            /* amount = */ 4.95,
            /* bonusDamage = */ Maps.newTreeMap<String, Double>(),
            /* bonusDamageModifiers = */ Maps.newTreeMap<String, Double>(),
            /* element = */ null,
            /* multiplier = */ 1.0,
            /* ignoresArmor = */ false,
            /* preventsImmunity = */ false,
            /* preventsKnockback = */ !knockback,
            /* ignoreEnchantments = */ false,
            /* damageCause = */ EntityDamageEvent.DamageCause.CUSTOM
        ).apply {
            putBoolean("trigger_skills", true)
        }

        SkillAdapter.get().doDamage(mythicDamageMetadata, mythicVictim)
    }
}