package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.LOGGER
import org.jetbrains.annotations.VisibleForTesting

object RecursionGuard {

    @VisibleForTesting
    @JvmStatic
    val callDepthMap: ThreadLocal<HashMap<String, Int>> =
        ThreadLocal.withInitial(::HashMap)

    /**
     * 运行一段代码, 若检测到递归调用则阻止执行并返回 `null`.
     *
     * @param methodName 函数名 (唯一标识)
     * @param block 代码块, 只有在非递归调用时才会执行
     * @return 返回 `block` 计算的结果, 或者 `null` (若递归调用被阻止)
     */
    @JvmStatic
    inline fun <T> withValue(
        methodName: String,
        silence: Boolean,
        block: () -> T,
    ): T? {
        if (enter(methodName)) {
            if (!silence) {
                LOGGER.error("Recursive call detected in $methodName! Blocking execution.")
            }
            return null
        }

        try {
            return block()
        } finally {
            exit(methodName)
        }
    }

    /**
     * 运行一段代码, 若检测到递归调用则阻止执行.
     *
     * @param functionName 函数名 (唯一标识)
     * @param block 代码块, 只有在非递归调用时才会执行
     */
    @JvmStatic
    inline fun with(
        functionName: String,
        silenceLogs: Boolean,
        block: () -> Unit,
    ) {
        withValue(functionName, silenceLogs, block)
    }

    /**
     * 进入方法时调用, 检测递归调用.
     *
     * @param methodName 函数名 (唯一标识)
     * @return 是否检测到递归调用
     */
    @JvmStatic
    fun enter(methodName: String): Boolean {
        val depthMap = callDepthMap.get()
        val depth = depthMap.getOrDefault(methodName, 0) + 1
        depthMap[methodName] = depth
        return depth > 1
    }

    /**
     * 退出方法时调用, 减少计数并清理 [ThreadLocal].
     *
     * @param methodName 函数名 (唯一标识)
     */
    @JvmStatic
    fun exit(methodName: String) {
        val depthMap = callDepthMap.get()
        val currentDepth = depthMap[methodName] ?: return // 不存在时直接返回

        if (currentDepth > 1) {
            depthMap[methodName] = currentDepth - 1
        } else {
            depthMap.remove(methodName)
        }

        if (depthMap.isEmpty()) {
            callDepthMap.remove()
        }
    }

}
