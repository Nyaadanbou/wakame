package cc.mewcraft.wakame.item.schema

import cc.mewcraft.wakame.item.schema.meta.SchemaItemMeta
import kotlin.reflect.KClass

sealed class NekoItemValidator {
    companion object {
        fun chain(vararg initializers: NekoItemValidator): NekoItemValidator {
            initializers.reduce { acc, initializer ->
                initializer.also { acc.next = it }
            }
            return initializers.first()
        }
    }

    protected abstract val item: NekoItem

    protected var next: NekoItemValidator? = null

    abstract fun init(): Result<Unit>

    protected fun initNext(): Result<Unit> {
        return next?.init() ?: Result.success(Unit)
    }
}

/**
 * Validate the required data by behaviors
 */
class BehaviorValidator(
    override val item: NekoItem,
) : NekoItemValidator() {
    override fun init(): Result<Unit> {
        val itemMetaMap = item.meta
        val missingMetaTypes = mutableListOf<KClass<out SchemaItemMeta<*>>>()
        for (behavior in item.behaviors) {
            val requiredMetaTypes = behavior.requiredMetaTypes
            val missingMetaTypes = requiredMetaTypes.filter { itemMetaMap[it.java]?.isEmpty == true }
            if (missingMetaTypes.isNotEmpty()) {
                "The behavior ${behavior::class.qualifiedName} requires the meta"
                return Result.failure(
                    IllegalArgumentException(
                        "Can't find metas ${requiredMetaTypes.map { it.simpleName }} being required by the behavior ${behavior::class.qualifiedName} in the item ${item.key}"
                    )
                )
            }
        }
        return initNext()
    }
}