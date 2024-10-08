package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.editMeta
import net.kyori.examination.Examinable
import org.bukkit.inventory.meta.Damageable as CraftDamageable

interface ItemMaxDamage : Examinable {

    companion object : ItemComponentBridge<Int> {
        override fun codec(id: String): ItemComponentType<Int> {
            return Codec(id)
        }

        override fun templateType(id: String): ItemTemplateType<Nothing> {
            throw UnsupportedOperationException()
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<Int> {
        override fun read(holder: ItemComponentHolder): Int? {
            return (holder.item.itemMeta as? CraftDamageable)?.takeIf { it.hasMaxDamage() }?.maxDamage
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