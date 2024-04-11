package cc.mewcraft.wakame.skill.condition

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import com.google.common.collect.TreeMultimap
import net.kyori.adventure.audience.Audience
import java.util.*
import kotlin.reflect.KClass

class SkillConditionResult(
    private val skillConditions: Collection<SkillCondition<*>>
) {
    private val conditions: MutableMap<Condition, FailureNotification> = HashMap()
    private val conditionSideEffects: MutableMap<KClass<out SkillCondition<*>>, ConditionSideEffect<*>> = HashMap()
    private val conditionPriority: Multimap<Condition.Priority, Condition> = TreeMultimap.create(Comparator.reverseOrder(), Comparator.naturalOrder())

    private val testFailedList: MutableSet<Condition> = hashSetOf()

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
                val notification = checkNotNull(conditions[condition]) { "Condition $condition is not registered" }
                if (notification == EmptyFailureNotification)
                    continue
                notification.notify(audience)
                count--
            }
        }
    }

    // 执行所有条件所附带的副作用
    fun cost() {
        for ((type, sideEffect) in conditionSideEffects) {
            val condition = skillConditions.firstOrNull { type.isInstance(it) } ?: continue
            @Suppress("UNCHECKED_CAST") (sideEffect as ConditionSideEffect<SkillCondition<*>>).apply(condition)
        }
    }

    fun builder(): Builder {
        return Builder()
    }

    private fun addCondition(condition: Condition, failureNotification: FailureNotification, priority: Condition.Priority) {
        conditions[condition] = failureNotification
        conditionPriority.put(priority, condition)
    }

    inner class Builder {
        private var condition: Condition = Condition.AlwaysTrue

        fun <T : SkillCondition<*>> typedConditions(
            requiredCondition: KClass<T>,
            block: T.() -> Boolean
        ): Builder {
            @Suppress("UNCHECKED_CAST") val skillCondition = skillConditions.firstOrNull { requiredCondition.isInstance(it) } as T?
            if (skillCondition == null) {
                addCondition(Condition.AlwaysFalse, EmptyFailureNotification, Condition.Priority.HIGH)
                return this
            } else {
                addCondition(Condition.AlwaysTrue, EmptyFailureNotification, Condition.Priority.LOW)
            }
            this.condition += Condition { skillCondition.block() }
            return this
        }

        inline fun <reified T : SkillCondition<*>> typedConditions(noinline block: T.() -> Boolean): Builder {
            return typedConditions(T::class, block)
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
        private val conditionSideEffects: MutableMap<KClass<out SkillCondition<*>>, ConditionSideEffect<*>> = hashMapOf()
        private val conditionPriority: Multimap<Condition.Priority, Condition> = HashMultimap.create()

        init {
            // Set default options
            addCondition(condition, EmptyFailureNotification, Condition.Priority.NORMAL)
        }

        fun withFailureNotification(failureNotification: FailureNotification): ConditionBuilder {
            conditions[condition] = failureNotification
            return this
        }

        fun withPriority(priority: Condition.Priority): ConditionBuilder {
            conditionPriority.put(priority, condition)
            return this
        }

        fun <T : SkillCondition<*>> addConditionSideEffect(clazz: KClass<T>, conditionSideEffect: ConditionSideEffect<T>): ConditionBuilder {
            conditionSideEffects[clazz] = conditionSideEffect
            return this
        }

        inline fun <reified T : SkillCondition<*>> addConditionSideEffect(conditionSideEffect: ConditionSideEffect<T>): ConditionBuilder {
            return addConditionSideEffect(T::class, conditionSideEffect)
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

    object AlwaysTrue : Condition {
        override fun check(): Boolean = true
    }

    object AlwaysFalse : Condition {
        override fun check(): Boolean = false
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

fun interface ConditionSideEffect<T : SkillCondition<*>> {
    fun apply(condition: T)
}

fun interface FailureNotification {
    fun notify(audience: Audience)
}

object EmptyFailureNotification : FailureNotification {
    override fun notify(audience: Audience) = Unit
}