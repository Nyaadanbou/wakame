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
import io.papermc.paper.event.player.PlayerArmSwingEvent
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import org.bukkit.GameMode
import org.bukkit.block.Block
import org.bukkit.block.BlockType
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
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

    // 记录了当前 tick 将要左键点击的玩家
    @JvmStatic
    private val willLeftClick: ReferenceOpenHashSet<Player> = ReferenceOpenHashSet()

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
        willLeftClick.clear()
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
            Action.LEFT_CLICK_AIR -> {
                // 该tick此玩家未触发过左键点击
                if (!haveLeftClicked.contains(player)) {
                    // 将此玩家标记为"将要左键"
                    // 目的是后续判定swing包是主手还是副手
                    willLeftClick.add(player)
                }
            }

            Action.LEFT_CLICK_BLOCK -> {
                // 该tick此玩家未触发过左键点击
                if (!haveLeftClicked.contains(player)) {
                    // 冒险模式下玩家左键方块是基于挥手包(swing)判定, 需要特殊处理
                    if (player.gameMode == GameMode.ADVENTURE) {
                        // 将此玩家标记为"将要左键"
                        // 目的是后续判定swing包是主手还是副手
                        willLeftClick.add(player)
                    } else {
                        PlayerItemLeftClickEvent(player, item, hand).callEvent()
                    }
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

            else -> {}
        }
    }

    @EventHandler
    fun on(event: PlayerArmSwingEvent) {
        val player = event.player
        // 只允许主手挥手包(swing)触发左键事件
        // 目的是修复服务端发送挥手动画时, 基于挥手包(swing)的左键事件会触发的问题
        if (willLeftClick.contains(player) && event.hand == EquipmentSlot.HAND) {
            haveLeftClicked.add(player)
            PlayerItemLeftClickEvent(player, player.inventory.itemInMainHand, EquipmentSlot.HAND).callEvent()
        }
    }

    private fun isRightClickInteractable(block: Block?): Boolean {
        val block = block ?: return false

        if (block.type.asBlockType() in RL_INTERACTABLE_BLOCK_TYPES) return true

        // TODO 引入家具系统后, 这里需要补充逻辑

        return false
    }

    @EventHandler
    fun on(event: PrePlayerAttackEntityEvent) {
        val player = event.player
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