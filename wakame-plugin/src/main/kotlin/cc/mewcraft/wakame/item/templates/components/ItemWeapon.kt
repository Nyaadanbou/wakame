package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.util.register
import cc.mewcraft.wakame.util.require
import cc.mewcraft.wakame.util.typeTokenOf
import cc.mewcraft.wakame.weapon.WeaponType
import cc.mewcraft.wakame.weapon.WeaponTypeSerializer
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializerCollection

data class ItemWeapon(
    val weaponType: WeaponType,
) : ItemTemplate<Nothing> {
    override val componentType: ItemComponentType<Nothing> = ItemComponentTypes.EMPTY

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<Nothing> {
        return ItemGenerationResult.empty()
    }

    companion object : ItemTemplateBridge<ItemWeapon> {
        override fun codec(id: String): ItemTemplateType<ItemWeapon> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<ItemWeapon> {
        override val type: TypeToken<ItemWeapon> = typeTokenOf()

        override fun decode(node: ConfigurationNode): ItemWeapon {
            val weaponType = node.require<WeaponType>()
            return ItemWeapon(weaponType)
        }

        override fun childrenCodecs(): TypeSerializerCollection {
            return TypeSerializerCollection.builder()
                .register(WeaponTypeSerializer)
                .build()
        }
    }
}