package cc.mewcraft.wakame.hook.impl.auraskills

import cc.mewcraft.wakame.entity.attribute.Attributes
import cc.mewcraft.wakame.integration.auraskills.ManaTraitBridge
import dev.aurelium.auraskills.api.AuraSkillsApi
import dev.aurelium.auraskills.api.trait.TraitModifier
import dev.aurelium.auraskills.api.trait.Traits
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import cc.mewcraft.wakame.entity.player.User as KoishUser

object AuraSkillsManaTraitBridge : ManaTraitBridge {
    private const val MODIFIER_MANA_REGEN = "koish_attribute_mana_regen"
    private const val MODIFIER_MAX_MANA = "koish_attribute_max_mana"

    override fun onTickUser(user: KoishUser, player: Player) {
        val auraUser = AuraSkillsApi.get().getUser(player.uniqueId)
        //val itemSlotChanges = user.itemSlotChanges
        val attributeContainer = user.attributeContainer
        if (Bukkit.getCurrentTick() % 20 != 0) {
            return
        }
        auraUser.removeTraitModifier(MODIFIER_MANA_REGEN)
        auraUser.removeTraitModifier(MODIFIER_MAX_MANA)
        auraUser.addTraitModifier(TraitModifier(MODIFIER_MANA_REGEN, Traits.MANA_REGEN, attributeContainer.getValue(Attributes.MANA_REGENERATION)))
        auraUser.addTraitModifier(TraitModifier(MODIFIER_MAX_MANA, Traits.MAX_MANA, attributeContainer.getValue(Attributes.MAX_MANA)))
        //itemSlotChanges.forEachChangingEntry { slot, curr, prev ->
        //    if (prev != null && ItemStackEffectiveness.testSlot(slot, prev)) {
        //        auraUser.removeTraitModifier(MODIFIER_MANA_REGEN)
        //        auraUser.removeTraitModifier(MODIFIER_MAX_MANA)
        //    }
        //    if (curr != null &&
        //        ItemStackEffectiveness.testSlot(slot, curr) &&
        //        ItemStackEffectiveness.testLevel(player, curr) &&
        //        ItemStackEffectiveness.testDamaged(curr)
        //    ) {
        //        auraUser.addTraitModifier(TraitModifier(MODIFIER_MANA_REGEN, Traits.MANA_REGEN, attributeContainer.getValue(Attributes.MANA_REGENERATION)))
        //        auraUser.addTraitModifier(TraitModifier(MODIFIER_MAX_MANA, Traits.MAX_MANA, attributeContainer.getValue(Attributes.MAX_MANA)))
        //    }
        //}
    }
}