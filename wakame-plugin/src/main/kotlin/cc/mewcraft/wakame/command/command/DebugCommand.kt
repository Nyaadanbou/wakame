package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.KoishCommandFactory
import cc.mewcraft.wakame.command.koishHandler
import cc.mewcraft.wakame.damage.DamageTag
import cc.mewcraft.wakame.damage.DamageTags
import cc.mewcraft.wakame.damage.PlayerDamageMetadata
import cc.mewcraft.wakame.damage.damageBundle
import cc.mewcraft.wakame.damage.hurt
import cc.mewcraft.wakame.item.KoishStackImplementations
import cc.mewcraft.wakame.item.wrap
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.user.toUser
import cc.mewcraft.wakame.util.coroutine.minecraft
import cc.mewcraft.wakame.util.item.takeUnlessEmpty
import cc.mewcraft.wakame.util.item.toNMS
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.paper.util.sender.PlayerSource
import org.incendo.cloud.paper.util.sender.Source
import org.incendo.cloud.parser.standard.IntegerParser

internal object DebugCommand : KoishCommandFactory<Source> {

    override fun KoishCommandFactory.Builder<Source>.createCommands() {
        val commonBuilder = build {
            senderType<PlayerSource>()
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

        buildAndAdd(commonBuilder) {
            literal("neko_suicide")
            koishHandler(context = Dispatchers.minecraft, handler = ::handleNekoSuicide)
        }
    }

    private fun String.prettifyJson(): String {
        val json = JsonParser.parseString(this).asJsonObject
        val gson = GsonBuilder().setPrettyPrinting().create()
        val prettyJson = gson.toJson(json)
        return prettyJson
    }

    private fun handleInspectNbt(context: CommandContext<Source>) {
        val sender = (context.sender() as PlayerSource).source()
        val nbtOrNull = KoishStackImplementations.getNbt(sender.inventory.itemInMainHand.toNMS())
        sender.sendPlainMessage("NBT: " + nbtOrNull?.asString?.prettifyJson())
    }

    private fun handleChangeVariant(context: CommandContext<Source>) {
        val sender = (context.sender() as PlayerSource).source()
        val variant = context.get<Int>("variant")
        val itemInMainHand = sender.inventory.itemInMainHand.takeUnlessEmpty()
        if (itemInMainHand == null) {
            sender.sendPlainMessage("No item in your main hand")
            return
        }

        val nekoStack = itemInMainHand.wrap()
        if (nekoStack == null) {
            sender.sendPlainMessage("Item is not a legal wakame item")
            return
        }

        val oldVariant = nekoStack.variant
        nekoStack.variant = variant
        sender.sendPlainMessage("Variant has been changed from $oldVariant to $variant")
    }

    private fun handleNekoSuicide(context: CommandContext<Source>) {
        val sender = (context.sender() as PlayerSource).source()
        sender.hurt(
            PlayerDamageMetadata(
                sender.toUser(),
                damageBundle {
                    single(KoishRegistries.ELEMENT.getDefaultEntry()) {
                        min(114514.0)
                        max(114514.0)
                        rate(1.0)
                        defensePenetration(.0)
                        defensePenetrationRate(.0)
                    }
                },
                DamageTags(DamageTag.DIRECT)
            ), source = sender
        )
    }
}