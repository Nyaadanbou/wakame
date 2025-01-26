package cc.mewcraft.wakame.hook.impl.mythicmobs.drop

import cc.mewcraft.wakame.api.Koish
import io.lumine.mythic.api.adapters.AbstractItemStack
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.drops.DropMetadata
import io.lumine.mythic.api.drops.IItemDrop
import io.lumine.mythic.bukkit.adapters.item.ItemComponentBukkitItemStack
import io.lumine.mythic.core.drops.droppables.ItemDrop
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import kotlin.jvm.optionals.getOrNull

class NekoItemDrop(
    config: MythicLineConfig,
    argument: String,
) : ItemDrop(argument, config), IItemDrop {

    private val itemKey: String = config.getString(arrayOf("type", "t", "i", "item"), "dirt", argument)

    override fun getDrop(data: DropMetadata, amount: Double): AbstractItemStack {
        val key = Key.key(itemKey)
        val player = data.cause.getOrNull()?.bukkitEntity as? Player

        val nekoItem = Koish.get().itemRegistry.get(key) // may throw exceptions, can MM handle it?
        val itemStack = nekoItem.createItemStack(player)

        return ItemComponentBukkitItemStack(itemStack).amount(amount.toInt())
    }
}