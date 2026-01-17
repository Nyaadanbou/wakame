package cc.mewcraft.bettergui.action

import cc.mewcraft.wakame.feature.TeleportOnJoin
import me.hsgamer.bettergui.builder.ActionBuilder
import me.hsgamer.bettergui.util.SchedulerUtil
import me.hsgamer.hscore.action.common.Action
import me.hsgamer.hscore.common.StringReplacer
import me.hsgamer.hscore.task.element.TaskProcess
import org.bukkit.Bukkit
import java.util.*


/**
 * 请求加入服务器时进行传送.
 *
 * 格式:
 *
 * ```yaml
 * request-teleport-on-join: <key>,<group>
 * ```
 */
class RequestTeleportOnJoin : Action {

    private val key: String
    private val group: String

    constructor(input: ActionBuilder.Input) {
        val split = input.value.split(",", limit = 2)
        this.key = split[0]
        this.group = split[1]
    }

    override fun apply(uuid: UUID, process: TaskProcess, stringReplacer: StringReplacer) {
        val player = Bukkit.getPlayer(uuid) ?: run {
            process.next()
            return
        }
        SchedulerUtil.entity(player).run({
            TeleportOnJoin.request(uuid, this.key, this.group)
        }, process::next)
    }
}