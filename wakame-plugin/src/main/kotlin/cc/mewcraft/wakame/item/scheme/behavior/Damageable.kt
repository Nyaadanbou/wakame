package cc.mewcraft.wakame.item.scheme.behavior

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.map
import cc.mewcraft.wakame.item.binary.NekoStackFactory
import cc.mewcraft.wakame.item.binary.meta.ItemMetaHolder
import cc.mewcraft.wakame.item.binary.meta.getOrCreate
import cc.mewcraft.wakame.item.scheme.NekoItem
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.item.scheme.meta.DurabilityMeta
import cc.mewcraft.wakame.item.scheme.meta.GenerationResult
import cc.mewcraft.wakame.provider.ConfigProvider
import cc.mewcraft.wakame.provider.entry
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import cc.mewcraft.wakame.item.binary.meta.DurabilityMeta as BDurabilityMeta
import cc.mewcraft.wakame.item.scheme.meta.DurabilityMeta as SDurabilityMeta

/**
 * 可以受损的物品。
 */
interface Damageable : ItemBehavior {
    /**
     * 可以用于修复的材料类型。
     */
    val repairMaterials: List<Key>

    /**
     * 耐久度的设置。
     */
    val durabilityMeta: SDurabilityMeta

    companion object Factory : ItemBehaviorFactory<Damageable> {
        override fun create(item: NekoItem, config: ConfigProvider): Damageable {
            val repairMaterials = config.entry<List<String>>("repair")
            val durabilityMeta = config.entry<SDurabilityMeta>("durability")
            return Default(repairMaterials, durabilityMeta)
        }
    }

    /**
     * 默认实现。理论上还可以有其他实现。
     */
    private class Default(
        repairMaterialsProvider: Provider<List<String>>,
        durabilityMetaProvider: Provider<SDurabilityMeta>,
    ) : Damageable {

        override val repairMaterials: List<Key> by repairMaterialsProvider.map { it.map(Key::key) }
        override val durabilityMeta: DurabilityMeta by durabilityMetaProvider

        override fun generateAndSet(holder: ItemMetaHolder, context: SchemeGenerationContext) {
            val value = durabilityMeta.generate(context)
            if (value is GenerationResult.Thing) {
                holder.getOrCreate<BDurabilityMeta>().set(value.value)
            }
        }

        override fun handleInteract(player: Player, itemStack: ItemStack, action: Action, wrappedEvent: PlayerInteractEvent) {
            val nekoStack = NekoStackFactory.wrap(itemStack)
            player.sendMessage("Seed: ${nekoStack.seed}")
        }
    }
}