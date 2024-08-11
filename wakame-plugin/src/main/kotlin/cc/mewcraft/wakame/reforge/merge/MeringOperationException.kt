package cc.mewcraft.wakame.reforge.merge

/**
 * 合并操作的异常.
 */
open class MergingOperationException(
    message: String?,
) : Throwable(message)

/**
 * 参与合并的物品的等级超过了限制.
 */
class ExceedMaximumLevelException(
    message: String? = "Exceed maximum merging level",
) : MergingOperationException(message)

/**
 * 合并输出的物品的惩罚值超过了限制.
 */
class ExceedMaximumPenaltyException(
    message: String? = "Exceed maximum merging penalty",
) : MergingOperationException(message)

/**
 * 参与合并的物品的类型不能够用于合并操作.
 */
class UnacceptedPortableCoreException(
    message: String? = "Unaccepted portable core type",
) : MergingOperationException(message)

/**
 * 玩家没有足够的资源完成本次合并操作.
 */
class InsufficientResourceException(
    message: String? = "Insufficient player resource",
) : MergingOperationException(message)