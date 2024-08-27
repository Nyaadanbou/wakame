package display

import cc.mewcraft.wakame.core.LorePipeline
import cc.mewcraft.wakame.core.PipelineHandler
import kotlin.test.Test
import kotlin.test.assertEquals

class LorePipelineTest {
    @Test
    fun `test pipeline handler`() {
        val pipelineResult1 = LorePipeline.create(StateLessHandler1)
            .concat(StateLessHandler2("Handler2"))
            .execute("Hello")
        assertEquals(Unit, pipelineResult1)

        val pipelineResult2 = LorePipeline.create(StateHandler1)
            .concat(StateHandler2("Handler2"))
            .execute("Hello", "World")
        assertEquals("Hello World Hello", pipelineResult2)
    }

    data object StateLessHandler1 : PipelineHandler.Stateless<String, String> {
        override fun process(input: String): String {
            assertEquals("Hello", input)
            println(toString() + "Input: $input")
            return "World"
        }
    }

    data class StateLessHandler2(val name: String) : PipelineHandler.Stateless<String, Unit> {
        init {
            assertEquals("Handler2", name)
        }

        override fun process(input: String) {
            assertEquals("World", input)
            println(toString() + "Input: $input")
        }
    }

    data object StateHandler1 : PipelineHandler<String, String, String> {
        override fun process(context: String, input: String): String {
            assertEquals("Hello", context)
            assertEquals("World", input)
            println(toString() + "Input: $input")
            return "$context $input"
        }
    }

    data class StateHandler2(val name: String) : PipelineHandler<String, String, String> {

        init {
            assertEquals("Handler2", name)
        }

        override fun process(context: String, input: String): String {
            assertEquals("Hello", context)
            assertEquals("Hello World", input)
            println(toString() + "Input: $input")
            return "$input $context"
        }
    }
}