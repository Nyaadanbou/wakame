package cc.mewcraft.wakame.reforge.merge

/**
 * 合并操作的异常.
 */
open class MergingOperationException(
    message: String?,
) : Throwable(message)

/**
 * 被合并物品的等级超过了限制.
 */
class ExceedMaximumLevelException(
    message: String? = "Exceed maximum merging level",
) : MergingOperationException(message)

/**
 * 物品的类型不能够用于合并操作.
 */
class UnacceptedPortableCoreException(
    message: String? = "Unaccepted portable core",
) : MergingOperationException(message)

/**
 * 玩家没有足够的资源完成合并操作.
 */
class InsufficientResourceException(
    message: String? = "Insufficient resource",
) : MergingOperationException(message)