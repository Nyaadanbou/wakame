package cc.mewcraft.wakame.core

/**
 * [cc.mewcraft.wakame.core.LorePipeline] 中的一环.
 */
fun interface PipelineHandler<in C, in I, out O> {

    fun process(context: C, input: I): O

    fun interface Stateless<in I, out O> : PipelineHandler<Any?, I, O> {

        fun process(input: I): O

        override fun process(context: Any?, input: I): O {
            return process(input)
        }
    }
}