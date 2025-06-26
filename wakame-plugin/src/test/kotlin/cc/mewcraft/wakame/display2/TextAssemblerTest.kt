package cc.mewcraft.wakame.display2

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.display2.implementation.BlankStaticTextMeta
import cc.mewcraft.wakame.display2.implementation.CustomStaticTextMeta
import cc.mewcraft.wakame.util.test.TestOnly
import cc.mewcraft.wakame.util.test.TestPath
import io.mockk.every
import io.mockk.mockk
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Key.key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 测试 [TextAssembler].
 *
 * 本测试使用了 [TextAssemblerTestLifecycle] 来快速构建测试用例.
 */
class TextAssemblerTest {

    // 每个 test 用到了不同的 RendererConfig 和
    // LoreMetaLookup, 因此需要在每个 test 前后
    // 重新注册和注销 mock

    @OptIn(TestOnly::class)
    @BeforeTest
    fun before() {
        KoishDataPaths.initializeForTest(TestPath.TEST)
    }

    // 每个测试都是手动定义好:
    // - default indexed texts
    //   static indexed texts
    // - text ordinal map
    //   text metadata map
    //
    // 而不用配置文件生成的结果,
    // 配置文件生成应该另外测试.

    /**
     * 情况:
     * - input 覆盖了所有可能出现的 indexed texts
     * 结果:
     * - 排列顺序完全正确
     * - 应该完全不采用默认值
     */
    @Test
    fun `case 1`(): Unit = textAssemblerTestLifecycle {
        configure {
            static {
                // 添加两个固定的内容行.
                // 不要忘记为它们配置好 text meta.
                +staticIndexedText("fixed", "1")
                +staticIndexedText("fixed", "2")
            }
            default {
                // 为 "a:1", "b:1", "c:1" 添加默认值;
                // 如果这些数据不存在, 将采用这里的默认值;
                +defaultIndexedText("a", "1")
                +defaultIndexedText("b", "1")
                +defaultIndexedText("c", "1")
            }
            index {
                // 为每一个可能出现的内容指定好相对位置.
                put("a", "1", 1)
                put("a", "2", 2)
                put("a", "3", 3)
                put("b", "1", 4)
                put("b", "2", 5)
                put("fixed", "1", 6)
                put("c", "1", 7)
                put("fixed", "2", 8)
            }
            meta {
                // 为每一个固定的内容行指定好元数据;
                // 动态的内容行无需指定元数据, 因为它们仅仅是用来生成 renderer config,
                // 而 renderer config 在这个测试中是完全手动定义的.
                put("fixed", "1", TextMetaBuilder.blankStatic(-1, null))
                put("fixed", "2", TextMetaBuilder.blankStatic(-1, null))
            }
        }
        runTest {
            expected {
                // 设置最终渲染出来的内容.
                // 使用函数:
                // simple() 或 default()
                // 来构建内容.
                simple("a", "1")
                simple("a", "2")
                simple("a", "3")
                simple("b", "1")
                simple("b", "2")
                simple("fixed", "1")
                simple("c", "1")
                simple("fixed", "2")
            }
            input {
                // 设置实际的内容.
                // 直接使用 IndexedText 的各种构建函数来构建实际内容.
                +plainIndexedText("a", "2")
                +plainIndexedText("a", "1")
                +plainIndexedText("b", "2")
                +plainIndexedText("b", "1")
                +plainIndexedText("a", "3")
                +plainIndexedText("c", "1")
            }
        }
    }


    /**
     * 情况:
     * - input 没有覆盖所有可能的 indexed text
     * - 某些 input 在 renderer config 中拥有默认值
     * 结果:
     * - 排列顺序完全正确
     * - 有 input 的 indexed text 应该采用 input 本身的值
     * - 没有 input 的 indexed text 应该采用默认值
     */
    @Test
    fun `case 2`(): Unit = textAssemblerTestLifecycle {
        configure {
            static {
                +staticIndexedText("fixed", "1")
                +staticIndexedText("fixed", "2")
            }
            default {
                +defaultIndexedText("a", "1")
                +defaultIndexedText("b", "1")
                +defaultIndexedText("c", "1")
            }
            index {
                put("a", "1", 1)
                put("a", "2", 2)
                put("a", "3", 3)
                put("b", "1", 4)
                put("b", "2", 5)
                put("fixed", "1", 6)
                put("c", "1", 7)
                put("fixed", "2", 8)
            }
            meta {
                put("fixed", "1", TextMetaBuilder.blankStatic(-1, null))
                put("fixed", "2", TextMetaBuilder.blankStatic(-1, null))
            }
        }
        runTest {
            expected {
                default("a", "1")
                simple("a", "2")
                simple("a", "3")
                default("b", "1")
                simple("b", "2")
                simple("fixed", "1")
                default("c", "1")
                simple("fixed", "2")
            }
            input {
                +plainIndexedText("a", "2")
                +plainIndexedText("a", "3")
                +plainIndexedText("b", "2")
            }
        }
    }

    /**
     * 情况:
     * - 设置了默认值, 并且默认值是自定义的内容
     * 结果:
     * - 当 input 缺省时, 自定义的默认值应该被渲染出来
     */
    @Test
    fun `case 3`() {
        textAssemblerTestLifecycle {
            configure {
                static {
                    // 没有固定内容
                }
                default {
                    +defaultIndexedText("a", "1", "foo")
                    +defaultIndexedText("b", "1", "bar")
                }
                index {
                    put("a", "1", 1)
                    put("b", "1", 2)
                }
                meta {
                    // 没有固定内容
                }
            }
            runTest {
                expected {
                    // a:1 有默认值, 但是 input 中没有 a:1,
                    // 因此应该渲染出默认值.
                    default("a", "1", "foo")
                    simple("b", "1")
                }
                input {
                    +plainIndexedText("b", "1")
                }
            }
            runTest { // 交换 a 和 b 的位置
                expected {
                    // b:1 有默认值, 但是 input 中没有 b:1,
                    // 因此应该渲染出默认值.
                    simple("a", "1")
                    default("b", "1", "bar")
                }
                input {
                    +plainIndexedText("a", "1")
                }
            }
        }
    }

    /**
     * 情况:
     * - 设置了固定内容, 并且还设置了 companion namespace
     * 结果:
     * - 仅当固定内容下面存在指定的 namespace 命名空间下的内容时才渲染
     */
    @Test
    fun `case 4`() {
        textAssemblerTestLifecycle {
            configure {
                static {
                    +staticIndexedText("fixed", "1")
                }
                default {
                    +defaultIndexedText("a", "1")
                    +defaultIndexedText("b", "1")
                }
                index {
                    put("fixed", "1", 1)
                    put("a", "1", 2)
                    put("b", "1", 3)
                }
                meta {
                    put("fixed", "1", TextMetaBuilder.blankStatic(-1, "a"))
                }
            }
            runTest {
                expected {
                    // fixed:1 要求下面有 namespace 为 “a” 的内容,
                    // 并且实际上也确实存在这样的内容: “a:1”,
                    // 因此最终结果应该是这样的:
                    simple("fixed", "1")
                    simple("a", "1")
                    simple("b", "1")
                }
                input {
                    +plainIndexedText("a", "1")
                    +plainIndexedText("b", "1")
                }
            }
        }

        textAssemblerTestLifecycle {
            configure {
                static {
                    +staticIndexedText("fixed", "1")
                }
                default {
                    +defaultIndexedText("b", "1")
                }
                index {
                    put("fixed", "1", 1)
                    put("a", "1", 2)
                    put("b", "1", 3)
                }
                meta {
                    put("fixed", "1", TextMetaBuilder.blankStatic(-1, "b"))
                }
            }
            runTest {
                expected {
                    // 设置上 fixed:1 要求下面有 namespace 为 “b” 的内容,
                    // 但实际上下面的内容为: “a:1”,
                    // 因此 fixed:1 不应该渲染出来:
                    simple("a", "1")
                    simple("b", "1")
                }
                input {
                    +plainIndexedText("a", "1")
                    +plainIndexedText("b", "1")
                }
            }
            runTest {
                expected {
                    // 设置上 fixed:1 要求下面有 namespace 为 “b” 的内容.
                    // 如果 input 完整, 那么 fixed:1 下面的内容应该是 a:1;
                    // 但是 input 实际不完整 (没有 a:1), 所以根据相对位置, 实际上下面的内容为 b:1;
                    // 因此结果上 fixed:1 应该要渲染出来:
                    simple("fixed", "1")
                    simple("b", "1")
                }
                input {
                    +plainIndexedText("b", "1")
                }
            }
        }
    }

    /**
     * 情况:
     * - 设置了固定内容, 并且 companion namespace 设置为 *
     * 结果:
     * - 仅当固定内容下面存在任意 namespace 命名空间(不包括不存在的情况) 下的内容时才渲染
     */
    @Test
    fun `case 5`() {
        textAssemblerTestLifecycle {
            configure {
                static {
                    +staticIndexedText("fixed", "1")
                }
                default {
                    // 无默认值
                }
                index {
                    put("fixed", "1", 1)
                    put("a", "1", 2)
                    put("b", "1", 3)
                }
                meta {
                    put("fixed", "1", TextMetaBuilder.blankStatic(-1, "*"))
                }
            }
            runTest {
                expected {
                    // fixed:1 要求下面有任意 namespace 的内容,
                    // 并且实际上也确实存在这样的内容: “a:1”,
                    // 因此最终结果应该是这样的:
                    simple("fixed", "1")
                    simple("a", "1")
                }
                input {
                    +plainIndexedText("a", "1")
                }
            }
            runTest {
                expected {
                    // fixed:1 要求下面有任意 namespace 的内容,
                    // 并且实际上也确实存在这样的内容: “b:1”,
                    // 因此最终结果应该是这样的:
                    simple("fixed", "1")
                    simple("b", "1")
                }
                input {
                    +plainIndexedText("b", "1")
                }
            }
            runTest {
                expected {
                    // fixed:1 要求下面有任意 namespace 的内容,
                    // 但实际上不存在任何内容, 因此最后是空的.
                }
                input {
                    // 空
                }
            }
        }
    }

    /**
     * 情况:
     * - 添加多个完全相同的 indexed text
     * 结果:
     * - 应该只有一个 indexed text 会被渲染
     */
    @Test
    fun `case 6`() {
        textAssemblerTestLifecycle {
            configure {
                static {
                    // 无固定内容
                }
                default {
                    +defaultIndexedText("a", "1")
                }

                index {
                    put("a", "1", 1)
                }
                meta {
                    // 无固定内容
                }
            }
            runTest {
                expected {
                    // a:1 只会被渲染一次
                    simple("a", "1")
                }
                input {
                    +plainIndexedText("a", "1")
                    +plainIndexedText("a", "1")
                    +plainIndexedText("a", "1")
                }
            }
        }
    }
}

// 方便构建测试的函数

private val Component.plain: String
    get() = PlainTextComponentSerializer.plainText().serialize(this)

private fun listText(vararg text: String): List<Component> =
    text.map { Component.text(it) }

// IndexedText 的构造函数

private fun plainIndexedText(namespace: String, value: String): IndexedText =
    SimpleIndexedText(key(namespace, value), listText("$namespace:$value"))

private fun defaultIndexedText(namespace: String, value: String, customText: String? = null): IndexedText =
    SimpleIndexedText(key(namespace, value), listText("$namespace:$value@" + (customText ?: "default")))

private fun staticIndexedText(namespace: String, value: String): IndexedText =
    StaticIndexedText(key(namespace, value), listText("$namespace:$value"))

// Assertion 扩展

private fun assertEquals0(expected: List<Component>, actual: List<Component>) {
    val expectedText = expected.joinToString("\n") { it.plain }
    val actualText = actual.joinToString("\n") { it.plain }
    println()
    println("Actual:\n$actualText")
    assertEquals(expectedText, actualText)
}

// 构建测试

private fun textAssemblerTestLifecycle(init: TextAssemblerTestLifecycle.() -> Unit) {
    TextAssemblerTestLifecycle().apply(init).buildAndTest()
}

/**
 * 用于构建测试用例的辅助类. 使用 [textAssemblerTestLifecycle] 来构建测试用例.
 */
@TextAssemblerTestLifecycleDsl
private class TextAssemblerTestLifecycle {
    // built configurations
    private lateinit var defaultTexts: List<IndexedText>
    private lateinit var staticTexts: List<IndexedText>
    private lateinit var ordinalMap: Map<Key, Int>
    private lateinit var metadataMap: Map<Key, TextMeta>

    // built test runners
    private val runners: MutableList<TestRunner> = mutableListOf()

    // 构建 RendererConfig.
    // 目前的实现只需要构建:
    // defaultTexts - 拥有默认内容的 (Simple)IndexedText - 如果这些没有在 input 中出现, 在进行 assemble 后将会变成 'namespace:value@default'
    // staticTexts - 始终要渲染的 (Static)IndexedText - 无论 input 是什么, 都需要渲染的 IndexedText; 当然, 有些是有 companionNamespace 的
    fun configure(block: RendererLayoutBuilder.() -> Unit) {
        val builder = RendererLayoutBuilder().apply(block)
        defaultTexts = builder.defaultTexts
        staticTexts = builder.staticTexts
        ordinalMap = builder.ordinalMap
        metadataMap = builder.metadataMap
    }

    fun runTest(block: TestRunner.() -> Unit) {
        runners += TestRunner().apply(block)
    }

    // 用上面构建好的配置, 运行测试
    fun buildAndTest() {
        println()

        // configure injections
        val rendererLayoutMock = mockk<RendererLayout> {
            every { defaultIndexedTextList } returns defaultTexts
            every { staticIndexedTextList } returns staticTexts
            every { getOrdinal(any()) } answers { ordinalMap.getValue(firstArg()) }
            every { getMetadata<TextMeta>(any()) } answers { metadataMap.getValue(firstArg()) }
        }

        // create assembler
        val assembler = TextAssembler(
            rendererLayout = rendererLayoutMock,
        )

        // run runners
        for (runner in runners) {
            val actual = assembler.assemble(ReferenceOpenHashSet(runner.input))
            val expected = runner.expected
            assertEquals0(expected, actual)
        }
    }

    @TextAssemblerTestLifecycleDsl
    class RendererLayoutBuilder {
        val defaultTexts = mutableListOf<IndexedText>()
        val staticTexts = mutableListOf<IndexedText>()
        val ordinalMap = mutableMapOf<Key, Int>() // key -> ordinal
        val metadataMap = mutableMapOf<Key, TextMeta>() // key -> metadata

        fun meta(init: MetadataBuilder.() -> Unit) {
            val builder = MetadataBuilder()
            builder.init()
            metadataMap.putAll(builder.map)
        }

        fun index(init: IndexBuilder.() -> Unit) {
            val builder = IndexBuilder()
            builder.init()
            ordinalMap.putAll(builder.map)
        }

        fun static(init: ConstantTextBuilder.() -> Unit) {
            ConstantTextBuilder().apply(init).texts.forEach { staticTexts.add(it) }
        }

        fun default(init: DefaultTextBuilder.() -> Unit) {
            DefaultTextBuilder().apply(init).texts.forEach { defaultTexts.add(it) }
        }
    }

    @TextAssemblerTestLifecycleDsl
    class DefaultTextBuilder {
        val texts = mutableListOf<IndexedText>()

        operator fun IndexedText.unaryPlus() {
            texts.add(this)
        }
    }

    @TextAssemblerTestLifecycleDsl
    class ConstantTextBuilder {
        val texts = mutableListOf<IndexedText>()

        operator fun IndexedText.unaryPlus() {
            texts.add(this)
        }
    }

    @TextAssemblerTestLifecycleDsl
    class IndexBuilder {
        val map = mutableMapOf<Key, Int>()

        fun put(namespace: String, value: String, index: Int) {
            map[key(namespace, value)] = index
        }
    }

    @TextAssemblerTestLifecycleDsl
    class MetadataBuilder {
        val map = mutableMapOf<Key, TextMeta>()

        fun put(namespace: String, value: String, meta: TextMeta) {
            map[key(namespace, value)] = meta
        }
    }

    @TextAssemblerTestLifecycleDsl
    class ActualBuilder {
        val texts = mutableListOf<IndexedText>()

        operator fun IndexedText.unaryPlus() {
            // 不能直接在这里进行 assemble 操作, 因为 assemble
            // 需要用到渲染的配置和 ordinal/metadata map,
            // 而到这里还没有注入好这两个对象.
            texts.add(this)
        }
    }

    @TextAssemblerTestLifecycleDsl
    class ExpectedBuilder {
        val texts = mutableListOf<String>()

        // 内容存在于 input
        fun simple(namespace: String, value: String) {
            texts += "$namespace:$value"
        }

        // 内容不存在于 input, 但是 renderer config 里有默认值
        fun default(namespace: String, value: String, customText: String? = null) {
            texts += "$namespace:$value@" + (customText ?: "default")
        }
    }

    @TextAssemblerTestLifecycleDsl
    class TestRunner {
        // expected & input
        lateinit var expected: List<Component>
        lateinit var input: List<IndexedText>

        // 构建 input.
        // 你需要把所有要渲染的 (Simple)IndexedText 通过这个函数添加;
        // 注意不要添加 (Static)IndexedText, 这些会由 assembler 自己添加.
        fun input(block: ActualBuilder.() -> Unit) {
            val builder = ActualBuilder().apply(block)
            input = builder.texts
        }

        // 构建 expected.
        // 你需要指定最终渲染出来的文本内容*应该*是什么样子
        fun expected(block: ExpectedBuilder.() -> Unit) {
            val builder = ExpectedBuilder().apply(block)
            expected = builder.texts.map { Component.text(it) }
        }
    }
}

/**
 * 快速构建 [TextMeta].
 */
private object TextMetaBuilder {
    // 固定的空白内容行
    // 对于一个手动定义的渲染配置来说,
    // 这里的 source ordinal 实际上没有作用.
    fun blankStatic(sourceOrdinal: SourceOrdinal, companionNamespace: String?): TextMeta =
        BlankStaticTextMeta(sourceOrdinal, companionNamespace)

    // 固定的自定义内容行
    // 对于一个手动定义的渲染配置来说,
    // 这里的 source ordinal 实际上没有作用.
    fun customStatic(sourceOrdinal: SourceOrdinal, companionNamespace: String, vararg text: String): TextMeta =
        CustomStaticTextMeta(sourceOrdinal, companionNamespace, listText(*text))

    // 按照物品的数据动态生成的内容行
    fun simple(): TextMeta =
        throw NotImplementedError() // 实际上不需要定义 simple text meta, 因为这只是用来生成配置文件
}

@DslMarker
private annotation class TextAssemblerTestLifecycleDsl