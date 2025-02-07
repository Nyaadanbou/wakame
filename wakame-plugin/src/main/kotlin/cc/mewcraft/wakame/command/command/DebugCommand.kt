package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.KoishCommandFactory
import cc.mewcraft.wakame.command.koishHandler
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
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.parser.standard.IntegerParser
import team.unnamed.hephaestus.bukkit.BukkitModelEngine


internal object DebugCommand : KoishCommandFactory<CommandSender> {

    override fun KoishCommandFactory.Builder<CommandSender>.createCommands() {
        val commonBuilder = build {
            senderType<Player>()
            permission(CommandPermissions.DEBUG)
            literal("debug")
        }

        // <root> debug inspect_nbt
        // Inspects the Koish NBT tags on the item held in main hand
        buildAndAdd(commonBuilder) {
            literal("inspect_nbt")
            koishHandler(context = Dispatchers.minecraft, handler = ::handleInspectNbt)
        }

        // <root> debug change_variant <variant>
        // Changes the variant of the item held in main hand
        buildAndAdd(commonBuilder) {
            literal("change_variant")
            required("variant", IntegerParser.integerParser(0, 127))
            koishHandler(context = Dispatchers.minecraft, handler = ::handleChangeVariant)
        }

        // <root> debug summon_model_engine_entity
        // Summons an entity from our model engine
        buildAndAdd(commonBuilder) {
            literal("summon_entity")
            koishHandler(context = Dispatchers.minecraft, handler = ::handleSummonEntity)
        }
    }

    private fun String.prettifyJson(): String {
        val json = JsonParser.parseString(this).getAsJsonObject()
        val gson = GsonBuilder().setPrettyPrinting().create()
        val prettyJson = gson.toJson(json)
        return prettyJson
    }

    private fun handleInspectNbt(context: CommandContext<CommandSender>) {
        val sender = context.sender() as Player
        val nbtOrNull = sender.inventory.itemInMainHand.unsafeNekooTagOrNull
        sender.sendPlainMessage("NBT: " + nbtOrNull?.asString()?.prettifyJson())
    }

    private fun handleChangeVariant(context: CommandContext<CommandSender>) {
        val sender = context.sender() as Player
        val variant = context.get<Int>("variant")
        val itemInMainHand = sender.inventory.itemInMainHand.takeUnlessEmpty()
        if (itemInMainHand == null) {
            sender.sendPlainMessage("No item in your main hand")
            return
        }

        val nekoStack = itemInMainHand.tryNekoStack
        if (nekoStack == null) {
            sender.sendPlainMessage("Item is not a legal wakame item")
            return
        }

        val oldVariant = nekoStack.variant
        nekoStack.variant = variant
        sender.sendPlainMessage("Variant has been changed from $oldVariant to $variant")
    }

    private fun handleSummonEntity(context: CommandContext<CommandSender>) {
        val player = context.sender() as Player
        val model = ModelRegistry.models().first()

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
}