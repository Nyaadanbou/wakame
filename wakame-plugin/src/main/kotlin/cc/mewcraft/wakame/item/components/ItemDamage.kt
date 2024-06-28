package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.util.editMeta
import net.kyori.examination.Examinable
import org.bukkit.inventory.meta.Damageable as CraftDamageable

interface ItemDamage : Examinable {

    data class Codec(
        override val id: String,
    ) : ItemComponentType<Int, ItemComponentHolder.Item> {
        override val holder: ItemComponentType.Holder = ItemComponentType.Holder.ITEM

        override fun read(holder: ItemComponentHolder.Item): Int? {
            return (holder.item.itemMeta as? CraftDamageable)?.damage
        }

        override fun write(holder: ItemComponentHolder.Item, value: Int) {
            holder.item.editMeta<CraftDamageable> { itemMeta ->
                itemMeta.damage = value
            }
        }

        override fun remove(holder: ItemComponentHolder.Item) {
            holder.item.editMeta<CraftDamageable> { itemMeta ->
                itemMeta.setMaxDamage(null)
            }
        }
    }
}