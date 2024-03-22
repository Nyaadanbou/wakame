package cc.mewcraft.wakame.pack.generate

import net.kyori.adventure.key.Key
import org.bukkit.Material
import team.unnamed.creative.model.ItemOverride
import team.unnamed.creative.model.ItemPredicate

data class ItemModelData(
    /**
     * 目标物品模型的 key。
     */
    val key: Key,

    /**
     * 表示此次生成的是该物品模型的第几个模型。
     *
     * 例如，如果是弓的第二个模型，那么这个值就是 1。
     * 生成出来的 ItemOverride 应该会是:
     * ```json
     *     {
     *         "predicate": {
     *             "pulling": 1,
     *             "custom_model_data": 10001
     *         },
     *         "model": "wakame:item/bow/demo"
     *     }
     * ```
     */
    val index: Int,

    /**
     * 目标物品模型的材质。
     */
    val material: Material,

    /**
     * 此次生成的物品模型的自定义模型数据。
     */
    val customModelData: Int,
)

sealed interface ItemOverrideGenerator {
    val data: ItemModelData

    fun generate(): ItemOverride
}

internal class ItemOverrideGeneratorProxy(
    override val data: ItemModelData,
) : ItemOverrideGenerator {

    /* Generators */
    private val bowGenerator: BowItemOverrideGenerator by lazy { BowItemOverrideGenerator(this) }

    private val generator: ItemOverrideGenerator?
        get() = when (data.material) {
            Material.BOW -> bowGenerator
            else -> null // Default generator
        }

    override fun generate(): ItemOverride {
        if (generator != null)
            return generator!!.generate()
        // Default generator
        return ItemOverride.of(
            data.key,
            ItemPredicate.customModelData(data.customModelData)
        )
    }
}

private class BowItemOverrideGenerator(
    private val generator: ItemOverrideGenerator,
) : ItemOverrideGenerator {
    override val data: ItemModelData
        get() = generator.data

    override fun generate(): ItemOverride {
        val (key, index, _, customModelData) = data
        when (index) {
            0 -> return ItemOverride.of(
                key,
                ItemPredicate.customModelData(customModelData)
            )

            1 -> return ItemOverride.of(
                key,
                ItemPredicate.customModelData(customModelData),
                ItemPredicate.pulling()
            )

            2 -> return ItemOverride.of(
                key,
                ItemPredicate.customModelData(customModelData),
                ItemPredicate.pulling(),
                ItemPredicate.pull(0.65F)
            )

            3 -> return ItemOverride.of(
                key,
                ItemPredicate.customModelData(customModelData),
                ItemPredicate.pulling(),
                ItemPredicate.pull(0.9F)
            )

            else -> throw IllegalArgumentException("Bow model index out of range: $index")
        }
    }
}