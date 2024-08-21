package cc.mewcraft.wakame.core

/**
 * [cc.mewcraft.wakame.core.Pipeline] 中的一环.
 */
fun interface PipelineHandler<C, I, O> {

    fun process(context: C, input: I): O

    fun interface Stateless<I, O> : PipelineHandler<Nothing, I, O> {

        fun process(input: I): O

        override fun process(context: Nothing, input: I): O {
            return process(input)
        }
    }
}