package cc.mewcraft.wakame.core

/**
 * 代表一个流水线.
 */
interface Pipeline<C, I, O> {
    /**
     * 拼接一个新的处理器到末尾, 并返回新的流水线.
     */
    fun <K> concat(next: PipelineHandler<C, O, K>): Pipeline<C, I, K>

    /**
     * 执行流水线.
     */
    fun execute(input: I): O

    /**
     * 执行流水线.
     */
    fun execute(context: C, input: I): O
}

class SimplePipeline<C, I, O>(
    private val handler: PipelineHandler<C, I, O>
) : Pipeline<C, I, O> {

    override fun <K> concat(next: PipelineHandler<C, O, K>): Pipeline<C, I, K> {
        return SimplePipeline(PipelineHandler { context: C, input: I ->
            next.process(context, handler.process(context, input))
        })
    }

    override fun execute(input: I): O {
        if (handler is PipelineHandler.Stateless) {
            // 这也只是确保当前的
            return handler.process(input)
        } else {
            throw IllegalStateException("Cannot execute stateful pipeline without context.")
        }
    }

    override fun execute(context: C, input: I): O {
        when (handler) {
            is PipelineHandler.Stateless -> {
                return handler.process(input) // (?) 避免 cast exception
            }

            else -> {
                return handler.process(context, input)
            }
        }
    }
}
