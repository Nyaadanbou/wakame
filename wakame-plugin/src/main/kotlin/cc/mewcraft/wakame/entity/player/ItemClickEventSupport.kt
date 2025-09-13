package cc.mewcraft.wakame.entity.player

import cc.mewcraft.wakame.event.bukkit.PlayerItemLeftClickEvent
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.item.takeUnlessEmpty
import com.destroystokyo.paper.event.server.ServerTickStartEvent
import io.papermc.paper.event.player.PlayerArmSwingEvent
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import org.bukkit.block.Block
import org.bukkit.block.BlockType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

/**
 * 本 object 负责实现:
 * - [cc.mewcraft.wakame.event.bukkit.PlayerItemLeftClickEvent]
 * - [cc.mewcraft.wakame.event.bukkit.PlayerItemRightClickEvent]
 */
@Init(stage = InitStage.POST_WORLD)
internal object ItemClickEventSupport : Listener {

    @InitFun
    fun init() {
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
    private val RL_INTERACTABLE_BLOCK_TYPES: HashSet<BlockType> = HashSet()

    @EventHandler
    fun on(event: ServerTickStartEvent) {
        willLeftClick.clear()
        haveLeftClicked.clear()
        haveRightClicked.clear()
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun on(event: PlayerInteractEvent) {
    }

    @EventHandler
    fun on(event: PlayerArmSwingEvent) {
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