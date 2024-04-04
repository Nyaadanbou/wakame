package cc.mewcraft.wakame.item.schema

sealed class NekoItemValidator {
    protected abstract val item: NekoItem

    companion object {
        fun chain(vararg initializers: NekoItemValidator): NekoItemValidator {
            initializers.reduce { acc, initializer ->
                acc.next = initializer
                initializer
            }
            return initializers.first()
        }
    }

    protected var next: NekoItemValidator? = null

    abstract fun init(): Result<Unit>

    protected fun initNext(): Result<Unit> {
        return next?.init() ?: Result.success(Unit)
    }
}

/**
 * Validate the required data by behaviours
 */
class BehaviorValidator(
    override val item: NekoItem
) : NekoItemValidator() {
    override fun init(): Result<Unit> {
        val allMetaTypes = item.meta.keys
        for (behavior in item.behaviors) {
            val requiredMetaTypes = behavior.requiredMetaTypes
            val missingMetaTypes = requiredMetaTypes.filter { it.java !in allMetaTypes }
            if (missingMetaTypes.isNotEmpty()) {
                return Result.failure(IllegalArgumentException("Can't find metas ${requiredMetaTypes.map { it.simpleName }} being required by the behavior ${behavior::class.simpleName} in the item ${item.key}"))
            }
        }
        return initNext()
    }
}