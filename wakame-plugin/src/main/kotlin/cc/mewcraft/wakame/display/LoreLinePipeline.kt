package cc.mewcraft.wakame.display

// TODO 为物品渲染的 LoreLine 引入一套“流水线”架构
//  - 流水线的开始是一堆无序的,原始的 LoreLine
//  - 用户可以加入一些处理器来修改这些 LoreLine (例如: sort, flat, map)
//  - 最终输出的是一个 List<Component>
interface LoreLinePipeline {
    fun <I : LoreLine, O : LoreLine> addFirst(handler: LoreLinePipelineHandler<I, O>)
    fun <I : LoreLine, O : LoreLine> addLast(handler: LoreLinePipelineHandler<I, O>)
    fun process(input: LoreLine): LoreLine
}

interface LoreLinePipelineHandler<I : LoreLine, O : LoreLine> {
    /**
     * 给定一个 [I], 输入一个 [O].
     */
    fun handle(input: I): O
}