package cc.mewcraft.wakame.test

import cc.mewcraft.wakame.util.*
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.examination.string.StringExaminer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class TestListener : Listener {
    @EventHandler
    fun testShadowNbt(e: AsyncChatEvent) {
        val player = e.player
        val plainMessage = e.message().let { PlainTextComponentSerializer.plainText().serialize(it) }
        val inventory = player.inventory

        when (plainMessage) {
            "s1" -> {
                val nbt = inventory.itemInMainHand.wakameCompound
                println("wakameCompound: " + nbt.asString())
            }

            "s2" -> {
                val nbtOrNull = inventory.itemInMainHand.wakameCompoundOrNull
                println("wakameCompoundOrNull: " + nbtOrNull?.asString())
            }

            "s3" -> {
                inventory.itemInMainHand.wakameCompound = compoundShadowTag {
                    putString("ns", "short_sword")
                    putString("id", "demo")
                    putByte("sid", 0)
                }
            }
        }
    }

    @EventHandler
    fun testAdventureNbt(e: AsyncChatEvent) {
        val player = e.player
        val plainMessage = e.message().let { PlainTextComponentSerializer.plainText().serialize(it) }
        val inventory = player.inventory

        when (plainMessage) {
            "a1" -> {
                val nbt = player.inventory.itemInMainHand.getNbt()
                println("nbt: " + nbt.examine(StringExaminer.simpleEscaping()))
            }

            "a2" -> {
                val nbtOrNull = player.inventory.itemInMainHand.getNbtOrNull()
                println("nbtOrNull: " + nbtOrNull?.examine(StringExaminer.simpleEscaping()))
            }

            "a3" -> {
                inventory.itemInMainHand.setNbt {
                    put("wakame", compoundBinaryTag {
                        putString("ns", "short_sword")
                        putString("id", "demo")
                        putByte("sid", 0)
                    })
                }
            }

            "a4" -> {
                inventory.itemInMainHand.copyWriteNbt {
                    put("wakame", compoundBinaryTag {
                        putString("ns", "long_sword")
                        putString("id", "demo")
                        putByte("sid", 0)
                    })
                }.apply {
                    inventory.addItem(this)
                }
            }
        }
    }
}
