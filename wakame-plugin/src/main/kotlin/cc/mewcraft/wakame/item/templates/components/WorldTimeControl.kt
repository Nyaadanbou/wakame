package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.util.require
import cc.mewcraft.wakame.util.typeTokenOf
import cc.mewcraft.wakame.world.TimeControl
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.ConfigurationNode

data class WorldTimeControl(
    val type: TimeControl.ActionType,
    val time: Long,
) : ItemTemplate<Nothing> {

    companion object : ItemTemplateBridge<WorldTimeControl> {
        override fun codec(id: String): ItemTemplateType<WorldTimeControl> {
            return Codec(id)
        }
    }

    init {
        require(time >= 0) { "time must be greater or equal to 0" }
    }

    override val componentType: ItemComponentType<Nothing> = ItemComponentTypes.EMPTY

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<Nothing> {
        return ItemGenerationResult.empty()
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<WorldTimeControl> {
        override val type: TypeToken<WorldTimeControl> = typeTokenOf()

        override fun decode(node: ConfigurationNode): WorldTimeControl {
            val type = node.node("type").require<TimeControl.ActionType>()
            val time = node.node("value").require<Long>()
            return WorldTimeControl(type, time)
        }
    }
}
