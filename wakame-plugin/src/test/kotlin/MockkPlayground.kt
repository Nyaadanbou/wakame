import io.mockk.every
import io.mockk.mockkClass
import kotlin.test.Test
import kotlin.test.assertEquals

class Foo {
    fun foo(): String {
        return "foo"
    }
}

class MockkPlayground {
    @Test
    fun `mockk class`() {
        val fooMock = mockkClass(Foo::class, relaxed = true, relaxUnitFun = true)
        every { fooMock.foo() } returns "bar"
        assertEquals("bar", fooMock.foo())
    }
}