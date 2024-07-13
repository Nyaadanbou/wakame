package display

import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.display.BlankConstantLoreMeta
import cc.mewcraft.wakame.display.CustomConstantLoreMeta
import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.LoreLineFlatter
import cc.mewcraft.wakame.display.LoreMeta
import cc.mewcraft.wakame.display.LoreMetaLookup
import cc.mewcraft.wakame.display.LoreMetaLookupImpl
import cc.mewcraft.wakame.display.RENDERER_GLOBAL_CONFIG_FILE
import cc.mewcraft.wakame.display.RawTooltipIndex
import cc.mewcraft.wakame.display.RendererConfig
import cc.mewcraft.wakame.display.RendererConfigImpl
import io.mockk.every
import io.mockk.mockkClass
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Key.key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.mock.MockProvider
import org.koin.test.mock.declare
import org.koin.test.mock.declareMock
import testEnv
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 测试 [LoreLineFlatter].
 *
 * 本测试使用了 [LoreLineFlatterLifecycle] 来快速构建测试用例.
 */
class LoreLineFlatter2Test : KoinTest {

    // 每个 test 用到了不同的 RendererConfig 和
    // LoreMetaLookup, 因此需要在每个 test 前后
    // 重新注册和注销 mock

    @BeforeTest
    fun before() {
        MockProvider.register { clazz ->
            mockkClass(clazz)
        }

        startKoin {
            modules(
                testEnv(),
            )

            modules(module {
                single<RendererConfig> { RendererConfigImpl(Configs.YAML[RENDERER_GLOBAL_CONFIG_FILE]) }
            })
        }
    }

    @AfterTest
    fun after() {
        stopKoin()
    }

    // 每个测试都是手动定义好:
    // - renderer config:
    //   default lore lines
    //   constant lore lines
    // - lore meta lookup
    // 不用配置文件生成的结果.
    // 配置文件生成应该另外测试.

    /**
     * 情况:
     * - input 覆盖了所有可能出现的 lore line
     * 结果:
     * - 排列顺序完全正确
     * - 应该完全不采用默认值
     */
    @Test
    fun `case 1`(): Unit = loreLineFlatterLifecycle {
        rendererConfig {
            default {
                // 为 "a:1", "b:1", "c:1" 添加默认值;
                // 如果这些数据不存在, 将采用这里的默认值;
                +defaultLoreLine("a", "1")
                +defaultLoreLine("b", "1")
                +defaultLoreLine("c", "1")
            }
            constant {
                // 添加两个固定的内容行.
                // 不要忘记为它们配置好 lore meta.
                +constantLoreLine("fixed", "1")
                +constantLoreLine("fixed", "2")
            }
        }
        loreMetaLookup {
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
                put("fixed", "1", LoreMetaBuilder.blankConstant(-1, null))
                put("fixed", "2", LoreMetaBuilder.blankConstant(-1, null))
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
                // 直接使用 LoreLine 的各种构建函数来构建实际内容.
                +simpleLoreLine("a", "2")
                +simpleLoreLine("a", "1")
                +simpleLoreLine("b", "2")
                +simpleLoreLine("b", "1")
                +simpleLoreLine("a", "3")
                +simpleLoreLine("c", "1")
            }
        }
    }


    /**
     * 情况:
     * - input 没有覆盖所有可能的 lore line
     * - 某些 input 在 renderer config 中拥有默认值
     * 结果:
     * - 排列顺序完全正确
     * - 有 input 的 lore line 应该采用 input 本身的值
     * - 没有 input 的 lore line 应该采用默认值
     */
    @Test
    fun `case 2`(): Unit = loreLineFlatterLifecycle {
        rendererConfig {
            default {
                +defaultLoreLine("a", "1")
                +defaultLoreLine("b", "1")
                +defaultLoreLine("c", "1")
            }
            constant {
                +constantLoreLine("fixed", "1")
                +constantLoreLine("fixed", "2")
            }
        }
        loreMetaLookup {
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
                put("fixed", "1", LoreMetaBuilder.blankConstant(-1, null))
                put("fixed", "2", LoreMetaBuilder.blankConstant(-1, null))
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
                +simpleLoreLine("a", "2")
                +simpleLoreLine("a", "3")
                +simpleLoreLine("b", "2")
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
        loreLineFlatterLifecycle {
            rendererConfig {
                default {
                    +defaultLoreLine("a", "1", "foo")
                    +defaultLoreLine("b", "1", "bar")
                }
                constant {
                    // 没有固定内容
                }
            }
            loreMetaLookup {
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
                    +simpleLoreLine("b", "1")
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
                    +simpleLoreLine("a", "1")
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
        loreLineFlatterLifecycle {
            rendererConfig {
                default {
                    +defaultLoreLine("a", "1")
                    +defaultLoreLine("b", "1")
                }
                constant {
                    +constantLoreLine("fixed", "1")
                }
            }
            loreMetaLookup {
                index {
                    put("fixed", "1", 1)
                    put("a", "1", 2)
                    put("b", "1", 3)
                }
                meta {
                    put("fixed", "1", LoreMetaBuilder.blankConstant(-1, "a"))
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
                    +simpleLoreLine("a", "1")
                    +simpleLoreLine("b", "1")
                }
            }
        }

        loreLineFlatterLifecycle {
            rendererConfig {
                default {
                    +defaultLoreLine("b", "1")
                }
                constant {
                    +constantLoreLine("fixed", "1")
                }
            }
            loreMetaLookup {
                index {
                    put("fixed", "1", 1)
                    put("a", "1", 2)
                    put("b", "1", 3)
                }
                meta {
                    put("fixed", "1", LoreMetaBuilder.blankConstant(-1, "b"))
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
                    +simpleLoreLine("a", "1")
                    +simpleLoreLine("b", "1")
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
                    +simpleLoreLine("b", "1")
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
        loreLineFlatterLifecycle {
            rendererConfig {
                default {
                    // 无默认值
                }
                constant {
                    +constantLoreLine("fixed", "1")
                }
            }
            loreMetaLookup {
                index {
                    put("fixed", "1", 1)
                    put("a", "1", 2)
                    put("b", "1", 3)
                }
                meta {
                    put("fixed", "1", LoreMetaBuilder.blankConstant(-1, "*"))
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
                    +simpleLoreLine("a", "1")
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
                    +simpleLoreLine("b", "1")
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
     * - 添加多个完全相同的 lore line
     * 结果:
     * - 应该只有一个 lore line 会被渲染
     */
    @Test
    fun `case 6`() {
        loreLineFlatterLifecycle {
            rendererConfig {
                default {
                    +defaultLoreLine("a", "1")
                }
                constant {
                    // 无固定内容
                }
            }
            loreMetaLookup {
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
                    +simpleLoreLine("a", "1")
                    +simpleLoreLine("a", "1")
                    +simpleLoreLine("a", "1")
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

// LoreLine 的构造函数

private fun noopLoreLine(): LoreLine =
    LoreLine.noop()

private fun simpleLoreLine(namespace: String, value: String): LoreLine =
    LoreLine.simple(key(namespace, value), listText("$namespace:$value"))

private fun defaultLoreLine(namespace: String, value: String, customText: String? = null): LoreLine =
    LoreLine.simple(key(namespace, value), listText("$namespace:$value@" + (customText ?: "default")))

private fun constantLoreLine(namespace: String, value: String): LoreLine =
    LoreLine.constant(key(namespace, value), listText("$namespace:$value"))

// Assertion 扩展

private fun assertEquals0(expected: List<Component>, actual: List<Component>) {
    val expectedText = expected.joinToString("\n") { it.plain }
    val actualText = actual.joinToString("\n") { it.plain }
    println()
    println("Actual:\n$actualText")
    assertEquals(expectedText, actualText)
}

// 构建测试

private fun loreLineFlatterLifecycle(init: LoreLineFlatterLifecycle.() -> Unit) {
    LoreLineFlatterLifecycle().apply(init).buildAndTest()
}

/**
 * 用于构建测试用例的辅助类. 使用 [loreLineFlatterLifecycle] 来构建测试用例.
 */
@LoreLineFlatterDsl
private class LoreLineFlatterLifecycle : KoinTest {
    // renderer config
    private lateinit var defaultLines: List<LoreLine>
    private lateinit var constantLines: List<LoreLine>

    // lore meta lookup
    private lateinit var loreMetaLookup: LoreMetaLookup

    // all test runners
    private val runners: MutableList<TestRunner> = mutableListOf()

    // 构建 RendererConfig.
    // 目前的实现只需要构建:
    // defaultLines - 拥有默认内容的 (Dynamic)LoreLine - 如果这些没有在 input 中出现, 在 makeFlatten 后将会变成 'namespace:value@default'
    // constantLines - 始终要渲染的 (Constant)LoreLine - 无论 input 是什么, 都需要渲染的 LoreLine; 当然, 有些是有 companionNamespace 的
    fun rendererConfig(block: RendererConfigBuilder.() -> Unit) {
        val builder = RendererConfigBuilder().apply(block)
        defaultLines = builder.defaultLines
        constantLines = builder.constantLines
    }

    // 构建 LoreMetaLookup.
    // 用于指定每个 LoreLine 的索引和元数据
    fun loreMetaLookup(block: LoreMetaLookupBuilder.() -> Unit) {
        val builder = LoreMetaLookupBuilder().apply(block)
        loreMetaLookup = builder.build()
    }


    fun runTest(block: TestRunner.() -> Unit) {
        val runner = TestRunner().apply(block)
        runners += runner
    }

    // 用上面构建好的配置, 运行测试
    fun buildAndTest() {
        println()

        // configure injections
        declareMock<RendererConfig> {
            every { defaultLoreLines } returns defaultLines
            every { constantLoreLines } returns constantLines
        }
        declare<LoreMetaLookup> {
            loreMetaLookup
        }

        // create flatter
        val flatter = LoreLineFlatter(
            rendererConfig = get(),
            loreMetaLookup = get()
        )

        // run runners
        for (runner in runners) {
            val actual = flatter.flatten(runner.input)
            val expected = runner.expected
            assertEquals0(expected, actual)
        }
    }

    @LoreLineFlatterDsl
    class RendererConfigBuilder {
        val defaultLines = mutableListOf<LoreLine>()
        val constantLines = mutableListOf<LoreLine>()

        fun default(init: DefaultLineBuilder.() -> Unit) {
            DefaultLineBuilder().apply(init).lines.forEach { defaultLines.add(it) }
        }

        fun constant(init: ConstantLineBuilder.() -> Unit) {
            ConstantLineBuilder().apply(init).lines.forEach { constantLines.add(it) }
        }
    }

    @LoreLineFlatterDsl
    class DefaultLineBuilder {
        val lines = mutableListOf<LoreLine>()

        operator fun LoreLine.unaryPlus() {
            lines.add(this)
        }
    }

    @LoreLineFlatterDsl
    class ConstantLineBuilder {
        val lines = mutableListOf<LoreLine>()

        operator fun LoreLine.unaryPlus() {
            lines.add(this)
        }
    }

    @LoreLineFlatterDsl
    class ActualBuilder {
        val lines = mutableListOf<LoreLine>()

        operator fun LoreLine.unaryPlus() {
            // 不能直接在这里 makeFlatten, 因为 flatten
            // 需要用到 renderer config 和 lore meta lookup,
            // 而到这里还没有注入好这两个对象.
            lines.add(this)
        }
    }

    @LoreLineFlatterDsl
    class ExpectedBuilder {
        val lines = mutableListOf<String>()

        // 内容存在于 input
        fun simple(namespace: String, value: String) {
            lines += "$namespace:$value"
        }

        // 内容不存在于 input, 但是 renderer config 里有默认值
        fun default(namespace: String, value: String, customText: String? = null) {
            lines += "$namespace:$value@" + (customText ?: "default")
        }
    }

    @LoreLineFlatterDsl
    class TestRunner {
        // expected & input
        lateinit var expected: List<Component>
        lateinit var input: List<LoreLine>

        // 构建 input.
        // 你需要把所有要渲染的 (Dynamic)LoreLine 通过这个函数添加;
        // 注意不要添加 ConstantLoreLine, 这些会由 flatter 自己添加.
        fun input(block: ActualBuilder.() -> Unit) {
            val builder = ActualBuilder().apply(block)
            input = builder.lines
        }

        // 构建 expected.
        // 你需要指定最终渲染出来的文本内容*应该*是什么样子
        fun expected(block: ExpectedBuilder.() -> Unit) {
            val builder = ExpectedBuilder().apply(block)
            expected = builder.lines.map { Component.text(it) }
        }
    }
}

/**
 * 用于构建 [LoreMetaLookup] 的辅助类, 配合 [buildLoreMetaLookup] 使用.
 */
private class LoreMetaLookupBuilder {
    private val indexes = mutableMapOf<Key, Int>()
    private val metadata = mutableMapOf<Key, LoreMeta>()

    fun index(init: IndexBuilder.() -> Unit) {
        val builder = IndexBuilder()
        builder.init()
        indexes.putAll(builder.map)
    }

    fun meta(init: MetadataBuilder.() -> Unit) {
        val builder = MetadataBuilder()
        builder.init()
        metadata.putAll(builder.map)
    }

    fun build(): LoreMetaLookup = LoreMetaLookupImpl(indexes, metadata)

    class IndexBuilder {
        val map = mutableMapOf<Key, Int>()

        fun put(namespace: String, value: String, index: Int) {
            map[key(namespace, value)] = index
        }
    }

    class MetadataBuilder {
        val map = mutableMapOf<Key, LoreMeta>()

        fun put(namespace: String, value: String, meta: LoreMeta) {
            map[key(namespace, value)] = meta
        }
    }
}

/**
 * 快速构建 [LoreMetaLookup].
 */
private fun buildLoreMetaLookup(init: LoreMetaLookupBuilder.() -> Unit): LoreMetaLookup {
    return LoreMetaLookupBuilder().apply(init).build()
}

/**
 * 快速构建 [LoreMeta].
 */
private object LoreMetaBuilder {
    // 固定的空白内容行
    // 对于一个手动定义的 renderer config 来说,
    // 这里的 rawTooltipIndex 实际上没有作用.
    fun blankConstant(rawTooltipIndex: RawTooltipIndex, companionNamespace: String?): LoreMeta =
        BlankConstantLoreMeta(rawTooltipIndex, companionNamespace)

    // 固定的自定义内容行
    // 对于一个手动定义的 renderer config 来说,
    // 这里的 rawTooltipIndex 实际上没有作用.
    fun customConstant(rawTooltipIndex: RawTooltipIndex, companionNamespace: String, vararg text: String): LoreMeta =
        CustomConstantLoreMeta(rawTooltipIndex, companionNamespace, listText(*text))

    // 按照物品的数据动态生成的内容行
    fun dynamic(): LoreMeta =
        throw NotImplementedError() // 实际上不需要定义 dynamic lore meta, 因为这只是用来生成配置文件
}

@DslMarker
private annotation class LoreLineFlatterDsl