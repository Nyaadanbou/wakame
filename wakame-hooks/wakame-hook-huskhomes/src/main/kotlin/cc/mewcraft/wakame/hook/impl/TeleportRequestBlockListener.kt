package cc.mewcraft.wakame.hook.impl

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import net.kyori.adventure.text.Component
import net.william278.huskhomes.event.ReceiveTeleportRequestEvent
import net.william278.huskhomes.event.SendTeleportRequestEvent
import net.william278.huskhomes.teleport.TeleportRequest
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class TeleportRequestBlockListener : Listener {

    //
    // 允许玩家在 <server> 收到 TPA 请求
    // huskhomes.extra.tpa.recv.at.<server>
    //
    // 允许玩家在 <server> 收到 TPA_HERE 请求
    // huskhomes.extra.tpahere.recv.at.<server>
    //
    @EventHandler
    private fun on(event: ReceiveTeleportRequestEvent) {
        LOGGER.warn("Firing receive teleport request event")

        if (event.isIgnored) {
            return
        }

        val request = event.request
        val requestType = request.type

        val recipient = event.recipient
        val recipientPos = recipient.position
        val recipientServer = recipientPos.server

        when (requestType) {
            // TPA 是 requester 传送到 recipient
            TeleportRequest.Type.TPA -> {
                if (!recipient.hasPermission("huskhomes.extra.recv.tpa.at.$recipientServer")) {
                    event.isCancelled = true
                    recipient.audience.sendMessage(TranslatableMessages.MSG_RECV_TELEPORT_REQUEST_BLOCKED.arguments(Component.text(request.requesterName)))
                }
            }

            // TPA_HERE 是 recipient 传送到 requester
            TeleportRequest.Type.TPA_HERE -> {
                if (!recipient.hasPermission("huskhomes.extra.recv.tpahere.at.$recipientServer")) {
                    event.isCancelled = true
                    recipient.audience.sendMessage(TranslatableMessages.MSG_RECV_TELEPORT_REQUEST_BLOCKED.arguments(Component.text(request.requesterName)))
                }
            }
        }
    }

    //
    // 允许玩家从 <server> 发送 TPA 请求
    // huskhomes.extra.tpa.send.at.<server>
    //
    // 允许玩家从 <server> 发送 TPA_HERE 请求
    // huskhomes.extra.tpahere.send.at.<server>
    //
    @EventHandler
    private fun on(event: SendTeleportRequestEvent) {
        LOGGER.warn("Firing send teleport request event")

        val request = event.request
        val requestType = request.type

        val sender = event.sender
        val senderPos = sender.position
        val senderServer = senderPos.server

        when (requestType) {
            // TPA 是 sender 传送到 target
            TeleportRequest.Type.TPA -> {
                if (!sender.hasPermission("huskhomes.extra.send.tpa.at.$senderServer")) {
                    event.isCancelled = true
                    sender.audience.sendMessage(TranslatableMessages.MSG_SEND_TELEPORT_REQUEST_BLOCKED.arguments(Component.text(request.recipientName ?: "???")))
                }
            }

            // TPA_HERE 是 target 传送到 sender
            TeleportRequest.Type.TPA_HERE -> {
                if (!sender.hasPermission("huskhomes.extra.send.tpahere.at.$senderServer")) {
                    event.isCancelled = true
                    sender.audience.sendMessage(TranslatableMessages.MSG_SEND_TELEPORT_REQUEST_BLOCKED.arguments(Component.text(request.recipientName ?: "???")))
                }
            }
        }
    }

}