package cc.mewcraft.wakame.item.scheme.behavior

import cc.mewcraft.wakame.item.binary.NekoStackFactory
import cc.mewcraft.wakame.item.binary.meta.ItemMetaHolder
import cc.mewcraft.wakame.item.binary.meta.getOrCreate
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.item.scheme.meta.DefaultDurabilityMeta
import cc.mewcraft.wakame.item.scheme.meta.SchemeItemMeta
import cc.mewcraft.wakame.util.requireKt
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type
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

    class Default(
        override val repairMaterials: List<Key>,
        override val durabilityMeta: SDurabilityMeta,
    ) : Damageable {

        override fun generateAndSet(holder: ItemMetaHolder, context: SchemeGenerationContext) {
            val value = durabilityMeta.generate(context)
            if (value is SchemeItemMeta.Result.NonEmptyResult) {
                holder.getOrCreate<BDurabilityMeta>().set(value.value)
            }
        }

        override fun handleInteract(player: Player, itemStack: ItemStack, action: Action, wrappedEvent: PlayerInteractEvent) {
            val nekoStack = NekoStackFactory.wrap(itemStack)
            player.sendMessage("Seed: ${nekoStack.seed}")
        }
    }
}

private data object EmptyDamageable : Damageable {
    override val repairMaterials: List<Key> = emptyList()
    override val durabilityMeta: SDurabilityMeta = DefaultDurabilityMeta
}

internal class DamageableSerializer : ItemBehaviorSerializer<Damageable> {
    override val defaultValue: Damageable = EmptyDamageable
    override fun deserialize(type: Type, node: ConfigurationNode): Damageable {
        return Damageable.Default(
            repairMaterials = node.node("repair_materials").requireKt<List<String>>().map(Key::key),
            durabilityMeta = node.node("durability").requireKt<SDurabilityMeta>()
        )
    }
}