package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.entity.player.AttackSpeedLevel
import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import net.kyori.examination.Examinable

data class ItemAttackSpeed(
    /**
     * 攻速等级.
     */
    val level: AttackSpeedLevel,
) : Examinable {

    companion object : ItemComponentBridge<ItemAttackSpeed> {
        /**
         * 该组件的配置文件.
         */
        private val config: ItemComponentConfig = ItemComponentConfig.provide(ItemConstants.ATTACK_SPEED)

        override fun codec(id: String): ItemComponentType<ItemAttackSpeed> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<ItemAttackSpeed> {
        override fun read(holder: ItemComponentHolder): ItemAttackSpeed? {
            val tag = holder.getNbt() ?: return null
            val level = tag.getByte(TAG_KEY)
            return ItemAttackSpeed(AttackSpeedLevel.entries[level.toInt()])
        }

        override fun write(holder: ItemComponentHolder, value: ItemAttackSpeed) {
            holder.editNbt { tag ->
                tag.putByte(TAG_KEY, value.level.ordinal.toByte())
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeNbt()
        }

        companion object {
            const val TAG_KEY = "level"
        }
    }
}