package cc.mewcraft.wakame.skill.condition

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import com.google.common.collect.TreeMultimap
import net.kyori.adventure.audience.Audience
import java.util.*
import kotlin.reflect.KClass

class SkillConditionResult(
    private val skillConditions: List<SkillCondition<*>>
) {
    private val conditions: MutableMap<Condition, FailureNotification> = HashMap()
    private val conditionPriority: Multimap<Condition.Priority, Condition> = TreeMultimap.create(Comparator.reverseOrder(), Comparator.naturalOrder())
    private val conditionSideEffects: MutableSet<ConditionSideEffect> = linkedSetOf()

    private val testFailedList: MutableList<Condition> = mutableListOf()

    fun test(): Boolean {
        conditions.forEach { (condition, _) ->
            if (!condition.check()) {
                testFailedList.add(condition)
            }
        }

        return testFailedList.isEmpty()
    }

    fun notifyFailure(audience: Audience, notifyCount: Int = 1) {
        if (notifyCount == 0)
            return
        val iterator = conditionPriority.values().iterator()

        var count = notifyCount
        while (iterator.hasNext() && count > 0) {
            val condition = iterator.next()
            if (testFailedList.contains(condition)) {
                checkNotNull(conditions[condition]) { "Condition $condition is not registered" }
                    .notify(audience)
                count--
            }
        }
    }

    // 执行所有条件所附带的副作用
    fun cost() {
        conditionSideEffects.forEach { it.apply() }
    }

    fun builder(): Builder {
        return Builder()
    }

    inner class Builder {
        private var condition = Condition { true }

        fun requireConditions(vararg requiredCondition: KClass<out SkillCondition<*>>): Builder {
            return createCondition { requiredCondition.all { clazz -> skillConditions.any { clazz.isInstance(it) } } }
        }

        fun createCondition(condition: Condition): Builder {
            this.condition += condition
            return this
        }

        fun next(): ConditionBuilder {
            return ConditionBuilder(condition)
        }
    }

    inner class ConditionBuilder(
        private val condition: Condition
    ) {
        private val conditions: MutableMap<Condition, FailureNotification> = hashMapOf()
        private val conditionPriority: Multimap<Condition.Priority, Condition> = HashMultimap.create()
        private val conditionSideEffects: MutableSet<ConditionSideEffect> = linkedSetOf()

        init {
            // Set default options
            conditions[condition] = FailureNotification { }
            conditionPriority.put(Condition.Priority.NORMAL, condition)
        }

        fun withFailureNotification(failureNotification: FailureNotification): ConditionBuilder {
            conditions[condition] = failureNotification
            return this
        }

        fun withPriority(priority: Condition.Priority): ConditionBuilder {
            conditionPriority.put(priority, condition)
            return this
        }

        fun addConditionSideEffect(conditionSideEffect: ConditionSideEffect): ConditionBuilder {
            conditionSideEffects.add(conditionSideEffect)
            return this
        }

        fun build() {
            this@SkillConditionResult.conditions += conditions
            this@SkillConditionResult.conditionSideEffects += conditionSideEffects
            this@SkillConditionResult.conditionPriority.putAll(conditionPriority)
        }

        fun buildAndNext(): Builder {
            build()
            return Builder()
        }
    }
}

fun interface Condition : Comparable<Condition> {
    fun check(): Boolean

    override fun compareTo(other: Condition): Int {
        return this.hashCode().compareTo(other.hashCode())
    }

    enum class Priority : Comparable<Priority> {
        LOW, NORMAL, HIGH;
    }
}

/**
 * Merge two conditions with logical AND
 *
 * @param other The other condition to merge
 * @return The merged condition
 */
fun Condition.merge(other: Condition): Condition {
    return Condition { this.check() && other.check() }
}

operator fun Condition.plus(other: Condition): Condition {
    return this.merge(other)
}

fun interface ConditionSideEffect {
    fun apply()
}

fun interface FailureNotification {
    fun notify(audience: Audience)
}