package cc.mewcraft.wakame.test

import cc.mewcraft.wakame.util.*
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
                println("nbt: " + nbt.examine(StringExaminer.simpleEscaping()))
            }

            "2" -> {
                val nbtOrNull = player.inventory.itemInMainHand.readNbtOrNull()
                println("nbtOrNull: " + nbtOrNull?.examine(StringExaminer.simpleEscaping()))
            }

            "3" -> {
                player.inventory.itemInMainHand.modifyNbt {
                    put("wakame", compoundBinaryTag {
                        putString("namespace", "short_sword")
                        putString("id", "demo")
                    })
                }
            }

            "4" -> {
                player.inventory.itemInMainHand.copyWriteNbt {
                    put("wakame", compoundBinaryTag {
                        putString("namespace", "long_sword")
                        putString("id", "demo")
                    })
                }.apply {
                    player.inventory.addItem(this)
                }
            }
        }
    }
}