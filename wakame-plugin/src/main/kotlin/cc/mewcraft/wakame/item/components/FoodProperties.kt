package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.component.GenerationContext
import cc.mewcraft.wakame.item.component.GenerationResult
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.template.ItemTemplate
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

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

    class Codec(
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

    class Template : ItemTemplate<FoodProperties> {
        override fun generate(context: GenerationContext): GenerationResult<FoodProperties> {
            TODO("Not yet implemented")
        }

        companion object : ItemTemplate.Serializer<Template> {
            override fun deserialize(type: Type, node: ConfigurationNode): Template {
                TODO("Not yet implemented")
            }
        }
    }
}