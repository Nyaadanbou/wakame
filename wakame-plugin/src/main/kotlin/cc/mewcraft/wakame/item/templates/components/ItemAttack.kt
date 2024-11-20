package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.attack.AttackType
import cc.mewcraft.wakame.attack.AttackTypeSerializer
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.util.*
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializerCollection

data class ItemAttack(
    val attackType: AttackType,
) : ItemTemplate<Nothing>{
    override val componentType: ItemComponentType<Nothing> = ItemComponentTypes.EMPTY

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<Nothing> {
        return ItemGenerationResult.empty()
    }

    companion object : ItemTemplateBridge<ItemAttack> {
        override fun codec(id: String): ItemTemplateType<ItemAttack> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<ItemAttack> {
        override val type: TypeToken<ItemAttack> = typeTokenOf()

        override fun decode(node: ConfigurationNode): ItemAttack {
            val attackType = node.krequire<AttackType>()
            return ItemAttack(attackType)
        }

        override fun childrenCodecs(): TypeSerializerCollection {
            return TypeSerializerCollection.builder()
                .kregister(AttackTypeSerializer)
                .build()
        }
    }
}