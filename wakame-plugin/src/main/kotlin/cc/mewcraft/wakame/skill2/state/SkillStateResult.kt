package cc.mewcraft.wakame.skill2.state

enum class SkillStateResult {
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