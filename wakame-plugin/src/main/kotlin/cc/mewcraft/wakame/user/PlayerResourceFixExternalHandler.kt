package cc.mewcraft.wakame.user

import cc.mewcraft.wakame.LOGGER


/** 该接口允许外部代码实现一个 [PlayerResourceFix] 的具体逻辑, 以此来避免 CNF 异常. */
fun interface PlayerResourceFixExternalHandler {

    /** 实现该函数来执行必要的逻辑, 例如注册 Event Handler. */
    fun run()

    /** 语法糖, 让外部可以直接 `PlayerResourceFixExternalHandler.CURRENT_HANDLER()`. */
    operator fun invoke() {
        run()
    }

    companion object {
        // 当前所使用的实例
        internal var CURRENT_HANDLER: PlayerResourceFixExternalHandler = PlayerResourceFixExternalHandler {
            LOGGER.error("The current PlayerResourceFixHandler is a no-op but it's being called. This is a bug!", Error())
        }
    }

}