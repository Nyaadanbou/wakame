package cc.mewcraft.wakame.lifecycle

import kotlinx.coroutines.CompletableDeferred
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaMethod

/**
 * Internal reflection helpers for lifecycle.
 */
internal object LifecycleReflection {

    suspend fun executeSuspendFunction(
        clazz: KClass<out Any>,
        functionName: String,
        completion: CompletableDeferred<Unit>,
    ) {
        val function = clazz.functions.first {
            it.javaMethod!!.name == functionName &&
                it.parameters.size == 1 &&
                it.parameters[0].kind == KParameter.Kind.INSTANCE
        }
        function.isAccessible = true
        function.callSuspend(clazz.objectInstance ?: throw IllegalArgumentException("${clazz.simpleName} is not an object"))
        completion.complete(Unit)
    }
}
