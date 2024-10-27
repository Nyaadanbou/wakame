package cc.mewcraft.wakame.util

import io.papermc.paper.adventure.PaperAdventure
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.kyori.adventure.text.event.HoverEvent
import net.minecraft.core.Holder
import org.bukkit.craftbukkit.inventory.CraftItemType
import net.minecraft.world.item.ItemStack as MojangStack
import org.bukkit.inventory.ItemStack as BukkitStack

fun HoverEvent.ShowItem.toItemStack(): BukkitStack {
    val type = this.item()
    val itemType = RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM)[type] ?: error("Item $type not found")
    itemType as CraftItemType<*>
    val dataComponents = this.dataComponents()
    val dataComponentPatch = PaperAdventure.asVanilla(dataComponents)
    val mojangStack = MojangStack(Holder.direct(itemType.handle), this.count(), dataComponentPatch)
    return mojangStack.asBukkitCopy()
}