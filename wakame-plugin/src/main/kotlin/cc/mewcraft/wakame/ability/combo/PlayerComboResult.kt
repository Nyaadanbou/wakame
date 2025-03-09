package cc.mewcraft.wakame.ability.combo

enum class PlayerComboResult {
    /**
     * 取消触发此操作的事件.
     */
    CANCEL_EVENT,

    /**
     * 偷偷失败, 取消技能触发.
     */
    SILENT_FAILURE,
    ;
}