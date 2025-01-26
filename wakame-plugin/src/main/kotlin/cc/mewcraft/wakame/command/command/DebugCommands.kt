package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.command.CommandConstants
import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.buildAndAdd
import cc.mewcraft.wakame.command.suspendingHandler
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.pack.entity.ModelRegistry
import cc.mewcraft.wakame.pack.entity.OnGroundBoneModifier
import cc.mewcraft.wakame.util.coroutine.minecraft
import cc.mewcraft.wakame.util.takeUnlessEmpty
import cc.mewcraft.wakame.util.unsafeNekooTagOrNull
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import org.bukkit.command.CommandSender
import org.bukkit.entity.Pig
import org.bukkit.entity.Player
import org.incendo.cloud.Command
import org.incendo.cloud.CommandFactory
import org.incendo.cloud.CommandManager
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.extension.commandBuilder
import org.incendo.cloud.parser.standard.IntegerParser
import team.unnamed.hephaestus.Model
import team.unnamed.hephaestus.bukkit.BukkitModelEngine


object DebugCommands : CommandFactory<CommandSender> {
    private const val DEBUG_LITERAL = "debug"

    private fun String.prettifyJson(): String {
        val json = JsonParser.parseString(this).getAsJsonObject()
        val gson = GsonBuilder().setPrettyPrinting().create()
        val prettyJson = gson.toJson(json)
        return prettyJson
    }

    override fun createCommands(commandManager: CommandManager<CommandSender>): List<Command<out CommandSender>> = buildList {
        // /<root> debug print_wakame_nbt_if_not_null
        commandManager.commandBuilder(
            name = CommandConstants.ROOT_COMMAND,
            description = Description.of("Inspects the `wakame` NBT tag on the item held in main hand")
        ) {
            senderType<Player>()
            permission(CommandPermissions.DEBUG)
            literal(DEBUG_LITERAL)
            literal("inspect_wakame")
            //<editor-fold desc="handler: print_wakame_nbt_if_not_null">
            handler { context ->
                val sender = context.sender() as Player
                val nbtOrNull = sender.inventory.itemInMainHand.unsafeNekooTagOrNull
                sender.sendPlainMessage("NBT: " + nbtOrNull?.asString()?.prettifyJson())
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
            literal("summon_entity")
            //<editor-fold desc="handler: spawn_model_engine_entity">
            suspendingHandler(context = Dispatchers.minecraft) { context ->
                val sender = context.sender() as Player

                fun spawn(player: Player, model: Model) {
                    val engine = Injector.get<BukkitModelEngine>()

                    // Spawn base entity
                    val pig = player.world.spawn(player.location, Pig::class.java)
                    // Make the pig invisible
                    pig.isInvisible = true
                    // Create the model view on the pig
                    val view = engine.spawn(model, pig)
                    // Make the model bones be on the ground
                    OnGroundBoneModifier(pig).apply(view)
                    // Save the created view so it's animated
                    ModelRegistry.view(view)

                    player.sendPlainMessage("Summoned " + model.name())
                }

                val model = ModelRegistry.models().first()
                spawn(sender, model)
            }
            //</editor-fold>
        }
    }
}