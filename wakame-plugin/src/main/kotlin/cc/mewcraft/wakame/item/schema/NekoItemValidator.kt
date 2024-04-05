package cc.mewcraft.wakame.item.schema

import cc.mewcraft.wakame.item.schema.behavior.ItemBehavior
import cc.mewcraft.wakame.item.schema.meta.SchemaItemMeta
import kotlin.reflect.KClass

/**
 * Verifies the correctness of NekoItem configuration.
 */
sealed class NekoItemValidator {
    data class Args(val item: NekoItem)

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

    abstract fun validate(args: Args)

    protected var next: NekoItemValidator? = null
    protected fun validateNext(args: Args) {
        next?.validate(args)
    }
}

/**
 * Validate the required data by behaviors
 */
class BehaviorValidator : NekoItemValidator() {
    override fun validate(args: Args) {
        val item = args.item

        // Creates a map recording the missing meta types.
        // If it's empty in the end, then nothing is missing.
        val missingMetaTypes = mutableMapOf<KClass<out ItemBehavior>, MutableList<KClass<out SchemaItemMeta<*>>>>()

        for (behavior in item.behaviors) {
            behavior.requiredMetaTypes
                .filter { metaClass -> item.getMeta(metaClass).isEmpty }
                .forEach { metaClass -> missingMetaTypes.getOrPut(behavior::class) { mutableListOf() } += metaClass }
        }

        if (missingMetaTypes.isNotEmpty()) {
            val exceptionMessage = buildString {
                missingMetaTypes.forEach { (k, v) ->
                    append("The behavior '${k.qualifiedName}' requires the item meta '${v.joinToString(", ", transform = { it.simpleName!! })}' but it does present in the config")
                    append("\n")
                }
            }
            throw IllegalArgumentException(exceptionMessage)
        }

        return validateNext(args)
    }
}