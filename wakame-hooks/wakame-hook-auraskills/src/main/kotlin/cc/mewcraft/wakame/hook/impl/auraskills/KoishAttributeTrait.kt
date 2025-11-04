package cc.mewcraft.wakame.hook.impl.auraskills

import cc.mewcraft.wakame.entity.attribute.Attribute
import cc.mewcraft.wakame.entity.attribute.AttributeModifier
import cc.mewcraft.wakame.entity.player.attributeContainer
import dev.aurelium.auraskills.api.AuraSkillsBukkit
import dev.aurelium.auraskills.api.bukkit.BukkitTraitHandler
import dev.aurelium.auraskills.api.trait.Trait
import dev.aurelium.auraskills.api.user.SkillsUser
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player

open class KoishAttributeTrait(
    protected val attribute: Attribute,
    protected val trait: Trait,
) : BukkitTraitHandler {

    protected val modifierKey: Key = Key.key("koish", "auraskills/trait/${attribute.id}")

    override fun getBaseLevel(player: Player, trait: Trait): Double {
        val attributeContainer = player.attributeContainer
        if (!attributeContainer.hasAttribute(attribute)) return .0
        return attributeContainer.getBaseValue(attribute)
    }

    override fun onReload(player: Player, user: SkillsUser, trait: Trait) {
        if (trait != this.trait) return
        set(player, user, trait, this.attribute)
    }

    override fun getTraits(): Array<out Trait> {
        return arrayOf(this.trait)
    }

    protected fun set(player: Player, user: SkillsUser, trait: Trait, attribute: Attribute) {
        val attributeContainer = player.attributeContainer
        val attributeInstance = attributeContainer.getInstance(attribute) ?: return

        // 先移除原本的由本 Trait 添加的 AttributeModifier
        attributeInstance.removeModifier(modifierKey)

        // 遵循 AuraSkills 的一些全局限制
        if (!trait.isEnabled)
            return
        if (AuraSkillsBukkit.get().locationManager.isPluginDisabled(player.location, player))
            return

        val amount = user.getBonusTraitLevel(trait)
        if (amount < 0.01)
            return
        attributeInstance.addTransientModifier(AttributeModifier(modifierKey, amount, AttributeModifier.Operation.ADD))
    }
}