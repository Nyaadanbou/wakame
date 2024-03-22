import cc.mewcraft.wakame.util.StringCombiner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import kotlin.test.assertEquals

class StringCombinerTest {

    @Test
    fun combine() {
        val combiner = StringCombiner("foo") {
            addList(listOf("a1", "a2", "a3")) // 默认条件
            addList(listOf("b1", "b2"), true) // 自定义条件
        }
        val result: List<String> = combiner.combine()
        val verify = listOf(
            "foo:a1:b1",
            "foo:a1:b2",
            "foo:a2:b1",
            "foo:a2:b2",
            "foo:a3:b1",
            "foo:a3:b2",
        )
        val executables = result.mapIndexed { index, s ->
            { assertEquals(verify[index], s) }
        }
        assertAll(
            { assertEquals(6, result.size) },
            *executables.toTypedArray()
        )
    }
}