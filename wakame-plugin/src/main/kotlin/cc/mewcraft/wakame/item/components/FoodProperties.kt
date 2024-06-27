package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

// TODO 完成组件: FoodProperties

interface FoodProperties : Examinable {

    val nutrition: Int
    val saturation: Float
    val canAlwaysEat: Boolean
    val eatSeconds: Float
    val usingConvertsTo: ItemStack
    val effects: FoodEffect
    val skills: List<Key>

    data class FoodEffect(
        val potionEffect: PotionEffect,
        val probability: Float,
    )

    /* data */ class Value(
        override val nutrition: Int,
        override val saturation: Float,
        override val canAlwaysEat: Boolean,
        override val eatSeconds: Float,
        override val usingConvertsTo: ItemStack,
        override val effects: FoodEffect,
        override val skills: List<Key>,
    ) : FoodProperties {

    }

    data class Codec(
        override val id: String,
    ) : ItemComponentType<FoodProperties, ItemComponentHolder.Complex> {
        override val holder: ItemComponentType.Holder = ItemComponentType.Holder.COMPLEX

        override fun read(holder: ItemComponentHolder.Complex): FoodProperties? {
            // TODO 要先从两个地方分别获取数据, 然后组成一个完整的食物组件信息
            //  具体来说, 先从 ItemStack 获取原版的食物组件信息
            //  再从 NBT 获取食物吃完后释放的技能
            return null
        }

        override fun write(holder: ItemComponentHolder.Complex, value: FoodProperties) {
            // TODO 要将数据分别写入两个地方, 一个是 ItemStack 上的原版物品组件, 一个是 NBT
        }

        override fun remove(holder: ItemComponentHolder.Complex) {
            // TODO 要将数据分别从两个地方移除: ItemStack 和 NBT
        }
    }

    /* data */ class Template : ItemTemplate<FoodProperties> {
        override fun generate(context: GenerationContext): GenerationResult<FoodProperties> {
            TODO("Not yet implemented")
        }

        companion object : ItemTemplateType<Template> {
            override fun deserialize(type: Type, node: ConfigurationNode): Template {
                TODO("Not yet implemented")
            }
        }
    }
}