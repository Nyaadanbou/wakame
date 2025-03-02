package cc.mewcraft.wakame.ability.state.display

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.ability.AbilityRegistryLoader
import cc.mewcraft.wakame.ability.trigger.SingleTrigger
import cc.mewcraft.wakame.ability.trigger.Trigger
import cc.mewcraft.wakame.adventure.AudienceMessageGroup
import cc.mewcraft.wakame.adventure.AudienceMessageGroupSerializer
import cc.mewcraft.wakame.adventure.CombinedAudienceMessageSerializer
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.registry2.RegistryConfigStorage
import cc.mewcraft.wakame.util.buildYamlConfigLoader
import cc.mewcraft.wakame.util.register
import cc.mewcraft.wakame.util.require
import cc.mewcraft.wakame.util.text.mini
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
object PlayerComboInfoDisplay : RegistryConfigStorage {
    const val CONFIG_NAME = "ability.yml"

    private lateinit var triggerNames: Map<Trigger, String>

    private lateinit var connector: String
    private lateinit var successMessages: AudienceMessageGroup
    private lateinit var failureMessages: AudienceMessageGroup
    private lateinit var progressMessages: AudienceMessageGroup
    private lateinit var manaCostMessages: AudienceMessageGroup
    private lateinit var noEnoughManaMessages: AudienceMessageGroup

    @InitFun
    @ReloadFun
    private fun loadConfiguration() {
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

            triggerNames = rootNode.node("display", "triggers").require()
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

    fun displaySuccess(triggers: List<SingleTrigger>, audience: Player) {
        successMessages.send(audience, getTagResolver(triggers))
    }

    fun displayProgress(triggers: List<SingleTrigger>, audience: Player) {
        progressMessages.send(audience, getTagResolver(triggers))
    }

    fun displayFailure(triggers: List<SingleTrigger>, audience: Player) {
        failureMessages.send(audience, getTagResolver(triggers))
    }

    fun displayManaCost(count: Int, audience: Player) {
        manaCostMessages.send(audience, Formatter.number("count", count))
    }

    fun displayNoEnoughMana(audience: Player) {
        noEnoughManaMessages.send(audience)
    }

    private fun getTagResolver(triggers: List<SingleTrigger>): TagResolver {
        return Placeholder.component("trigger_completed") {
            val string = triggers.joinToString(separator = connector) { triggerNames[it].orEmpty() }
            string.mini
        }
    }
}