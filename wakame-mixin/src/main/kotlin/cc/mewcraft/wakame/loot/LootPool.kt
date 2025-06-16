package cc.mewcraft.wakame.loot

import cc.mewcraft.wakame.loot.context.LootContext
import cc.mewcraft.wakame.loot.entry.ComposableEntryContainer
import cc.mewcraft.wakame.loot.entry.LootPoolEntry
import cc.mewcraft.wakame.loot.predicate.LootPredicate
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.require
import io.leangen.geantyref.TypeFactory
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

fun <S> LootPool(
    rolls: Int,
    conditions: List<LootPredicate>,
    entries: List<ComposableEntryContainer<S>>,
): LootPool<S> = SimpleLootPool(
    rolls = rolls,
    conditions = conditions,
    entries = entries
)

/**
 * [LootPool] 是一个包含了若干 [cc.mewcraft.wakame.loot.entry.ComposableEntryContainer] 的集合.
 */
interface LootPool<S> {

    companion object {
        val SERIALIZER: TypeSerializer2<LootPool<*>> = Serializer
    }

    /**
     * 抽取的次数.
     */
    val rolls: Int

    /**
     * 抽取这个 [LootPool] 的条件.
     */
    val conditions: List<LootPredicate>

    /**
     * 这个 [LootPool] 中的所有 [cc.mewcraft.wakame.loot.entry.ComposableEntryContainer].
     */
    val entries: List<ComposableEntryContainer<S>>

    /**
     * 选择 [LootPool] 中的样本.
     */
    fun select(context: LootContext): List<S>

    private object Serializer : TypeSerializer2<LootPool<*>> {
        override fun deserialize(type: Type, node: ConfigurationNode): LootPool<*> {
            val type = type as ParameterizedType
            val rolls = node.node("rolls").require<Int>()
            val conditions = node.node("conditions").require<List<LootPredicate>>()
            val entries = getEntries(node.node("entries"), type)
            return SimpleLootPool(
                rolls = rolls,
                conditions = conditions,
                entries = entries
            )
        }

        private fun getEntries(
            node: ConfigurationNode,
            entryType: ParameterizedType
        ): List<ComposableEntryContainer<Any>> {
            val sType = entryType.actualTypeArguments[0]
            val entryContainerType: Type = TypeFactory.parameterizedClass(ComposableEntryContainer::class.java, sType) // ComposableEntryContainer<S>
            val entriesType = TypeFactory.parameterizedClass(List::class.java, entryContainerType) // List<ComposableEntryContainer<S>>
            val typeToken = TypeToken.get(entriesType) as TypeToken<List<ComposableEntryContainer<Any>>>
            return node.require(typeToken) // 获取 entries 的类型为 List<ComposableEntryContainer<S>>
        }
    }
}

/* Implementations */

private data class SimpleLootPool<S>(
    override val rolls: Int,
    override val conditions: List<LootPredicate> = emptyList(),
    override val entries: List<ComposableEntryContainer<S>> = emptyList(),
) : LootPool<S> {
    override fun select(context: LootContext): List<S> {
        val results = mutableListOf<S>()
        this.addRandomItems(context) { data -> results.add(data) }
        return results
    }

    private fun addRandomItem(context: LootContext, dataConsumer: (S) -> Unit) {
        val random = context.random
        val entries = mutableListOf<LootPoolEntry<S>>()
        var totalWeight = 0

        // 遍历当前池的所有容器条目, 获取所有符合条件的条目并计算这些条目的总权重.
        for (lootPoolEntryContainer in this.entries) {
            lootPoolEntryContainer.expand(context) { entry: LootPoolEntry<S> ->
                // 计算 entry 的权重, 若其权重大于 0, 则记录 entry 和并累计权重.
                val weight = entry.getWeight(context.luck)
                if (weight > 0) {
                    entries.add(entry)
                    totalWeight += weight
                }
            }
        }

        val size = entries.size
        // 如果没有符合条件的 entry 或总权重为 0, 则直接返回
        if (totalWeight != 0 && size != 0) {
            if (size == 1) {
                // 如果只有一个 entry, 直接产出.
                entries.first().createData(context, dataConsumer)
            } else {
                // 如果有多个 entry, 根据总权重做加权随机:
                // 随机出一个范围在 [0, totalWeight) 的整数,
                // 遍历记录好的 entry, 每次用这个随机整数与 entry 的权重相减, 当随机整数为负数 (即 entry 的权重大于这个随机整数) 时抽取当前条目产出数据.
                var randomInt = random.nextInt(totalWeight)

                for (lootPoolEntry in entries) {
                    randomInt -= lootPoolEntry.getWeight(context.luck)
                    if (randomInt < 0) {
                        lootPoolEntry.createData(context, dataConsumer)
                        return
                    }
                }
            }
        }
    }

    fun addRandomItems(context: LootContext, dataConsumer: (S) -> Unit) {
        if (this.conditions.all { it.test(context) }) {
            repeat(rolls) { this.addRandomItem(context, dataConsumer) }
        }
    }
}