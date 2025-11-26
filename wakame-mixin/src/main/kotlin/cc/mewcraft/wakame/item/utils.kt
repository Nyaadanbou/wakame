@file:JvmName("KoishItemUtils")

package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.integration.playermana.PlayerManaIntegration
import cc.mewcraft.wakame.integration.skill.SkillIntegration
import cc.mewcraft.wakame.integration.skill.SkillWrapper
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import cc.mewcraft.wakame.item.property.impl.Castable as CastableProp
import org.bukkit.Sound as BukkitSound

/**
 * Unified skill casting logic.
 *
 * - For [SkillWrapper.Block]: check cooldown first, then consume mana, and finally cast the skill.
 * - For [SkillWrapper.Inline]: only check mana and cast the skill (because inline skills don't have cooldowns).
 *
 * Try to cast a skill with unified cooldown and mana checks, sending action bar messages on failure.
 */
fun tryCastSkill(player: Player, castable: CastableProp) {
    val skill = castable.skill
    val manaCost = castable.manaCost

    if (skill is SkillWrapper.Block) {
        if (SkillIntegration.isCooldown(player, skill.id, castable)) {
            player.sendActionBar(TranslatableMessages.MSG_ERR_SKILL_ON_COOLDOWN)
            player.playSound(Sound.sound().type(BukkitSound.ENTITY_SHULKER_HURT).volume(.5f).build())
            return
        }
        if (!PlayerManaIntegration.consumeMana(player, manaCost)) {
            player.sendActionBar(TranslatableMessages.MSG_ERR_NOT_ENOUGH_MANA)
            player.playSound(Sound.sound().type(BukkitSound.ENTITY_SHULKER_HURT).volume(.5f).build())
            return
        } else {
            if (manaCost > 0) {
                player.sendActionBar(TranslatableMessages.MSG_MANA_CONSUMED.arguments(Component.text(manaCost)))
                player.playSound(Sound.sound().type(BukkitSound.ENTITY_EXPERIENCE_ORB_PICKUP).volume(.5f).build())
            }
        }
        skill.cast(player, castable) // cast!
    } else {
        if (!PlayerManaIntegration.consumeMana(player, manaCost)) {
            player.sendActionBar(TranslatableMessages.MSG_ERR_NOT_ENOUGH_MANA)
            player.playSound(Sound.sound().type(BukkitSound.ENTITY_SHULKER_HURT).volume(.5f).build())
            return
        }
        if (manaCost > 0) {
            player.sendActionBar(TranslatableMessages.MSG_MANA_CONSUMED.arguments(Component.text(manaCost)))
            player.playSound(Sound.sound().type(BukkitSound.ENTITY_EXPERIENCE_ORB_PICKUP).volume(.5f).build())
        }
        skill.cast(player, castable) // cast!
    }
}
