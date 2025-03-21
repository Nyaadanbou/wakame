package cc.mewcraft.wakame.entity.player

import cc.mewcraft.wakame.event.bukkit.PlayerItemLeftClickEvent
import cc.mewcraft.wakame.event.bukkit.PlayerItemRightClickEvent
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.item.takeUnlessEmpty
import cc.mewcraft.wakame.util.registerEvents
import com.destroystokyo.paper.event.server.ServerTickStartEvent
import it.unimi.dsi.fastutil.objects.Reference2BooleanOpenHashMap
import org.bukkit.Tag
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Boat
import org.bukkit.entity.Minecart
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent

// FIXME #363: 随便写了点实现, 需要完善/修正这里的逻辑

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

    // FIXME #363: 手持物品左键 Boat/Minecart 不会触发 EntityDamageEvent
    @EventHandler
    fun on(event: EntityDamageEvent) {
        val player = event.damageSource.directEntity as? Player ?: return
        val itemInMainHand = player.inventory.itemInMainHand.takeUnlessEmpty() ?: return
        PlayerItemLeftClickEvent(player, itemInMainHand).callEvent()
    }

    // 记录了当前 tick 右键点击过 Boat 的玩家
    @JvmStatic
    private val haveRightClickedBoat: Reference2BooleanOpenHashMap<Player> = Reference2BooleanOpenHashMap<Player>().apply { defaultReturnValue(false) }

    @EventHandler
    fun on(event: ServerTickStartEvent) {
        haveRightClickedBoat.clear()
    }

    @EventHandler
    fun on(event: PlayerInteractEvent) {
        val player = event.player
        val item = event.item ?: return
        val itemType = item.type // 优化: 调用 ItemStack#type 需要查询, 这里只计算一次
        when {
            event.action.isLeftClick -> {
                PlayerItemLeftClickEvent(player, item).callEvent()
            }

            event.action.isRightClick -> {
                if (Tag.ITEMS_BOATS.isTagged(itemType)) {
                    if (haveRightClickedBoat.putIfAbsent(player, true))
                        return // 阻止第二次触发右键 Boat
                    PlayerItemRightClickEvent(player, item, event.hand!!).callEvent()
                } else {
                    PlayerItemRightClickEvent(player, item, event.hand!!).callEvent()
                }
            }

            else -> {} // Action = PHYSICAL
        }
    }

    @EventHandler
    fun on(event: PlayerInteractAtEntityEvent) {
        val player = event.player
        val hand = event.hand
        val item = player.inventory.getItem(hand).takeUnlessEmpty() ?: return
        val rightClicked = event.rightClicked
        if (rightClicked is Boat ||
            rightClicked is Minecart ||
            rightClicked is ArmorStand
        ) {
            // 仅处理 Boat, Minecart, ArmorStand
            PlayerItemRightClickEvent(player, item, hand).callEvent()
        }
    }

}