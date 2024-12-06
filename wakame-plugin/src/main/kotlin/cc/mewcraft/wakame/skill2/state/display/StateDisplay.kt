package cc.mewcraft.wakame.skill2.state.display

import cc.mewcraft.wakame.adventure.AudienceMessageGroup
import cc.mewcraft.wakame.skill2.SkillSupport
import cc.mewcraft.wakame.skill2.trigger.SingleTrigger
import cc.mewcraft.wakame.skill2.trigger.Trigger
import me.lucko.helper.text3.mini
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.entity.Player
import xyz.xenondevs.commons.provider.immutable.orElse

/**
 * Interface for displaying skill state to the user.
 */
interface StateDisplay<A : Audience> {
    fun displaySuccess(triggers: List<SingleTrigger>, audience: A)
    fun displayFailure(triggers: List<SingleTrigger>, audience: A)
    fun displayProgress(triggers: List<SingleTrigger>, audience: A)
}

class EntityStateDisplay : StateDisplay<Player> {
    private val triggerNames: Map<Trigger, String> by SkillSupport.GLOBAL_STATE_DISPLAY_CONFIG.entry("triggers")

    private val playerConfig = SkillSupport.GLOBAL_STATE_DISPLAY_CONFIG.node("player_state")

    private val connector: String by playerConfig.entry("connector")

    private val successMessages: AudienceMessageGroup by playerConfig.optionalEntry<AudienceMessageGroup>("success_message").orElse(AudienceMessageGroup.empty())
    private val failureMessages: AudienceMessageGroup by playerConfig.optionalEntry<AudienceMessageGroup>("failure_message").orElse(AudienceMessageGroup.empty())
    private val progressMessages: AudienceMessageGroup by playerConfig.optionalEntry<AudienceMessageGroup>("progress_message").orElse(AudienceMessageGroup.empty())

    override fun displayProgress(triggers: List<SingleTrigger>, audience: Player) {
        successMessages.send(audience, getTagResolver(triggers))
    }

    override fun displayFailure(triggers: List<SingleTrigger>, audience: Player) {
        failureMessages.send(audience, getTagResolver(triggers))
    }

    override fun displaySuccess(triggers: List<SingleTrigger>, audience: Player) {
        progressMessages.send(audience, getTagResolver(triggers))
    }

    private fun getTagResolver(triggers: List<SingleTrigger>): TagResolver {
        return Placeholder.component("trigger_completed") {
            val string = triggers.joinToString(separator = connector) { triggerNames[it].orEmpty() }
            string.mini
        }
    }
}