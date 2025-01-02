@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.DamageResistant as PaperDamageResistant
import net.kyori.examination.Examinable
import org.bukkit.damage.DamageType
import io.papermc.paper.registry.tag.TagKey


data class DamageResistant(
    val types: TagKey<DamageType>,
) : Examinable {
    companion object : ItemComponentBridge<DamageResistant> {
        override fun codec(id: String): ItemComponentType<DamageResistant> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<DamageResistant> {

        // 2024/12/3 开发日记 小米
        // 先试试使用 NMS 来读取物品信息
        // 2024/12/25 开发日记 g2213swo
        // Paper 已经有了 Item Component API, 可以直接使用

        override fun read(holder: ItemComponentHolder): DamageResistant? {
            return holder.item.getData(DataComponentTypes.DAMAGE_RESISTANT)?.types()?.let { DamageResistant(it) }
        }

        override fun write(holder: ItemComponentHolder, value: DamageResistant) {
            holder.item.setData(DataComponentTypes.DAMAGE_RESISTANT, PaperDamageResistant.damageResistant(value.types))
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.unsetData(DataComponentTypes.DAMAGE_RESISTANT)
        }
    }
}