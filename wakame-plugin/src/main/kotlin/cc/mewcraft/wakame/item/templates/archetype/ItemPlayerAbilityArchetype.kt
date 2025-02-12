package cc.mewcraft.wakame.item.templates.archetype

import cc.mewcraft.wakame.ability.PlayerAbility
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.ItemPlayerAbility
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.require
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.ConfigurationNode

data class ItemPlayerAbilityArchetype(
    val abilities: List<PlayerAbility>,
) : ItemTemplate<ItemPlayerAbility> {

    override val componentType: ItemComponentType<ItemPlayerAbility> = ItemComponentTypes.PLAYER_ABILITY

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<ItemPlayerAbility> {
        return ItemGenerationResult.of(ItemPlayerAbility(abilities))
    }

    companion object : ItemTemplateBridge<ItemPlayerAbilityArchetype> {

        override fun codec(id: String): ItemTemplateType<ItemPlayerAbilityArchetype> {
            return Codec(id)
        }

    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<ItemPlayerAbilityArchetype> {

        override val type: TypeToken<ItemPlayerAbilityArchetype> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>:
         *   - id: <identifier>
         *     ...
         *   - id: <identifier>
         *     ...
         * ```
         */
        override fun decode(node: ConfigurationNode): ItemPlayerAbilityArchetype {
            val childrenList = node.childrenList()
            val abilityList = ArrayList<PlayerAbility>(childrenList.size)
            for (childNode in childrenList) {
                val id = childNode.node("id").require<Identifier>()
                val ability = PlayerAbility(id, childNode)
                abilityList += ability
            }
            return ItemPlayerAbilityArchetype(abilityList)
        }

    }
}
