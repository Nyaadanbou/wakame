package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.*
import cc.mewcraft.wakame.util.editMeta
import net.kyori.examination.Examinable
import org.bukkit.inventory.meta.Damageable as CraftDamageable


data class Damageable(
    /**
     * 当前损耗.
     */
    val damage: Int,
    /**
     * 最大损耗.
     */
    val maxDamage: Int,
) : Examinable {

    companion object : ItemComponentBridge<Damageable> {
        /**
         * 该组件的配置文件.
         */
        private val config: ItemComponentConfig = ItemComponentConfig.provide(ItemConstants.DAMAGEABLE)

        override fun codec(id: String): ItemComponentType<Damageable> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<Damageable> {
        override fun read(holder: ItemComponentHolder): Damageable? {
            val itemMeta = (holder.item.itemMeta as? CraftDamageable)?.takeIf { it.hasMaxDamage() } ?: return null
            return Damageable(
                damage = itemMeta.damage,
                maxDamage = itemMeta.maxDamage,
            )
        }

        override fun write(holder: ItemComponentHolder, value: Damageable) {
            holder.item.editMeta<CraftDamageable> { itemMeta ->
                itemMeta.damage = value.damage
                itemMeta.setMaxDamage(value.maxDamage)
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.editMeta<CraftDamageable> { itemMeta ->
                itemMeta.setMaxDamage(null)
            }
        }
    }
}