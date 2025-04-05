package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.rarity2.LevelToRarityMapping
import cc.mewcraft.wakame.rarity2.Rarity
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.util.require
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import cc.mewcraft.wakame.item.components.ItemRarity as ItemRarityData

data class ItemRarity(
    /**
     * The default rarity held in this schema.
     */
    private val static: RegistryEntry<Rarity>? = null,
    /**
     * The mappings used to generate the rarity.
     */
    private val dynamic: RegistryEntry<LevelToRarityMapping>? = null,
) : ItemTemplate<ItemRarityData> {

    /**
     * 检查稀有度是不是固定的.
     */
    val isStatic: Boolean = static != null

    /**
     * 检查稀有度是不是动态的.
     */
    val isDynamic: Boolean = dynamic != null

    init {
        require(isStatic xor isDynamic) { "(static != null) xor (dynamic != null)" }
    }

    override val componentType: ItemComponentType<ItemRarityData> = ItemComponentTypes.RARITY

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<ItemRarityData> {
        fun warnNullItemLevel() {
            LOGGER.warn("Failed to generate ${ItemComponentTypes.RARITY} for item '${context.target}' because no ${ItemComponentTypes.LEVEL} was found in the generation context")
        }

        val rarity: RegistryEntry<Rarity>?

        when {
            // use static rarity
            static != null -> {
                rarity = static
            }

            // use dynamic rarity
            dynamic != null -> {
                val level = context.level
                if (level != null) {
                    rarity = dynamic.unwrap().pick(level.toInt(), context.random)
                } else {
                    rarity = null
                    warnNullItemLevel()
                }
            }

            // fallback to the global rarity mappings
            else -> {
                val level = context.level
                if (level != null) {
                    rarity = BuiltInRegistries.LEVEL_TO_RARITY_MAPPING.getDefaultEntry().unwrap().pick(level.toInt(), context.random)
                } else {
                    rarity = null
                    warnNullItemLevel()
                }
            }
        }

        // leave trace to the context
        context.rarity = rarity

        if (rarity != null) {
            return ItemGenerationResult.of(ItemRarityData(rarity = rarity))
        }

        return ItemGenerationResult.empty()
    }

    companion object : ItemTemplateBridge<ItemRarity> {
        override fun codec(id: String): ItemTemplateType<ItemRarity> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<ItemRarity> {
        private companion object {
            const val MAPPING_PREFIX = "mapping:"
            const val RARITY_PREFIX = "rarity:"
        }

        override val type: TypeToken<ItemRarity> = typeTokenOf()

        /**
         * ## Node structure 1
         * ```yaml
         * <node>: "mapping:_"
         * ```
         *
         * ## Node structure 2
         * ```yaml
         * <node>: "rarity:_"
         * ```
         */
        override fun decode(node: ConfigurationNode): ItemRarity {
            val string = node.require<String>()
            return when {
                string.startsWith(MAPPING_PREFIX) -> ItemRarity(
                    dynamic = BuiltInRegistries.LEVEL_TO_RARITY_MAPPING.createEntry(string.substringAfter(MAPPING_PREFIX)),
                )

                string.startsWith(RARITY_PREFIX) -> ItemRarity(
                    static = BuiltInRegistries.RARITY.createEntry(string.substringAfter(RARITY_PREFIX))
                )

                else -> throw SerializationException("Can't parse rarity value '$string'")
            }
        }
    }
}
