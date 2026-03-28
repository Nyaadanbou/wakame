package cc.mewcraft.bettergui.action

import cc.mewcraft.wakame.integration.virtualstorage.VirtualStorageIntegration
import me.hsgamer.bettergui.builder.ActionBuilder
import me.hsgamer.bettergui.util.SchedulerUtil
import me.hsgamer.hscore.action.common.Action
import me.hsgamer.hscore.common.StringReplacer
import me.hsgamer.hscore.task.element.TaskProcess
import org.bukkit.Bukkit
import java.util.*


/**
 * 为玩家打开指定 id 的 vault.
 *
 * 格式:
 *
 * ```yaml
 * open-vault: <vault_id>
 * ```
 */
class OpenVault : Action {

    private val vaultId: String

    constructor(input: ActionBuilder.Input) {
        this.vaultId = input.value
    }

    override fun apply(uuid: UUID, process: TaskProcess, stringReplacer: StringReplacer) {
        val player = Bukkit.getPlayer(uuid) ?: run {
            process.next()
            return
        }
        val vaultId2 = stringReplacer.replaceOrOriginal(vaultId, uuid).toIntOrNull() ?: run {
            player.sendRichMessage("<red>Invalid vault id: $vaultId")
            process.next()
            return
        }
        SchedulerUtil.entity(player).run({
            try {
                VirtualStorageIntegration.openVault(player, vaultId2)
            } finally {
                process.next()
            }
        }, process::next)
    }
}