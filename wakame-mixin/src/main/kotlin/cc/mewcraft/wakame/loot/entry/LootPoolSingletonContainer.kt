package cc.mewcraft.wakame.loot.entry

import cc.mewcraft.wakame.loot.context.LootContext
import cc.mewcraft.wakame.loot.predicate.LootPredicate
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get

/**
 * 一个用于管理战利品池条目的容器，包含单一的战利品条目.
 *
 * 该类负责管理具有条件、权重和质量的战利品池条目. 它允许基于上下文和条件扩展战利品条目，
 * 并根据提供的幸运值修改权重来计算战利品条目的权重.
 *
 * @param S 生成的战利品数据的类型.
 * @param weight 战利品条目的基础权重.该值影响战利品条目被选中的概率.
 * @param quality 战利品条目的质量，用于通过幸运值来修改基础权重.
 * @param conditions 定义何时应用该战利品条目的条件的谓词列表.
 */
abstract class LootPoolSingletonContainer<S>(
    val weight: Int,
    val quality: Int,
    conditions: List<LootPredicate>,
) : LootPoolEntryContainer<S>(conditions) {

    companion object {
        protected fun commonFields(node: ConfigurationNode): Triple<Int, Int, List<LootPredicate>> {
            val weight = node.node("weight").get<Int>(0)
            val quality = node.node("quality").get<Int>(0)
            val conditions = node.node("conditions").get<List<LootPredicate>>() ?: emptyList()
            return Triple(weight, quality, conditions)
        }
    }

    @Transient
    private val entry: LootPoolEntry<S> = object : EntryBase() {
        override fun createData(context: LootContext, dataConsumer: (S) -> Unit) {
            this@LootPoolSingletonContainer.createData(context, dataConsumer)
        }
    }

    protected abstract fun createData(context: LootContext, dataConsumer: (S) -> Unit)

    final override fun expand(context: LootContext, dataConsumer: (LootPoolEntry<S>) -> Unit): Boolean {
        if (this.canRun(context)) {
            dataConsumer.invoke(this.entry)
            return true
        } else {
            return false
        }
    }

    protected abstract inner class EntryBase : LootPoolEntry<S> {
        override fun getWeight(luck: Float): Int {
            val qualityModifier = this@LootPoolSingletonContainer.quality.toFloat() * luck
            val baseWeight = (this@LootPoolSingletonContainer.weight + qualityModifier).toInt()
            return baseWeight
        }
    }
}