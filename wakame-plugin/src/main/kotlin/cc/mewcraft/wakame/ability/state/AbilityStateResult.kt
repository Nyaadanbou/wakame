package cc.mewcraft.wakame.ability.state

enum class AbilityStateResult {
    /**
     * 取消触发此操作的事件
     */
    CANCEL_EVENT,

    /**
     * 取消技能的触发
     */
    SILENT_FAILURE,
    ;
}