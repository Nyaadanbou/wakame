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

// 自动熔炼: 如果目标方块可烧炼，挖掘后掉落烧炼后的物品。
/**
 * [cc.mewcraft.wakame.enchantment2.effect.EnchantmentAutoMeltingEffect] 的运行逻辑.
 * 根据玩家的 [AutoMelting] 来执行自动熔炼的逻辑.
 */
object AutoMeltingSystem : ListenableIteratingSystem(
    family = World.family { all(BukkitObject, BukkitPlayerComponent, AutoMelting) }
) {

    override fun onTickEntity(entity: Entity) {
        // 无操作: 自动熔炼暂时不需要持续 tick 的逻辑
    }

    // FIXME #365: 破坏方块时, 检查 fleks player entity 上有无 AutoMelting 的 component.
    //  如果有, 则根据 component 里的数据 (称之为 “魔咒运行时配置”) 来执行 AutoMelting 的效果.
    //  ---
    //  需要有一个独立的 system 来创建/移除 [魔咒运行时配置] (以下简称为 “魔咒配置”).
    //  对于我们的魔咒系统本身来说, 就是在玩家切换物品时创建相应的 [魔咒配置].
    //  ---
    //  [魔咒配置] 其实也可以被其他系统(比如技能)创建, 从而允许其他系统直接给予玩家魔咒效果,
    //  即使玩家没有装备任何有魔咒的物品.
    @EventHandler
    fun on(event: BlockDropItemEvent) {
        val player = event.player
        val playerEntity = player.koishify().unwrap()
        val autoMelting = playerEntity.getOrNull(AutoMelting) ?: return
        if (!autoMelting.activated) return

        event.items.forEach(::getBurned)

        LOGGER.info("BlockDropItemEvent passed to AutoMeltingSystem")
    }

    private fun getBurned(item: Item): ItemStack {
        val stack = item.itemStack
        val dummyLore = ItemLore.lore(listOf(Component.text("Melted")))
        stack.setData(DataComponentTypes.LORE, dummyLore)
        return stack
    }

}