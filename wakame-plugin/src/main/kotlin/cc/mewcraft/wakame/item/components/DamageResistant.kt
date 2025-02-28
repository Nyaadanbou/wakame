@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemDeprecations
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.registry.tag.TagKey
import net.kyori.examination.Examinable
import org.bukkit.damage.DamageType
import io.papermc.paper.datacomponent.item.DamageResistant as PaperDamageResistant


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
            val paperDamageResistant = holder.bukkitStack.getData(DataComponentTypes.DAMAGE_RESISTANT) ?: return null
            return DamageResistant(paperDamageResistant.types())
        }

        override fun write(holder: ItemComponentHolder, value: DamageResistant) {
            val paperDamageResistant = PaperDamageResistant.damageResistant(value.types)
            holder.bukkitStack.setData(DataComponentTypes.DAMAGE_RESISTANT, paperDamageResistant)
        }

        override fun remove(holder: ItemComponentHolder) {
            ItemDeprecations.usePaperOrNms()
        }
    }
}