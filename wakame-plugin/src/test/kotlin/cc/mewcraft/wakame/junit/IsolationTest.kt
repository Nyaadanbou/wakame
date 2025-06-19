package cc.mewcraft.wakame.junit

import org.junit.jupiter.api.TestInstance
import kotlin.test.Test
import kotlin.test.assertNull

// 捣鼓一下 TestInstance.Lifecycle
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class IsolationTest {
    var memberInstance: Any? = null

    @Test
    fun test1() {
        staticInstance = Any()
        memberInstance = Any()
    }

    // @Test
    // fun test2() {
    //     assertNull(staticInstance)
    // }

    @Test
    fun test3() {
        assertNull(memberInstance)
    }

    companion object {
        @JvmStatic
        var staticInstance: Any? = null
    }
}
