package cc.mewcraft.wakame.skill.state

enum class SkillStateResult {
    SUCCESS,

    /**
     * 取消触发此操作的事件
     */
    CANCEL_EVENT,
    SILENT_FAILURE,
    ;
}