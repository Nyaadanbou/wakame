package cc.mewcraft.wakame.ability.state.display

import cc.mewcraft.wakame.ability.AbilitySerializer
import cc.mewcraft.wakame.ability.display.AbilityDisplaySerializer
import cc.mewcraft.wakame.ability.trigger.AbilityTriggerSerializer
import cc.mewcraft.wakame.ability.trigger.SingleTrigger
import cc.mewcraft.wakame.ability.trigger.Trigger
import cc.mewcraft.wakame.adventure.AudienceMessageGroup
import cc.mewcraft.wakame.adventure.AudienceMessageGroupSerializer
import cc.mewcraft.wakame.adventure.CombinedAudienceMessageSerializer
import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.config.node
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.util.register
import me.lucko.helper.text3.mini
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.entity.Player
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import xyz.xenondevs.commons.provider.orElse

// FIXME 需要进一步整理这块配置文件的写法, 为了减少 diff 方便合并只做了最小修改

private val ABILITY_CONFIG = Configs["ability"]

@Init(
    stage = InitStage.PRE_CONFIG,
)
internal object AbilityConfigBootstrap {

    @InitFun
    fun init() {
        Configs.registerSerializer(
            "ability",
            TypeSerializerCollection.builder()
                .register(AbilitySerializer)
                .register(CombinedAudienceMessageSerializer)
                .register(AudienceMessageGroupSerializer)
                .register(AbilityDisplaySerializer)
                .register(AbilityTriggerSerializer)
                .build()
        )
    }

}

/**
 * Interface for displaying ability state to the user.
 */
interface StateDisplay<A : Audience> {
    fun displaySuccess(triggers: List<SingleTrigger>, audience: A)
    fun displayFailure(triggers: List<SingleTrigger>, audience: A)
    fun displayProgress(triggers: List<SingleTrigger>, audience: A)
    fun displayManaCost(count: Int, audience: A)
    fun displayNoEnoughMana(audience: A)
}

class PlayerStateDisplay : StateDisplay<Player> {
    private val triggerNames: Map<Trigger, String> by ABILITY_CONFIG.entry<Map<Trigger, String>>("display", "triggers")

    private val playerConfig = ABILITY_CONFIG.node("display", "player_state")

    private val connector: String by playerConfig.entry("connector")

    private val successMessages: AudienceMessageGroup by playerConfig.optionalEntry<AudienceMessageGroup>("success_message").orElse(AudienceMessageGroup.empty())
    private val failureMessages: AudienceMessageGroup by playerConfig.optionalEntry<AudienceMessageGroup>("failure_message").orElse(AudienceMessageGroup.empty())
    private val progressMessages: AudienceMessageGroup by playerConfig.optionalEntry<AudienceMessageGroup>("progress_message").orElse(AudienceMessageGroup.empty())
    private val manaCostMessages: AudienceMessageGroup by playerConfig.optionalEntry<AudienceMessageGroup>("mana_cost_message").orElse(AudienceMessageGroup.empty())
    private val noEnoughManaMessages: AudienceMessageGroup by playerConfig.optionalEntry<AudienceMessageGroup>("no_enough_mana_message").orElse(AudienceMessageGroup.empty())

    override fun displaySuccess(triggers: List<SingleTrigger>, audience: Player) {
        successMessages.send(audience, getTagResolver(triggers))
    }

    override fun displayProgress(triggers: List<SingleTrigger>, audience: Player) {
        progressMessages.send(audience, getTagResolver(triggers))
    }

    override fun displayFailure(triggers: List<SingleTrigger>, audience: Player) {
        failureMessages.send(audience, getTagResolver(triggers))
    }

    override fun displayManaCost(count: Int, audience: Player) {
        manaCostMessages.send(audience, Formatter.number("count", count))
    }

    override fun displayNoEnoughMana(audience: Player) {
        noEnoughManaMessages.send(audience)
    }

    private fun getTagResolver(triggers: List<SingleTrigger>): TagResolver {
        return Placeholder.component("trigger_completed") {
            val string = triggers.joinToString(separator = connector) { triggerNames[it].orEmpty() }
            string.mini
        }
    }
}