package cc.mewcraft.wakame.test

import cc.mewcraft.wakame.event.NekoReloadEvent
import cc.mewcraft.wakame.item.binary.NekoItemStackFactory
import cc.mewcraft.wakame.item.binary.meta.DisplayLoreMeta
import cc.mewcraft.wakame.item.binary.meta.get
import cc.mewcraft.wakame.registry.NekoItemRegistry
import cc.mewcraft.wakame.util.*
import io.papermc.paper.event.player.AsyncChatEvent
import me.lucko.helper.nbt.ShadowTagType
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import me.lucko.helper.shadows.nbt.IntShadowTag
import me.lucko.helper.shadows.nbt.ListShadowTag
import me.lucko.helper.shadows.nbt.ShortShadowTag
import me.lucko.helper.text3.mini
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.examination.string.StringExaminer
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import java.util.UUID

class TestListener : Listener {
    @EventHandler
    fun testItemGeneration(e: AsyncChatEvent) {
        val player = e.player
        val plainMessage = e.message().let { PlainTextComponentSerializer.plainText().serialize(it) }
        val inventory = player.inventory

        when (plainMessage) {
            "i1" -> {
                val nekoItem = NekoItemRegistry.getOrThrow("short_sword:demo")
                val nekoItemStack = nekoItem.createItemStack(player)
                inventory.addItem(nekoItemStack.handle)
            }
        }
    }

    @EventHandler
    fun testItemGeneration2(e: AsyncChatEvent) {
        val player = e.player
        val plainMessage = e.message().let { PlainTextComponentSerializer.plainText().serialize(it) }
        val inventory = player.inventory

        when {
            plainMessage.startsWith("i-") -> {
                val nekoItem = NekoItemRegistry.getOrThrow(plainMessage.substringAfter("i-"))
                val nekoItemStack = nekoItem.createItemStack(player)
                inventory.addItem(nekoItemStack.handle)
            }
        }
    }

    @EventHandler
    fun testItemRead(e: AsyncChatEvent) {
        val player = e.player
        val plainMessage = e.message().let { PlainTextComponentSerializer.plainText().serialize(it) }
        val inventory = player.inventory

        when (plainMessage) {
            "r1" -> {
                val wrap = NekoItemStackFactory.wrap(inventory.itemInMainHand)
                val lore = wrap.metadata.get<DisplayLoreMeta, _>().orEmpty()
                val preview = ItemStack(Material.STONE).apply { editMeta { it.lore(lore.mini) } }
                inventory.addItem(preview)
            }
        }
    }

    @EventHandler
    fun testShadowNbt(e: AsyncChatEvent) {
        val player = e.player
        val plainMessage = e.message().let { PlainTextComponentSerializer.plainText().serialize(it) }
        val inventory = player.inventory

        when (plainMessage) {
            "s1" -> {
                val nbt = inventory.itemInMainHand.nekoCompound
                println("wakameCompound: " + nbt.asString())
            }

            "s2" -> {
                val nbtOrNull = inventory.itemInMainHand.nekoCompoundOrNull
                println("wakameCompoundOrNull: " + nbtOrNull?.asString())
            }

            "s3" -> {
                inventory.itemInMainHand.nekoCompound = compoundShadowTag {
                    putString("ns", "short_sword")
                    putString("id", "demo")
                    putByte("sid", 0)
                }
            }

            "s4" -> {
                val bukkitStack = ItemStack(Material.NETHERITE_SWORD)

                // write
                bukkitStack.nekoCompound = compoundShadowTag {
                    putString("ns", "short_sword")
                    putString("id", "demo")
                    putByte("sid", 18)
                    putShort("short", Short.MAX_VALUE)
                    putInt("int", Int.MAX_VALUE)
                    putLong("long", Long.MAX_VALUE)
                    putUUID("uuid", UUID(5, 5))
                    putFloat("float", Float.MAX_VALUE)
                    putDouble("double", Double.MAX_VALUE)
                    putByteArray("byte_array", byteArrayOf(1, 2, 3))
                    putIntArray("int_array", intArrayOf(1, 2, 3))
                    putLongArray("long_array", longArrayOf(1, 2, 3))
                    putBoolean("boolean", true)

                    val listShadowTag1: ListShadowTag = listShadowTag(
                        IntShadowTag.valueOf(1),
                    )
                    put("list1", listShadowTag1)

                    val listShadowTag2 = listShadowTag {
                        add(ShortShadowTag.valueOf(1))
                        add(ShortShadowTag.valueOf(2))
                        add(ShortShadowTag.valueOf(3))
                    }
                    put("list2", listShadowTag2)

                    val compoundShadowTag: CompoundShadowTag = compoundShadowTag {
                        putByte("k1", 31)
                    }
                    put("stats", compoundShadowTag)
                }

                // read
                bukkitStack.nekoCompound.run {
                    check(getString("ns") == "short_sword")
                    check(getString("id") == "demo")
                    check(getByte("sid") == 18.toByte())
                    check(getShort("short") == Short.MAX_VALUE)
                    check(getInt("int") == Int.MAX_VALUE)
                    check(getLong("long") == Long.MAX_VALUE)
                    if (hasUUID("uuid")) check(getUUID("uuid") == UUID(5, 5))
                    check(getFloat("float") == Float.MAX_VALUE)
                    check(getDouble("double") == Double.MAX_VALUE)
                    check(getByteArray("byte_array").last() == 3.toByte())
                    check(getIntArray("int_array").last() == 3)
                    check(getLongArray("long_array").last() == 3L)
                    check(getBoolean("boolean"))

                    val intList = getList("list1", ShadowTagType.INT)
                    val shortList = getList("list2", ShadowTagType.SHORT)

                    check(intList.getInt(0) == 1)
                    check(shortList.getShort(2) == 3.toShort())

                    val intShadowTag = intList[0]
                    check((intShadowTag as IntShadowTag).intValue() == 1)

                    val shortShadowTag = shortList[2]
                    check((shortShadowTag as ShortShadowTag).intValue() == 3)

                    check(getCompound("stats").getByte("k1") == 31.toByte())
                }

                // add to inv
                inventory.addItem(bukkitStack)
            }

            "s5" -> {
                val nbtOrNull = inventory.itemInMainHand.nekoCompoundOrNull
                nbtOrNull?.run {
                    putInt("in_place", 233)
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

    @EventHandler
    fun testPluginReload(e: AsyncChatEvent) {
        val player = e.player
        val plainMessage = e.message().let { PlainTextComponentSerializer.plainText().serialize(it) }
        if (plainMessage == "reload") {
            NekoReloadEvent().callEvent()
        }
    }
}
