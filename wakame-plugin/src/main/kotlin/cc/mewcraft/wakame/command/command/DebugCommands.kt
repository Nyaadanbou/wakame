package cc.mewcraft.wakame.command.command

import cc.mewcraft.nbt.IntTag
import cc.mewcraft.nbt.ShortTag
import cc.mewcraft.nbt.TagType
import cc.mewcraft.wakame.command.CommandConstants
import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.buildAndAdd
import cc.mewcraft.wakame.command.suspendingHandler
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.pack.model.ModelRegistry
import cc.mewcraft.wakame.pack.model.OnGroundBoneModifier
import cc.mewcraft.wakame.util.CompoundBinaryTag
import cc.mewcraft.wakame.util.CompoundTag
import cc.mewcraft.wakame.util.ListTag
import cc.mewcraft.wakame.util.ThreadType
import cc.mewcraft.wakame.util.adventureNbt
import cc.mewcraft.wakame.util.adventureNbtOrNull
import cc.mewcraft.wakame.util.copyWriteAdventureNbt
import cc.mewcraft.wakame.util.setAdventureNbt
import cc.mewcraft.wakame.util.takeUnlessEmpty
import cc.mewcraft.wakame.util.wakameTag
import cc.mewcraft.wakame.util.wakameTagOrNull
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Pig
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.incendo.cloud.Command
import org.incendo.cloud.CommandFactory
import org.incendo.cloud.CommandManager
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.extension.commandBuilder
import org.incendo.cloud.parser.standard.IntegerParser
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import team.unnamed.hephaestus.Model
import team.unnamed.hephaestus.bukkit.BukkitModelEngine
import java.util.UUID


object DebugCommands : KoinComponent, CommandFactory<CommandSender> {
    private const val DEBUG_LITERAL = "debug"

    private fun String.prettifyJson(): String {
        val json = JsonParser.parseString(this).getAsJsonObject()
        val gson = GsonBuilder().setPrettyPrinting().create()
        val prettyJson = gson.toJson(json)
        return prettyJson
    }

    override fun createCommands(commandManager: CommandManager<CommandSender>): List<Command<out CommandSender>> {
        return buildList {
            // /<root> debug print_wakame_nbt_if_not_null
            commandManager.commandBuilder(
                name = CommandConstants.ROOT_COMMAND,
                description = Description.of(
                    "Prints the `wakame` NBT tag on the item held in main hand; If the NBT tag does not exist, `null` will be printed instead"
                )
            ) {
                senderType<Player>()
                permission(CommandPermissions.DEBUG)
                literal(DEBUG_LITERAL)
                literal("print_wakame_nbt_if_not_null")
                //<editor-fold desc="handler: print_wakame_nbt_if_not_null">
                handler { context ->
                    val sender = context.sender() as Player
                    val nbtOrNull = sender.inventory.itemInMainHand.wakameTagOrNull
                    sender.sendPlainMessage("NBT: " + nbtOrNull?.asString()?.prettifyJson())
                }
                //</editor-fold>
            }.buildAndAdd(this)

            // /<root> debug print_wakame_nbt_or_create
            commandManager.commandBuilder(
                name = CommandConstants.ROOT_COMMAND,
                description = Description.of(
                    "Prints the `wakame` NBT tag on the item held in main hand; If the NBT tag does not exist, a new NBT will be saved to the item"
                )
            ) {
                senderType<Player>()
                permission(CommandPermissions.DEBUG)
                literal(DEBUG_LITERAL)
                literal("print_wakame_nbt_or_create")
                //<editor-fold desc="handler: print_wakame_nbt_or_create">
                handler { context ->
                    val sender = context.sender() as Player
                    val nbt = sender.inventory.itemInMainHand.wakameTag
                    sender.sendPlainMessage("NBT: " + nbt.asString().prettifyJson())
                }
                //</editor-fold>
            }.buildAndAdd(this)

            // /<root> debug test_shadow_nbt_invocation
            commandManager.commandBuilder(
                name = CommandConstants.ROOT_COMMAND,
                description = Description.of("Invokes the shadow NBT proxies and see if they run as expected")
            ) {
                permission(CommandPermissions.DEBUG)
                literal(DEBUG_LITERAL)
                literal("test_shadow_nbt_invocation")
                //<editor-fold desc="handler: test_shadow_nbt_invocation">
                handler { context ->
                    val bukkitStack = ItemStack(Material.NETHERITE_SWORD)

                    // test write operations
                    bukkitStack.wakameTag = CompoundTag {
                        putString("namespace", "short_sword")
                        putString("path", "demo")
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

                        val listShadowTag1 = ListTag(
                            IntTag.valueOf(1),
                        )
                        put("list1", listShadowTag1)

                        val listShadowTag2 = ListTag {
                            add(ShortTag.valueOf(1))
                            add(ShortTag.valueOf(2))
                            add(ShortTag.valueOf(3))
                        }
                        put("list2", listShadowTag2)

                        val compoundShadowTag = CompoundTag {
                            putByte("k1", 31)
                        }
                        put("stats", compoundShadowTag)
                    }

                    // test read operations (based on what have been written previously)
                    with(bukkitStack.wakameTag) {
                        check(getString("namespace") == "short_sword")
                        check(getString("path") == "demo")
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

                        val intList = getList("list1", TagType.INT)
                        val shortList = getList("list2", TagType.SHORT)

                        check(intList.getInt(0) == 1)
                        check(shortList.getShort(2) == 3.toShort())

                        val intShadowTag = intList[0]
                        check((intShadowTag as IntTag).intValue() == 1)

                        val shortShadowTag = shortList[2]
                        check((shortShadowTag as ShortTag).intValue() == 3)

                        check(getCompound("stats").getByte("k1") == 31.toByte())
                    }

                    val sender = context.sender()
                    if (sender is Player) {
                        // add to sender's inv if it's a player
                        sender.inventory.addItem(bukkitStack)
                    } else {
                        // otherwise drop it to the overworld spawn
                        val overworld = Bukkit.getWorlds().first()
                        val spawnLoc = overworld.spawnLocation
                        overworld.dropItem(spawnLoc, bukkitStack)
                    }
                }
                //</editor-fold>
            }.buildAndAdd(this)

            // /<root> debug test_adventure_nbt_invocation
            commandManager.commandBuilder(
                name = CommandConstants.ROOT_COMMAND,
                description = Description.of("Invokes the adventure NBT and see if they run as expected")
            ) {
                senderType<Player>()
                permission(CommandPermissions.DEBUG)
                literal(DEBUG_LITERAL)
                literal("test_adventure_nbt_invocation")
                required("case", IntegerParser.integerParser(1, 4))
                //<editor-fold desc="handler: test_adventure_nbt_invocation">
                handler { context ->
                    val sender = context.sender() as Player
                    val inventory = sender.inventory
                    val case = context.get<Int>("case")

                    when (case) {
                        1 -> {
                            val nbt = sender.inventory.itemInMainHand.adventureNbt
                            sender.sendPlainMessage("NBT: ${nbt.toString().prettifyJson()}")
                        }

                        2 -> {
                            val nbtOrNull = sender.inventory.itemInMainHand.adventureNbtOrNull
                            sender.sendPlainMessage("NBT: " + nbtOrNull?.toString()?.prettifyJson())
                        }

                        3 -> {
                            inventory.itemInMainHand.setAdventureNbt {
                                put("adventure", CompoundBinaryTag {
                                    putString("k1", "v1")
                                    putString("k2", "v2")
                                    putByte("k3", 3)
                                })
                            }
                        }

                        4 -> {
                            val item = inventory.itemInMainHand.copyWriteAdventureNbt {
                                put("adventure", CompoundBinaryTag {
                                    putString("k1", "v1")
                                    putString("k2", "v2")
                                    putByte("k3", 3)
                                })
                            }
                            inventory.addItem(item)
                        }
                    }
                }
                //</editor-fold>
            }.buildAndAdd(this)

            // /<root> debug change_variant <variant>
            commandManager.commandBuilder(
                name = CommandConstants.ROOT_COMMAND,
                description = Description.of("Changes the variant of held item")
            ) {
                senderType<Player>()
                permission(CommandPermissions.DEBUG)
                literal("change_variant")
                required("variant", IntegerParser.integerParser(0, 127))
                //<editor-fold desc="handler: change_variant">
                handler { context ->
                    val sender = context.sender() as Player
                    val variant = context.get<Int>("variant")
                    val itemInMainHand = sender.inventory.itemInMainHand.takeUnlessEmpty()
                    if (itemInMainHand == null) {
                        sender.sendPlainMessage("No item in your main hand")
                        return@handler
                    }

                    val nekoStack = itemInMainHand.tryNekoStack
                    if (nekoStack == null) {
                        sender.sendPlainMessage("Item is not a legal wakame item")
                        return@handler
                    }

                    val oldVariant = nekoStack.variant
                    nekoStack.variant = variant
                    sender.sendPlainMessage("Variant has been changed from $oldVariant to $variant")
                }
                //</editor-fold>
            }.buildAndAdd(this)

            // /<root> debug summon_model_engine_entity
            commandManager.commandBuilder(
                name = CommandConstants.ROOT_COMMAND,
                description = Description.of("Summons an entity from our model engine")
            ) {
                senderType<Player>()
                permission(CommandPermissions.DEBUG)
                literal("summon_model_engine_entity")
                //<editor-fold desc="handler: spawn_model_engine_entity">
                suspendingHandler { context ->
                    val sender = context.sender() as Player

                    fun spawn(player: Player, model: Model) {
                        val engine = get<BukkitModelEngine>()
                        // Spawn base entity
                        val pig = player.world.spawn(player.location, Pig::class.java)
                        pig.isInvisible = true
                        // Create the model view on the pig
                        val view = engine.spawn(model, pig)
                        // Make the model bones be on the ground
                        OnGroundBoneModifier(pig).apply(view)
                        // Save the created view so it's animated
                        ModelRegistry.view(view)

                        player.sendPlainMessage("Summoned " + model.name())
                    }

                    ThreadType.SYNC.switchContext {
                        val model = ModelRegistry.models().first()
                        spawn(sender, model)
                    }
                }
                //</editor-fold>
            }
        }
    }
}