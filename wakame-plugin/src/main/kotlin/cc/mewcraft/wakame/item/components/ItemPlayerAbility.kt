package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.ability.PlayerAbility
import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.util.Identifiers
import java.util.*

data class ItemPlayerAbility(
    val abilities: List<PlayerAbility>,
) {

    companion object : ItemComponentBridge<ItemPlayerAbility> {

        private val config: ItemComponentConfig = ItemComponentConfig.provide(ItemConstants.PLAYER_ABILITY)

        override fun codec(id: String): ItemComponentType<ItemPlayerAbility> {
            return Codec(id)
        }

    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<ItemPlayerAbility> {
        override fun read(holder: ItemComponentHolder): ItemPlayerAbility? {
            val tag = holder.getNbt() ?: return null
            val abilities = ArrayList<PlayerAbility>(tag.size())
            for (stringId in tag.allKeys) {
                val id = Identifiers.of(stringId)
                val ability = PlayerAbility(id, tag)
                abilities += ability
            }
            return ItemPlayerAbility(abilities)
        }

        override fun write(holder: ItemComponentHolder, value: ItemPlayerAbility) {
            holder.editNbt { tag ->
                val abilities = value.abilities
                for (ability in abilities) {
                    val id = ability.id.toString()
                    val nbt = ability.saveNbt()
                    tag.put(id, nbt)
                }
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeNbt()
        }
    }

}
