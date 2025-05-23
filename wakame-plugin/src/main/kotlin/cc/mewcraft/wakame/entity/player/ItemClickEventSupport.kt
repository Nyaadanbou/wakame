package cc.mewcraft.wakame.entity.player

import cc.mewcraft.wakame.config.MAIN_CONFIG
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.event.bukkit.PlayerItemLeftClickEvent
import cc.mewcraft.wakame.event.bukkit.PlayerItemRightClickEvent
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.item.takeUnlessEmpty
import cc.mewcraft.wakame.util.registerEvents
import com.destroystokyo.paper.event.server.ServerTickStartEvent
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import org.bukkit.block.Block
import org.bukkit.block.BlockType
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.vehicle.VehicleDamageEvent
import org.bukkit.inventory.EquipmentSlot
import xyz.xenondevs.commons.provider.map

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

    /**
     * 可以右键交互的方块类型.
     */
    @JvmStatic
    private val RL_INTERACTABLE_BLOCK_TYPES: HashSet<BlockType> by MAIN_CONFIG
        .entry<List<BlockType>>("right_click_interactable_blocks")
        .map { it.toHashSet() }

    @EventHandler
    fun on(event: ServerTickStartEvent) {
        haveLeftClicked.clear()
        haveRightClicked.clear()
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun on(event: PlayerInteractEvent) {
        val item = event.item?.takeUnlessEmpty() ?: return
        val action = event.action
        if (action == Action.PHYSICAL) return
        val hand = event.hand!!
        val player = event.player
        when (action) {
            Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK -> {
                if (haveLeftClicked.add(player)) {
                    PlayerItemLeftClickEvent(player, item, hand).callEvent()
                }
            }

            Action.RIGHT_CLICK_AIR -> {
                if (haveRightClicked.add(player)) {
                    PlayerItemRightClickEvent(player, item, hand).callEvent()
                }
            }

            Action.RIGHT_CLICK_BLOCK -> {
                if (haveRightClicked.add(player)) {
                    if (player.isSneaking || // c1
                        event.useInteractedBlock() == Event.Result.DENY || // c2
                        !isRightClickInteractable(event.clickedBlock) // c3
                    ) {
                        // 如果玩家是潜行右键点击 (c1) 或者方块右键交互已被其他代码显式禁用 (c2)
                        // 则认为物品可以无条件 right_click, 不再考虑可右键交互的方块类型 (c3)
                        PlayerItemRightClickEvent(player, item, hand).callEvent()
                    }
                }
            }

            else -> {} // Action = PHYSICAL
        }
    }

    private fun isRightClickInteractable(block: Block?): Boolean {
        val block = block ?: return false

        if (block.type.asBlockType() in RL_INTERACTABLE_BLOCK_TYPES) return true

        // TODO 引入家具系统后, 这里需要补充逻辑

        return false
    }

    @EventHandler
    fun on(event: EntityDamageEvent) {
        val player = event.damageSource.directEntity as? Player ?: return
        val itemInMainHand = player.inventory.itemInMainHand.takeUnlessEmpty() ?: return
        if (haveLeftClicked.add(player)) {
            PlayerItemLeftClickEvent(player, itemInMainHand, EquipmentSlot.HAND).callEvent()
        }
    }

    // 手持物品左键 Boat/Minecart 不会触发 EntityDamageEvent.
    // 需使用 VehicleDamageEvent 实现.
    @EventHandler
    fun on(event: VehicleDamageEvent) {
        val player = event.attacker as? Player ?: return
        val itemInMainHand = player.inventory.itemInMainHand.takeUnlessEmpty() ?: return
        if (haveLeftClicked.add(player)) {
            PlayerItemLeftClickEvent(player, itemInMainHand, EquipmentSlot.HAND).callEvent()
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