package cc.mewcraft.wakame.ability.combo.display

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.ability.AbilityRegistryLoader
import cc.mewcraft.wakame.ability.trigger.AbilitySingleTrigger
import cc.mewcraft.wakame.ability.trigger.AbilityTrigger
import cc.mewcraft.wakame.adventure.AudienceMessageGroup
import cc.mewcraft.wakame.adventure.AudienceMessageGroupSerializer
import cc.mewcraft.wakame.adventure.CombinedAudienceMessageSerializer
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.registry2.RegistryLoader
import cc.mewcraft.wakame.util.buildYamlConfigLoader
import cc.mewcraft.wakame.util.register
import cc.mewcraft.wakame.util.require
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
@Init(
    stage = InitStage.PRE_WORLD,
    runAfter = [
        AbilityRegistryLoader::class,
    ]
)
@Reload(
    runAfter = [
        AbilityRegistryLoader::class,
    ]
)
internal object PlayerComboInfoDisplay : RegistryLoader {
    const val CONFIG_NAME = "ability.yml"

    private lateinit var triggerDisplays: Map<AbilityTrigger, TriggerDisplay>

    private lateinit var connector: Component
    private lateinit var successMessages: AudienceMessageGroup
    private lateinit var failureMessages: AudienceMessageGroup
    private lateinit var progressMessages: AudienceMessageGroup
    private lateinit var manaCostMessages: AudienceMessageGroup
    private lateinit var noEnoughManaMessages: AudienceMessageGroup

    @InitFun
    @ReloadFun
    fun loadConfiguration() {
        val file = getFileInConfigDirectory(CONFIG_NAME)
        val loader = buildYamlConfigLoader {
            withDefaults()
            serializers {
                register(CombinedAudienceMessageSerializer)
                register(AudienceMessageGroupSerializer)
            }
        }

        try {
            val rootNode = loader.buildAndLoadString(file.readText())
            val playerComboConfig = rootNode.node("display", "player_combo")

            triggerDisplays = rootNode.node("display", "triggers").require()
            connector = playerComboConfig.node("connector").require()
            successMessages = playerComboConfig.node("success_message").require()
            failureMessages = playerComboConfig.node("failure_message").require()
            progressMessages = playerComboConfig.node("progress_message").require()
            manaCostMessages = playerComboConfig.node("mana_cost_message").require()
            noEnoughManaMessages = playerComboConfig.node("no_enough_mana_message").require()
        } catch (e: Exception) {
            LOGGER.error("Failed to load ability config", e)
        }
    }

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