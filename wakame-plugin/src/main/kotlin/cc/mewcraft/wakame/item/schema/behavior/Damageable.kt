package cc.mewcraft.wakame.item.schema.behavior

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.map
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.item.binary.NekoStackFactory
import cc.mewcraft.wakame.item.binary.meta.BDurabilityMeta
import cc.mewcraft.wakame.item.binary.meta.getAccessor
import cc.mewcraft.wakame.item.schema.NekoItem
import cc.mewcraft.wakame.provider.ConfigProvider
import cc.mewcraft.wakame.provider.optionalEntry
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger

/**
 * 可以受损的物品。
 */
interface Damageable : ItemBehavior {
    /**
     * 可以用于修复的材料类型。
     */
    val repairMaterials: List<Key>

    companion object Factory : ItemBehaviorFactory<Damageable> {
        override fun create(item: NekoItem, behaviorConfig: ConfigProvider): Damageable {
            val repairMaterials = behaviorConfig.optionalEntry<List<String>>("repair").orElse(emptyList())
            return Default(repairMaterials)
        }
    }

    /**
     * 默认实现。理论上还可以有其他实现。
     */
    private class Default(
        repairMaterialsProvider: Provider<List<String>>
    ) : Damageable, KoinComponent {
        private val logger: Logger by inject()
        override val repairMaterials: List<Key> by repairMaterialsProvider.map { it.map(Key::key) }

        override fun handleInteract(player: Player, itemStack: ItemStack, action: Action, wrappedEvent: PlayerInteractEvent) {
            val nekoStack = NekoStackFactory.wrap(itemStack)
            val durabilityMeta = nekoStack.meta.getAccessor<BDurabilityMeta>()
            if (durabilityMeta == null) {
                logger.warn("物品 ${nekoStack.schema.key} 拥有行为 Damageable, 但是没有耐久度元数据")
                return
            }
            player.sendMessage("当前耐久度：${durabilityMeta.damage()}/${durabilityMeta.threshold()}")
        }
    }
}