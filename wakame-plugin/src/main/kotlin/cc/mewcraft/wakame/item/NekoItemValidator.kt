package cc.mewcraft.wakame.item

/**
 * Verifies the correctness of NekoItem configuration.
 */
sealed class NekoItemValidator {
    data class Context(val item: NekoItem)

    companion object {
        /**
         * Chains the given validators, in order.
         *
         * @param validators the validators to be chained
         * @return the head of the validator
         */
        fun chain(vararg validators: NekoItemValidator): NekoItemValidator {
            validators.reduce { acc, validator ->
                validator.also { acc.next = it }
            }
            return validators.first()
        }
    }

    abstract fun validate(context: Context)

    protected var next: NekoItemValidator? = null

    protected fun validateNext(context: Context) {
        next?.validate(context)
    }
}