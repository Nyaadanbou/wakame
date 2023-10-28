package cc.mewcraft.wakame.test

import cc.mewcraft.wakame.util.compoundBinaryTag
import cc.mewcraft.wakame.util.readNbt
import cc.mewcraft.wakame.util.writeNbt
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.examination.string.StringExaminer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class TestListener : Listener {
    @EventHandler
    fun onTest(e: AsyncChatEvent) {
        val player = e.player
        val plainMessage = e.message().let { PlainTextComponentSerializer.plainText().serialize(it) }
        when (plainMessage) {
            "1" -> {
                val nbt = player.inventory.itemInMainHand.readNbt()
                println(nbt?.examine(StringExaminer.simpleEscaping()))
            }

            "2" -> {
                player.inventory.itemInMainHand.writeNbt(compoundBinaryTag {
                    put("waka", compoundBinaryTag {
                        put("namespaced_id", compoundBinaryTag {
                            putString("namespace", "short_sword")
                            putString("id", "demo")
                        })
                    })
                }).apply {
                    player.inventory.addItem(this)
                }
            }
        }
    }
}