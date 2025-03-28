package cc.mewcraft.wakame.entity.player

import cc.mewcraft.wakame.event.bukkit.PlayerItemLeftClickEvent
import cc.mewcraft.wakame.event.bukkit.PlayerItemRightClickEvent
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.item.takeUnlessEmpty
import cc.mewcraft.wakame.util.registerEvents
import com.destroystokyo.paper.event.server.ServerTickStartEvent
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import xyz.xenondevs.commons.collections.enumSetOf

/**
 * 本 object 负责实现:
 * - [cc.mewcraft.wakame.event.bukkit.PlayerItemLeftClickEvent]
 * - [cc.mewcraft.wakame.event.bukkit.PlayerItemRightClickEvent]
 */
@Init(stage = InitStage.POST_WORLD)
internal object ItemClickEventSupport : Listener {

    @InitFun
    fun init() {
        registerEvents()
    }

    // 记录了当前 tick 左键点击过的玩家
    @JvmStatic
    private val haveLeftClicked: ReferenceOpenHashSet<Player> = ReferenceOpenHashSet()

    // 记录了当前 tick 右键点击过的玩家
    @JvmStatic
    private val haveRightClicked: ReferenceOpenHashSet<Player> = ReferenceOpenHashSet()

    // 可以右键交互的方块类型
    @JvmStatic
    private val RL_INTERACTABLE_BLOCK_TYPES: Set<Material> = enumSetOf(
        Material.CHEST,
        Material.TRAPPED_CHEST,
        Material.ENDER_CHEST,
        Material.SHULKER_BOX,
        Material.WHITE_SHULKER_BOX,
        Material.ORANGE_SHULKER_BOX,
        Material.MAGENTA_SHULKER_BOX,
        Material.LIGHT_BLUE_SHULKER_BOX,
        Material.YELLOW_SHULKER_BOX,
        Material.LIME_SHULKER_BOX,
    )

    @EventHandler
    fun on(event: ServerTickStartEvent) {
        haveLeftClicked.clear()
        haveRightClicked.clear()
    }

    @EventHandler
    fun on(event: PlayerInteractEvent) {
        val player = event.player
        val item = event.item ?: return
        val itemType = item.type // 优化: 调用 ItemStack#type 需要查询, 这里只计算一次
        when {
            event.action.isLeftClick -> {
                if (haveLeftClicked.add(player)) {
                    PlayerItemLeftClickEvent(player, item).callEvent()
                }
            }

            event.action == Action.RIGHT_CLICK_AIR -> {
                if (haveRightClicked.add(player)) {
                    PlayerItemRightClickEvent(player, item, event.hand!!).callEvent()
                }
            }

            event.action == Action.RIGHT_CLICK_BLOCK -> {
                if (haveRightClicked.add(player)) {
                    val clickedBlock = event.clickedBlock
                    if (clickedBlock != null && clickedBlock.type in RL_INTERACTABLE_BLOCK_TYPES) {
                        return
                    }
                    PlayerItemRightClickEvent(player, item, event.hand!!).callEvent()
                }
            }

            else -> {} // Action = PHYSICAL
        }
    }

    // 注意: 手持物品左键 Boat/Minecart 不会触发 EntityDamageEvent
    @EventHandler
    fun on(event: EntityDamageEvent) {
        val player = event.damageSource.directEntity as? Player ?: return
        val itemInMainHand = player.inventory.itemInMainHand.takeUnlessEmpty() ?: return
        if (haveLeftClicked.add(player)) {
            PlayerItemLeftClickEvent(player, itemInMainHand).callEvent()
        }
    }

    // 经讨论, 对于本身存在交互的操作(如右键船,矿车,盔甲架)不触发事件
    //@EventHandler
    //fun on(event: PlayerInteractAtEntityEvent) {
    //    val player = event.player
    //    val hand = event.hand
    //    val item = player.inventory.getItem(hand).takeUnlessEmpty() ?: return
    //    val rightClicked = event.rightClicked
    //    if (rightClicked is Boat ||
    //        rightClicked is Minecart ||
    //        rightClicked is ArmorStand
    //    ) {
    //        // 仅处理 Boat, Minecart, ArmorStand
    //        PlayerItemRightClickEvent(player, item, hand).callEvent()
    //    }
    //}

}