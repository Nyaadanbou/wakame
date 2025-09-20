package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.KoishCommandFactory
import cc.mewcraft.wakame.command.koishHandler
import cc.mewcraft.wakame.damage.KoishDamageSources
import cc.mewcraft.wakame.damage.PlayerDamageMetadata
import cc.mewcraft.wakame.damage.damageBundle
import cc.mewcraft.wakame.damage.hurt
import cc.mewcraft.wakame.entity.player.attributeContainer
import cc.mewcraft.wakame.item2.ItemRef
import cc.mewcraft.wakame.item2.data.ItemDataTypes
import cc.mewcraft.wakame.item2.getData
import cc.mewcraft.wakame.item2.isKoish
import cc.mewcraft.wakame.item2.setData
import cc.mewcraft.wakame.util.coroutine.minecraft
import cc.mewcraft.wakame.util.item.takeUnlessEmpty
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import org.bukkit.entity.LivingEntity
import org.incendo.cloud.bukkit.data.MultipleEntitySelector
import org.incendo.cloud.bukkit.parser.selector.MultipleEntitySelectorParser
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.kotlin.extension.getOrNull
import org.incendo.cloud.paper.util.sender.PlayerSource
import org.incendo.cloud.paper.util.sender.Source
import org.incendo.cloud.parser.standard.DoubleParser
import org.incendo.cloud.parser.standard.IntegerParser
import kotlin.math.pow

internal object DebugCommand : KoishCommandFactory<Source> {

    private val MINECRAFT_KILL_DAMAGE: Double = 2.0.pow(128)

    override fun KoishCommandFactory.Builder<Source>.createCommands() {
        val commonBuilder = build {
            senderType<PlayerSource>()
            permission(CommandPermissions.DEBUG)
            literal("debug")
        }

        // <root> debug change_variant <variant>
        // Changes the variant of the item held in main hand
        buildAndAdd(commonBuilder) {
            literal("change_variant")
            required("variant", IntegerParser.integerParser(0, 127))
            koishHandler(context = Dispatchers.minecraft, handler = ::handleChangeVariant)
        }

        // <root> debug damage <damage> [target]
        // 对指定目标实体 [target] 造成自定义伤害 <damage>
        buildAndAdd(commonBuilder) {
            literal("damage")
            required("damage", DoubleParser.doubleParser(.0, MINECRAFT_KILL_DAMAGE))
            optional("target", MultipleEntitySelectorParser.multipleEntitySelectorParser(false))
            koishHandler(context = Dispatchers.minecraft, handler = ::handleCustomDamage)
        }

        // <root> debug read_item_ref
        // 读取手持物品的 ItemRef
        buildAndAdd(commonBuilder) {
            literal("read_item_ref")
            koishHandler(context = Dispatchers.minecraft, handler = ::handleReadItemRef)
        }
    }

    private fun String.prettifyJson(): String {
        val json = JsonParser.parseString(this).asJsonObject
        val gson = GsonBuilder().setPrettyPrinting().create()
        val prettyJson = gson.toJson(json)
        return prettyJson
    }

    private fun handleChangeVariant(context: CommandContext<Source>) {
        val sender = (context.sender() as PlayerSource).source()
        val variant = context.get<Int>("variant")
        val itemInMainHand = sender.inventory.itemInMainHand.takeUnlessEmpty()
        if (itemInMainHand == null) {
            sender.sendPlainMessage("No item in your main hand")
            return
        }

        if (!itemInMainHand.isKoish) {
            sender.sendPlainMessage("Item in your main hand is not a Koish item")
            return
        }

        val oldVariant = itemInMainHand.getData(ItemDataTypes.VARIANT)
        itemInMainHand.setData(ItemDataTypes.VARIANT, variant)
        sender.sendPlainMessage("Variant has been changed from $oldVariant to $variant")
    }

    private fun handleCustomDamage(context: CommandContext<Source>) {
        val sender = (context.sender() as PlayerSource).source()
        val damage = context.get<Double>("damage")
        val target = context.getOrNull<MultipleEntitySelector>("target")?.values() ?: listOf(sender)
        target.filterIsInstance<LivingEntity>().forEach { entity ->
            val damageMetadata = PlayerDamageMetadata(
                attributes = sender.attributeContainer.getSnapshot(),
                damageBundle = damageBundle {
                    default {
                        min(damage)
                        max(damage)
                        rate(1.0)
                        defensePenetration(.0)
                        defensePenetrationRate(.0)
                    }
                }
            )
            entity.hurt(damageMetadata, KoishDamageSources.playerAttack(sender), true)
        }
    }

    private fun handleReadItemRef(context: CommandContext<Source>) {
        val sender = (context.sender() as PlayerSource).source()
        val itemInMainHand = sender.inventory.itemInMainHand.takeUnlessEmpty()
        if (itemInMainHand == null) {
            sender.sendPlainMessage("No item in your main hand")
            return
        }

        val itemRef = ItemRef.create(itemInMainHand)
        sender.sendPlainMessage("ItemRef: $itemRef")
    }

}