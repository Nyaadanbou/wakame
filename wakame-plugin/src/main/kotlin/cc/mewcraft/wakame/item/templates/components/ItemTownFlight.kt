package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.ItemGenerationContext
import cc.mewcraft.wakame.item.template.ItemGenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateBridge
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.ConfigurationNode

data class ItemTownFlight(
    val duration: Long, // 秒
    val rocketOnConsume: Boolean,
    val rocketForce: Double,
) : ItemTemplate<Nothing> {

    companion object : ItemTemplateBridge<ItemTownFlight> {
        override fun codec(id: String): ItemTemplateType<ItemTownFlight> {
            return Codec(id)
        }
    }

    init {
        require(duration > 0) { "duration (seconds) must be positive" }
    }

    override val componentType: ItemComponentType<Nothing> = ItemComponentTypes.EMPTY

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<Nothing> {
        return ItemGenerationResult.empty()
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<ItemTownFlight> {
        override val type: TypeToken<ItemTownFlight> = typeTokenOf()

        override fun decode(node: ConfigurationNode): ItemTownFlight {
            val duration = node.node("duration").krequire<Long>()
            val rocketOnConsume = node.node("rocket_on_consume").getBoolean(false)
            val rocketForce = node.node("rocket_force").krequire<Double>()
            return ItemTownFlight(duration, rocketOnConsume, rocketForce)
        }
    }
}
