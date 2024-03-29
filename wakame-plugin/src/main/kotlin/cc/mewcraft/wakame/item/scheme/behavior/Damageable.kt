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
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.nova.data.config.entry
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

    companion object Impl : ItemBehaviorFactory<Damageable> {
        const val KEY = "damageable"

        override fun create(item: NekoItem): Damageable {
            val cfg = item.config
            val repairMaterials = cfg.entry<List<String>>("behaviors", KEY, "repair")
            val durabilityMeta = cfg.entry<SDurabilityMeta>("behaviors", KEY, "durability")
            return Default(repairMaterials, durabilityMeta)
        }
    }

    class Default(
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