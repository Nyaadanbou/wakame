package cc.mewcraft.wakame.enchantment2.system

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.ecs.component.BukkitObject
import cc.mewcraft.wakame.ecs.component.BukkitPlayerComponent
import cc.mewcraft.wakame.ecs.system.ListenableIteratingSystem
import cc.mewcraft.wakame.enchantment2.component.AutoMelting
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.inventory.ItemStack

/**
 * @see cc.mewcraft.wakame.enchantment2.effect.EnchantmentAutoMeltingEffect
 */
object EnchantmentAutoMeltingSystem : ListenableIteratingSystem(
    family = World.family { all(BukkitObject, BukkitPlayerComponent, AutoMelting) }
) {

    override fun onTickEntity(entity: Entity) {
        // 无操作: 自动熔炼暂时不需要持续 tick 的逻辑
    }

    @EventHandler
    fun on(event: BlockDropItemEvent) {
        val player = event.player
        val playerEntity = player.koishify().unwrap()
        val autoMelting = playerEntity.getOrNull(AutoMelting) ?: return
        if (!autoMelting.activated) return

        event.items.forEach(::getBurned)

        LOGGER.info("BlockDropItemEvent passed to AutoMeltingSystem")
    }

    // FIXME #365: 获取真实的烧炼后的物品
    private fun getBurned(item: Item): ItemStack {
        val stack = item.itemStack
        val dummyLore = ItemLore.lore(listOf(Component.text("Melted")))
        stack.setData(DataComponentTypes.LORE, dummyLore)
        return stack
    }

}