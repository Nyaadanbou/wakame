package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.ItemGenerationContext
import cc.mewcraft.wakame.item.template.ItemGenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateBridge
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.getList
import cc.mewcraft.wakame.item.components.FoodProperties as FoodPropertiesData


data class FoodProperties(
    val nutrition: Int,
    val saturation: Float,
    val canAlwaysEat: Boolean,
    val abilities: List<Key>,
) : ItemTemplate<FoodPropertiesData> {
    override val componentType: ItemComponentType<FoodPropertiesData> = ItemComponentTypes.FOOD

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<FoodPropertiesData> {
        val foodProperties = FoodPropertiesData(
            nutrition,
            saturation,
            canAlwaysEat,
            abilities
        )
        return ItemGenerationResult.of(foodProperties)
    }

    companion object : ItemTemplateBridge<FoodProperties> {
        override fun codec(id: String): ItemTemplateType<FoodProperties> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<FoodProperties> {
        override val type: TypeToken<FoodProperties> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>:
         *   nutrition: <int>
         *   saturation: <float>
         *   can_always_eat: <boolean>
         *   eat_seconds: <float>
         *   effects:
         *     - probability: <float>
         *       effect: <potion_effect>
         * ```
         */
        override fun decode(node: ConfigurationNode): FoodProperties {
            val nutrition = node.node("nutrition").getInt(0)
            val saturation = node.node("saturation").getFloat(0f)
            val canAlwaysEat = node.node("can_always_eat").getBoolean(false)
            val abilities = node.node("ability").getList<Key>(emptyList())
            return FoodProperties(nutrition, saturation, canAlwaysEat, abilities)
        }
    }
}