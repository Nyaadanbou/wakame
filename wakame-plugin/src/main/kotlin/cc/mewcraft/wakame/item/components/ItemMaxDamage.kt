package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.util.editMeta
import net.kyori.examination.Examinable
import org.bukkit.inventory.meta.Damageable as CraftDamageable

interface ItemMaxDamage : Examinable {

    data class Codec(
        override val id: String,
    ) : ItemComponentType<Int> {
        override fun read(holder: ItemComponentHolder): Int? {
            return (holder.item.itemMeta as? Damageable)?.maxDamage ?: return null
        }

        override fun write(holder: ItemComponentHolder, value: Int) {
            holder.item.editMeta<CraftDamageable> { itemMeta ->
                itemMeta.setMaxDamage(value)
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.editMeta<CraftDamageable> { itemMeta ->
                itemMeta.setMaxDamage(null)
            }
        }
    }
}