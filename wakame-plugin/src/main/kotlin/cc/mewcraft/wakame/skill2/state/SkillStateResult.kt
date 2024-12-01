package cc.mewcraft.wakame.skill2.state

enum class SkillStateResult {
    /**
     * 当前状态下的技能执行成功
     */
    SUCCESS,

    /**
     * 取消触发此操作的事件
     */
    CANCEL_EVENT,
    SILENT_FAILURE,
    ;
}