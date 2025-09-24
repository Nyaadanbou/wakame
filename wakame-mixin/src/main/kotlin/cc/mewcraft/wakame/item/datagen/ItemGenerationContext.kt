package cc.mewcraft.wakame.item.datagen

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.entity.attribute.bundle.AttributeContextData
import cc.mewcraft.wakame.item.KoishItem
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.loot.context.LootContext
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.registry.entry.RegistryEntry
import kotlin.random.Random

/**
 * 从 [cc.mewcraft.wakame.item.KoishItem] 生成一个物品堆叠时的上下文.
 *
 * 该实例的生命周期仅存在于物品生成时. 当一个物品生成完成后该实例便不应该再使用.
 *
 * @property koishItem 物品类型
 */
data class ItemGenerationContext(
    val koishItem: KoishItem,
    override val luck: Float,
    override var level: Int = 0,
) : LootContext {
    override val random: Random = Random.Default
    override var selectEverything: Boolean = false

    var rarity: RegistryEntry<Rarity> = BuiltInRegistries.RARITY.getDefaultEntry()
    val attributes: MutableList<AttributeContextData> = mutableListOf()
    val elements: MutableSet<RegistryEntry<Element>> = mutableSetOf()
    val kizami: MutableSet<RegistryEntry<Kizami>> = mutableSetOf()
}