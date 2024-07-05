package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.component.ItemComponent
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.editMeta
import net.kyori.examination.Examinable
import org.bukkit.inventory.meta.Damageable as CraftDamageable

interface ItemDamage : Examinable, ItemComponent {

    companion object : ItemComponentBridge<Int> {
        override fun codec(id: String): ItemComponentType<Int> {
            return Codec(id)
        }

        override fun templateType(): ItemTemplateType<Nothing> {
            throw UnsupportedOperationException()
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<Int> {
        override fun read(holder: ItemComponentHolder): Int? {
            return (holder.item.itemMeta as? CraftDamageable)?.damage
        }

        override fun write(holder: ItemComponentHolder, value: Int) {
            holder.item.editMeta<CraftDamageable> { itemMeta ->
                itemMeta.damage = value
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.editMeta<CraftDamageable> { itemMeta ->
                itemMeta.setMaxDamage(null)
            }
        }
    }
}