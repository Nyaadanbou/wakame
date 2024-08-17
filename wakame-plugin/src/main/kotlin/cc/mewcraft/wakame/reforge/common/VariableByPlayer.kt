package cc.mewcraft.wakame.reforge.common

/**
 * 标记一个属性或函数的返回值可以被玩家的操作而改变.
 */
@Target(
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FUNCTION
)
@Retention(
    AnnotationRetention.SOURCE
)
annotation class VariableByPlayer()
