package cc.mewcraft.wakame.mixin.support

import cc.mewcraft.wakame.player.equipment.ArmorChangeEvent
import net.minecraft.core.NonNullList
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import org.bukkit.inventory.EquipmentSlot

class WatchedArmorList(
    player: Player,
) : NonNullList<ItemStack>(
    Array(4) { ItemStack.EMPTY }.asList(),
    ItemStack.EMPTY
) {
    @JvmField
    var initialized = false
    private val serverPlayer: ServerPlayer? = player as? ServerPlayer
    private val previousStacks: Array<ItemStack> = Array(4) { ItemStack.EMPTY }

    override fun set(index: Int, element: ItemStack): ItemStack {
        if (initialized) {
            if (serverPlayer != null) {
                val previous = previousStacks[index]
                // if (ItemStack.matches(previous, element)) {
                //     return element
                // }

                @Suppress("SENSELESS_COMPARISON")
                if (serverPlayer.connection != null) {
                    // 玩家初次进入游戏时, ServerPlayer#connection 可能还没来得及赋值
                    val action = when {
                        previous.isEmpty && !element.isEmpty -> ArmorChangeEvent.Action.EQUIP
                        !previous.isEmpty && element.isEmpty -> ArmorChangeEvent.Action.UNEQUIP
                        else -> ArmorChangeEvent.Action.CHANGE
                    }
                    val event = ArmorChangeEvent(
                        serverPlayer.bukkitEntity,
                        EquipmentSlot.entries[index + 2],
                        action,
                        previous.asBukkitMirror(),
                        element.asBukkitMirror()
                    )
                    if (!event.callEvent()) {
                        // 如果事件被取消, 返回尝试 set 的物品
                        return element
                    }
                }
            }
        } else if (index == 3) {
            // 当玩家第一次加入时, 玩家的物品栏是从 nbt 加载的, 而 slot 3 是最后初始化的
            initialized = true
        }

        previousStacks[index] = element.copy()
        return super.set(index, element)
    }

    override fun add(element: ItemStack?): Boolean {
        throw UnsupportedOperationException("cannot add element to the armor list")
    }
}