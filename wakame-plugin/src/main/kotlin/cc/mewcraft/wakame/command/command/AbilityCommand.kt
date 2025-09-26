package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.ability.AbilityCastUtils
import cc.mewcraft.wakame.ability.meta.AbilityMeta
import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.KoishCommandFactory
import cc.mewcraft.wakame.command.koishHandler
import cc.mewcraft.wakame.command.parser.AbilityMetaParser
import org.incendo.cloud.bukkit.data.SingleEntitySelector
import org.incendo.cloud.bukkit.data.SinglePlayerSelector
import org.incendo.cloud.bukkit.parser.selector.SingleEntitySelectorParser
import org.incendo.cloud.bukkit.parser.selector.SinglePlayerSelectorParser
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.kotlin.extension.getOrNull
import org.incendo.cloud.paper.util.sender.Source

internal object AbilityCommand : KoishCommandFactory<Source> {

    override fun KoishCommandFactory.Builder<Source>.createCommands() {
        val commonBuilder = build {
            permission(CommandPermissions.ABILITY)
            literal("ability")
        }

        buildAndAdd(commonBuilder) {
            literal("cast")
            required("caster", SinglePlayerSelectorParser.singlePlayerSelectorParser())
            required("ability", AbilityMetaParser.abilityMetaParser())
            optional("target", SingleEntitySelectorParser.singleEntitySelectorParser())
            koishHandler(handler = ::handleCastAbilityAtTarget)
        }
    }

    private fun handleCastAbilityAtTarget(context: CommandContext<Source>) {
        val ability = context.get<AbilityMeta>("ability")
        val casterPlayer = context.get<SinglePlayerSelector>("caster").single()
        val caster = casterPlayer
        val target = context.getOrNull<SingleEntitySelector>("target")?.single() ?: caster
        AbilityCastUtils.castPoint(ability, caster, target)
    }

}