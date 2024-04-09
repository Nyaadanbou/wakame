package cc.mewcraft.wakame.test

import cc.mewcraft.wakame.event.NekoReloadEvent
import cc.mewcraft.wakame.item.binary.PlayNekoStackFactory
import cc.mewcraft.wakame.item.binary.getMetaAccessor
import cc.mewcraft.wakame.item.binary.meta.BDisplayLoreMeta
import cc.mewcraft.wakame.item.binary.meta.getOrEmpty
import cc.mewcraft.wakame.item.schema.PaperNekoItemRealizer
import cc.mewcraft.wakame.pack.ResourcePackManager
import cc.mewcraft.wakame.pack.model.ModelRegistry
import cc.mewcraft.wakame.pack.model.OnGroundBoneModifier
import cc.mewcraft.wakame.registry.NekoItemRegistry
import cc.mewcraft.wakame.registry.NekoItemRegistry.get
import cc.mewcraft.wakame.user.asNekoUser
import cc.mewcraft.wakame.util.*
import io.papermc.paper.event.player.AsyncChatEvent
import me.lucko.helper.Schedulers
import me.lucko.helper.nbt.ShadowTagType
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import me.lucko.helper.shadows.nbt.IntShadowTag
import me.lucko.helper.shadows.nbt.ListShadowTag
import me.lucko.helper.shadows.nbt.ShortShadowTag
import me.lucko.helper.text3.mini
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.examination.string.StringExaminer
import org.bukkit.Material
import org.bukkit.entity.Pig
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import team.unnamed.hephaestus.Model
import team.unnamed.hephaestus.bukkit.BukkitModelEngine
import team.unnamed.hephaestus.bukkit.ModelView
import java.util.UUID


class TestListener : KoinComponent, Listener {
    @EventHandler
    fun testItemGeneration(e: AsyncChatEvent) {
        val player = e.player
        val plainMessage = e.message().let { PlainTextComponentSerializer.plainText().serialize(it) }
        val inventory = player.inventory

        when (plainMessage) {
            "i1" -> {
                val nekoItem = NekoItemRegistry.INSTANCES.get("short_sword:demo")
                val nekoStack = PaperNekoItemRealizer.realize(nekoItem, player.asNekoUser())
                inventory.addItem(nekoStack.itemStack)
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
                val nekoItem = NekoItemRegistry.INSTANCES.get(plainMessage.substringAfter("i-"))
                val nekoStack = PaperNekoItemRealizer.realize(nekoItem, player.asNekoUser())
                inventory.addItem(nekoStack.itemStack)
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
                val wrap = PlayNekoStackFactory.require(inventory.itemInMainHand)
                val lore = wrap.getMetaAccessor<BDisplayLoreMeta>().getOrEmpty()
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
                inventory.itemInMainHand.nekoCompound = CompoundShadowTag {
                    putString("ns", "short_sword")
                    putString("id", "demo")
                    putByte("variant", 0)
                }
            }

            "s4" -> {
                val bukkitStack = ItemStack(Material.NETHERITE_SWORD)

                // write
                bukkitStack.nekoCompound = CompoundShadowTag {
                    putString("ns", "short_sword")
                    putString("id", "demo")
                    putByte("variant", 18)
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

                    val listShadowTag1: ListShadowTag = ListShadowTag(
                        IntShadowTag.valueOf(1),
                    )
                    put("list1", listShadowTag1)

                    val listShadowTag2 = ListShadowTag {
                        add(ShortShadowTag.valueOf(1))
                        add(ShortShadowTag.valueOf(2))
                        add(ShortShadowTag.valueOf(3))
                    }
                    put("list2", listShadowTag2)

                    val compoundShadowTag: CompoundShadowTag = CompoundShadowTag {
                        putByte("k1", 31)
                    }
                    put("stats", compoundShadowTag)
                }

                // read
                bukkitStack.nekoCompound.run {
                    check(getString("ns") == "short_sword")
                    check(getString("id") == "demo")
                    check(getByte("variant") == 18.toByte())
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
                    put("wakame", CompoundBinaryTag {
                        putString("ns", "short_sword")
                        putString("id", "demo")
                        putByte("variant", 0)
                    })
                }
            }

            "a4" -> {
                inventory.itemInMainHand.copyWriteNbt {
                    put("wakame", CompoundBinaryTag {
                        putString("ns", "long_sword")
                        putString("id", "demo")
                        putByte("variant", 0)
                    })
                }.apply {
                    inventory.addItem(this)
                }
            }
        }
    }

    @EventHandler
    fun testVariantChange(e: AsyncChatEvent) {
        val player = e.player
        val plainMessage = e.message().let { PlainTextComponentSerializer.plainText().serialize(it) }
        val inventory = player.inventory

        if (plainMessage.startsWith("v-")) {
            val nekoStack = PlayNekoStackFactory.require(inventory.itemInMainHand)
            val variant = plainMessage.substringAfter("v-").toInt()
            nekoStack.putVariant(variant)
            inventory.setItemInMainHand(nekoStack.itemStack)
        }
    }

    private val engine: BukkitModelEngine by inject()

    @EventHandler
    fun testModel(e: AsyncChatEvent) {
        val player = e.player
        val plainMessage = e.message().let { PlainTextComponentSerializer.plainText().serialize(it) }

        if (plainMessage == "m1") {
            val model = ModelRegistry.models().first()
            Schedulers.sync().run {
                spawn(player, model)
            }
        }
    }

    private fun spawn(player: Player, model: Model) {
        // Spawn base entity
        val pig: Pig = player.world.spawn(player.location, Pig::class.java)
        pig.isInvisible = true
        // Create the model view on the pig
        val view: ModelView = engine.spawn(model, pig)
        // Make the model bones be on the ground
        OnGroundBoneModifier(pig).apply(view)
        // Save the created view so it's animated
        ModelRegistry.view(view)

        player.sendPlainMessage("Summoned " + model.name())
    }

    @EventHandler
    fun testPluginReload(e: AsyncChatEvent) {
        val player = e.player
        val plainMessage = e.message().let { PlainTextComponentSerializer.plainText().serialize(it) }
        if (plainMessage == "reload") {
            NekoReloadEvent().callEvent()
        }
    }

    private val resourcePackManager: ResourcePackManager by inject()

    @EventHandler
    fun testResourcePackReGenerate(e: AsyncChatEvent) {
        val player = e.player
        val plainMessage = e.message().let { PlainTextComponentSerializer.plainText().serialize(it) }
        if (plainMessage == "regenpack") {
            resourcePackManager.generate(reGenerate = true)
                .onSuccess { player.sendPlainMessage("Resource pack has been re-generated") }
                .onFailure { player.sendPlainMessage("Failed to re-generate resource pack: $it") }
        }
    }
}
