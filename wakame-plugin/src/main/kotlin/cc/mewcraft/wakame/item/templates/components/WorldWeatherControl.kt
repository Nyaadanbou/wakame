package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.ItemGenerationContext
import cc.mewcraft.wakame.item.template.ItemGenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateBridge
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.typeTokenOf
import cc.mewcraft.wakame.world.WeatherControl
import cc.mewcraft.wakame.world.WeatherControlActionSerializer
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializerCollection

data class WorldWeatherControl(
    val actions: List<WeatherControl.Action>,
) : ItemTemplate<Nothing> {

    companion object : ItemTemplateBridge<WorldWeatherControl> {
        override fun codec(id: String): ItemTemplateType<WorldWeatherControl> {
            return Codec(id)
        }
    }

    override val componentType: ItemComponentType<Nothing> = ItemComponentTypes.EMPTY

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<Nothing> {
        return ItemGenerationResult.empty()
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<WorldWeatherControl> {
        override val type: TypeToken<WorldWeatherControl> = typeTokenOf()

        override fun decode(node: ConfigurationNode): WorldWeatherControl {
            val actions = node.krequire<List<WeatherControl.Action>>()
            return WorldWeatherControl(actions)
        }

        override fun childrenCodecs(): TypeSerializerCollection {
            return TypeSerializerCollection.builder().apply {
                kregister(WeatherControlActionSerializer)
            }.build()
        }
    }
}
