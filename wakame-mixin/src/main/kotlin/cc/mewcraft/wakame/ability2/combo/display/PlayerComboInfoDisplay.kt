package cc.mewcraft.wakame.ability2.combo.display

import cc.mewcraft.wakame.ability2.trigger.AbilitySingleTrigger
import cc.mewcraft.wakame.ability2.trigger.AbilityTrigger
import cc.mewcraft.wakame.adventure.AudienceMessageGroup
import cc.mewcraft.wakame.config.ConfigAccess
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.config.node
import cc.mewcraft.wakame.registry2.RegistryLoader
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.entity.Player

/**
 * 玩家打出的 Combo 的显示器.
 */
internal object PlayerComboInfoDisplay : RegistryLoader {
    private val GLOBAL_ABILITY_CONFIG = ConfigAccess.INSTANCE["ability.yml"]

    private val triggerDisplays: Map<AbilityTrigger, TriggerDisplay> by GLOBAL_ABILITY_CONFIG.entry("display", "triggers")

    private val playerComboConfig = GLOBAL_ABILITY_CONFIG.node("display", "player_combo")
    private val connector: Component by playerComboConfig.entry("connector")
    private val successMessages: AudienceMessageGroup by playerComboConfig.entry("success_message")
    private val failureMessages: AudienceMessageGroup by playerComboConfig.entry("failure_message")
    private val progressMessages: AudienceMessageGroup by playerComboConfig.entry("progress_message")
    private val manaCostMessages: AudienceMessageGroup by playerComboConfig.entry("mana_cost_message")
    private val noEnoughManaMessages: AudienceMessageGroup by playerComboConfig.entry("no_enough_mana_message")

    fun displaySuccess(triggers: List<AbilitySingleTrigger>, audience: Player) {
        val triggerTagResolver = getTriggersTagResolver(triggers) { it.successStyle }
        successMessages.send(audience, triggerTagResolver)
    }

    fun displayProgress(triggers: List<AbilitySingleTrigger>, audience: Player) {
        val triggerTagResolver = getTriggersTagResolver(triggers) { it.progressStyle }
        progressMessages.send(audience, triggerTagResolver)
    }

    fun displayFailure(triggers: List<AbilitySingleTrigger>, audience: Player) {
        val triggerTagResolver = getTriggersTagResolver(triggers) { it.failureStyle }
        failureMessages.send(audience, triggerTagResolver)
    }

    fun displayManaCost(count: Int, audience: Player) {
        manaCostMessages.send(audience, Formatter.number("count", count))
    }

    fun displayNotEnoughMana(audience: Player) {
        noEnoughManaMessages.send(audience)
    }

    private fun getTriggersTagResolver(triggers: List<AbilitySingleTrigger>, style: (TriggerDisplay) -> Style): TagResolver {
        return Placeholder.component("trigger_completed") {
            Component.join(
                JoinConfiguration.separator(connector),
                triggers.map { triggerDisplays[it] }
                    .map { it?.let { triggerDisplay -> Component.text().content(triggerDisplay.name).style(style(triggerDisplay)) } ?: Component.empty() }
            )
        }
    }

}