import cc.mewcraft.nbt.ByteArrayTag
import cc.mewcraft.nbt.ByteTag
import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.nbt.DoubleTag
import cc.mewcraft.nbt.EndTag
import cc.mewcraft.nbt.FloatTag
import cc.mewcraft.nbt.IntArrayTag
import cc.mewcraft.nbt.IntTag
import cc.mewcraft.nbt.ListTag
import cc.mewcraft.nbt.LongArrayTag
import cc.mewcraft.nbt.LongTag
import cc.mewcraft.nbt.ShortTag
import cc.mewcraft.nbt.StringTag
import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.adventure.adventureModule
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.element.elementModule
import cc.mewcraft.wakame.entity.entityModule
import cc.mewcraft.wakame.item.NekoItem
import cc.mewcraft.wakame.item.NekoItemFactory
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.Attributable
import cc.mewcraft.wakame.item.components.Castable
import cc.mewcraft.wakame.item.components.cells.CoreTypes
import cc.mewcraft.wakame.item.components.cells.CurseTypes
import cc.mewcraft.wakame.item.components.cells.cores.attribute.element
import cc.mewcraft.wakame.item.itemModule
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.GenerationTrigger
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.kizami.kizamiModule
import cc.mewcraft.wakame.level.levelModule
import cc.mewcraft.wakame.molang.molangModule
import cc.mewcraft.wakame.rarity.rarityModule
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.registry.EntityRegistry
import cc.mewcraft.wakame.registry.ITEM_PROTO_CONFIG_LOADER
import cc.mewcraft.wakame.registry.KizamiRegistry
import cc.mewcraft.wakame.registry.LevelMappingRegistry
import cc.mewcraft.wakame.registry.RarityRegistry
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.registry.registryModule
import cc.mewcraft.wakame.skill.skillModule
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import it.unimi.dsi.fastutil.longs.LongSet
import item.MockGenerationContext
import item.MockNekoStack
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject
import org.slf4j.Logger
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

class ItemTest2 : KoinTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            // 配置依赖注入
            startKoin {
                // environment
                modules(
                    testEnvironment()
                )

                // this module
                modules(
                    itemModule()
                )

                // dependencies
                modules(
                    adventureModule(),
                    elementModule(),
                    entityModule(),
                    kizamiModule(),
                    levelModule(),
                    molangModule(),
                    rarityModule(),
                    registryModule(),
                    skillModule(),
                )
            }

            // 按依赖顺序, 初始化注册表
            AttributeRegistry.onPreWorld()
            ElementRegistry.onPreWorld()
            SkillRegistry.onPreWorld()
            KizamiRegistry.onPreWorld()
            RarityRegistry.onPreWorld()
            LevelMappingRegistry.onPreWorld()
            EntityRegistry.onPreWorld()
        }

        @JvmStatic
        @AfterAll
        fun shutdown() {
            unmockStaticNBT()
            stopKoin()
        }
    }

    //<editor-fold desc="Units">
    @Test
    fun `unit - hide additional tooltip`() {
        val item = readPrototype("unit", "hide_additional_tooltip")
        assertTrue { item.hideAdditionalTooltip }
    }

    @Test
    fun `unit - hide tooltip`() {
        val item = readPrototype("unit", "hide_tooltip")
        assertTrue { item.hideTooltip }
    }

    @Test
    fun `unit - least configuration`() {
        val item = readPrototype("unit", "least_configuration")
        assertEquals(UUID.fromString("8729823f-8b80-4efd-bb9e-1c0f9b2eecc3"), item.uuid)
        assertEquals(Key.key("wooden_sword"), item.itemType)
    }

    @Test
    fun `unit - remove_components`() {
        val item = readPrototype("unit", "remove_components")
        val removeComponents = item.removeComponents
        assertTrue { removeComponents.has("attribute_modifiers") }
        assertTrue { removeComponents.has("food") }
        assertTrue { removeComponents.has("tool") }
    }

    @Test
    fun `unit - shown in tooltip 1`() {
        val item = readPrototype("unit", "shown_in_tooltip_1")
        val shownInTooltip = item.shownInTooltip
        assertTrue { shownInTooltip.isPresent("trim") && shownInTooltip.shouldHide("trim") }
        assertTrue { shownInTooltip.isPresent("attribute_modifiers") && shownInTooltip.shouldHide("attribute_modifiers") }
        assertTrue { shownInTooltip.isPresent("can_break") && shownInTooltip.shouldHide("can_break") }
        assertTrue { shownInTooltip.isPresent("dyed_color") && shownInTooltip.shouldHide("dyed_color") }
        assertTrue { shownInTooltip.isPresent("enchantments") && shownInTooltip.shouldHide("enchantments") }
        assertTrue { shownInTooltip.isPresent("can_place_on") && shownInTooltip.shouldHide("can_place_on") }
        assertTrue { shownInTooltip.isPresent("stored_enchantments") && shownInTooltip.shouldHide("stored_enchantments") }
        assertTrue { shownInTooltip.isPresent("unbreakable") && shownInTooltip.shouldHide("unbreakable") }
    }

    @Test
    fun `unit - shown in tooltip 2`() {
        val item = readPrototype("unit", "shown_in_tooltip_2")
        val shownInTooltip = item.shownInTooltip
        assertTrue { shownInTooltip.isPresent("trim") && shownInTooltip.shouldShow("trim") }
        assertTrue { shownInTooltip.isPresent("attribute_modifiers") && shownInTooltip.shouldHide("attribute_modifiers") }
        assertTrue { shownInTooltip.isPresent("can_break") && shownInTooltip.shouldShow("can_break") }
        assertTrue { shownInTooltip.isPresent("dyed_color") && shownInTooltip.shouldHide("dyed_color") }
        assertTrue { shownInTooltip.isPresent("enchantments") && shownInTooltip.shouldShow("enchantments") }
        assertTrue { shownInTooltip.isPresent("can_place_on") && shownInTooltip.shouldHide("can_place_on") }
        assertTrue { shownInTooltip.isPresent("stored_enchantments") && shownInTooltip.shouldShow("stored_enchantments") }
        assertTrue { shownInTooltip.isPresent("unbreakable") && shownInTooltip.shouldHide("unbreakable") }
    }

    @Test
    fun `unit - shown in tooltip 3`() {
        val item = readPrototype("unit", "shown_in_tooltip_3")
        val shownInTooltip = item.shownInTooltip
        assertTrue { shownInTooltip.isPresent("trim") && shownInTooltip.shouldShow("trim") }
        assertTrue { !shownInTooltip.isPresent("attribute_modifiers") && shownInTooltip.shouldHide("attribute_modifiers") }
        assertTrue { shownInTooltip.isPresent("can_break") && shownInTooltip.shouldShow("can_break") }
        assertTrue { !shownInTooltip.isPresent("dyed_color") && shownInTooltip.shouldHide("dyed_color") }
        assertTrue { shownInTooltip.isPresent("enchantments") && shownInTooltip.shouldShow("enchantments") }
        assertTrue { !shownInTooltip.isPresent("can_place_on") && shownInTooltip.shouldHide("can_place_on") }
        assertTrue { shownInTooltip.isPresent("stored_enchantments") && shownInTooltip.shouldShow("stored_enchantments") }
        assertTrue { !shownInTooltip.isPresent("unbreakable") && shownInTooltip.shouldHide("unbreakable") }
    }

    @Test
    fun `unit - slot`() {
        val item = readPrototype("unit", "slot")
        val slot = item.slot
        assertEquals("MAIN_HAND", slot.id())
    }
    //</editor-fold>

    //<editor-fold desc="Components">
    @Test
    fun `component - arrow`() = componentLifecycleTest(
        "arrow", ItemTemplateTypes.ARROW, ItemComponentTypes.ARROW
    ) {
        handleSerialization { arrowTemplate ->
            assertNotNull(arrowTemplate)
            assertEquals(3, arrowTemplate.pierceLevel.calculate().toInt())
        }

        handleGenerationResult { generationResult ->
            assertTrue { !generationResult.isEmpty() }
        }

        handleGenerated { itemArrow ->
            assertEquals(3, itemArrow.pierceLevel)
        }
    }

    @Test
    fun `component - attributable`() = componentLifecycleTest(
        "attributable", ItemTemplateTypes.ATTRIBUTABLE, ItemComponentTypes.ATTRIBUTABLE
    ) {
        handleSerialization {
            assertNotNull(it)
        }

        handleGenerationResult {
            assertTrue { !it.isEmpty() }
        }

        handleGenerated {
            assertTrue { it == Attributable.of() }
        }
    }

    @Test
    fun `component - castable`() = componentLifecycleTest(
        "castable", ItemTemplateTypes.CASTABLE, ItemComponentTypes.CASTABLE
    ) {
        handleSerialization {
            assertNotNull(it)
        }

        handleGenerationResult {
            assertTrue { !it.isEmpty() }
        }

        handleGenerated {
            assertTrue { it == Castable.of() }
        }
    }

    @Test
    fun `component - cells simple`() = componentLifecycleTest(
        "cells_simple", ItemTemplateTypes.CELLS, ItemComponentTypes.CELLS
    ) {
        handleGenerationContext {
            it.level = 10 // 预设物品等级为 10
        }

        handleSerialization {
            assertNotNull(it)
        }

        handleGenerationResult {
            assertTrue(!it.isEmpty())
        }

        handleGenerated {
            // 词条栏: attack
            run {
                val cell = it.get("attack")
                assertNotNull(cell)

                // 测试核心
                val core = cell.getTypedCore(CoreTypes.ATTRIBUTE)
                assertNotNull(core)

                fun assert(element: Element, expectedMin: Double, expectedMax: Double) {
                    val modMap = core.provideAttributeModifiers(ZERO_UUID)
                    val modMin = modMap[Attributes.byElement(element).MIN_ATTACK_DAMAGE]
                    val modMax = modMap[Attributes.byElement(element).MAX_ATTACK_DAMAGE]
                    assertNotNull(modMin)
                    assertNotNull(modMax)
                    assertEquals(expectedMin, modMin.amount)
                    assertEquals(expectedMax, modMax.amount)
                }

                val fire = ElementRegistry.INSTANCES["fire"]
                val water = ElementRegistry.INSTANCES["water"]
                when (val actual = core.element) {
                    fire -> assert(actual, 15.0, 20.0)
                    water -> assert(actual, 20.0, 25.0)
                }
            }

            // 词条栏: bonus
            run {
                val cell = it.get("bonus")
                assertNotNull(cell)

                // 测试核心
                val core = cell.getTypedCore(CoreTypes.ATTRIBUTE)
                assertNotNull(core)

                val modMap = core.provideAttributeModifiers(ZERO_UUID)
                val mod = modMap[Attributes.CRITICAL_STRIKE_CHANCE]
                assertNotNull(mod)
                assertEquals(0.75, mod.amount, 1e-5)

                // 测试诅咒
                val curseEntityKills = cell.getTypedCurse(CurseTypes.ENTITY_KILLS)
                assertNotNull(curseEntityKills)
                assertEquals(3, curseEntityKills.count)

                val expectedIndex = EntityRegistry.TYPES["demo_creeps_1"]
                val actualIndex = curseEntityKills.index
                assertEquals(expectedIndex, actualIndex)
            }
        }
    }

    @Test
    fun `component - cells only empty`() = componentLifecycleTest(
        "cells_only_empty", ItemTemplateTypes.CELLS, ItemComponentTypes.CELLS
    ) {
        handleSerialization {
            assertNotNull(it)
        }
    }

    @Test
    fun `component - cells check_core_registrations`() {
        componentLifecycleTest(
            "cells_check_core_registrations", ItemTemplateTypes.CELLS, ItemComponentTypes.CELLS
        ) {
            handleGenerationContext {
                it.level = 10
            }

            handleSerialization {
                assertNotNull(it)
            }
        }
    }

    @Test
    fun `component - cells check_curse_registrations`() = componentLifecycleTest(
        "cells_check_curse_registrations", ItemTemplateTypes.CELLS, ItemComponentTypes.CELLS
    ) {
        handleGenerationContext {
            it.level = 10
        }

        handleSerialization {
            assertNotNull(it)
        }
    }

    @Test
    fun `component - crate`() {

    }

    @Test
    fun `component - custom_name`() = componentLifecycleTest(
        "custom_name", ItemTemplateTypes.CUSTOM_NAME, ItemComponentTypes.CUSTOM_NAME,
    ) {
        handleSerialization {
            assertNotNull(it)
        }
    }

    @Test
    fun `component - custom_model_data`() {

    }

    @Test
    fun `component - damage`() {

    }

    @Test
    fun `component - damageable`() = componentLifecycleTest(
        "damageable", ItemTemplateTypes.DAMAGEABLE, ItemComponentTypes.DAMAGEABLE,
    ) {
        handleSerialization {
            assertNotNull(it)
        }
    }

    @Test
    fun `component - max_damage`() {

    }

    @Test
    fun `component - elements`() = componentLifecycleTest(
        "elements", ItemTemplateTypes.ELEMENTS, ItemComponentTypes.ELEMENTS,
    ) {
        handleSerialization {
            assertNotNull(it)
        }
    }

    @Test
    fun `component - fire_resistant`() = componentLifecycleTest(
        "fire_resistant", ItemTemplateTypes.FIRE_RESISTANT, ItemComponentTypes.FIRE_RESISTANT
    ) {
        handleSerialization {
            assertNotNull(it)
        }
    }

    @Test
    fun `component - food`() = componentLifecycleTest(
        "food", ItemTemplateTypes.FOOD, ItemComponentTypes.FOOD,
    ) {
        handleSerialization {
            assertNotNull(it)
        }
    }

    @Test
    fun `component - item_name`() = componentLifecycleTest(
        "item_name", ItemTemplateTypes.ITEM_NAME, ItemComponentTypes.ITEM_NAME,
    ) {
        handleSerialization {
            assertNotNull(it)
        }
    }

    @Test
    fun `component - kizamiz`() = componentLifecycleTest(
        "kizamiz", ItemTemplateTypes.KIZAMIZ, ItemComponentTypes.KIZAMIZ,
    ) {
        handleGenerationContext {
            it.rarity = RarityRegistry.INSTANCES["rare"]
        }

        handleSerialization {
            assertNotNull(it)
        }

        handleGenerated {

        }
    }

    @Test
    fun `component - kizamiable`() = componentLifecycleTest(
        "kizamiable", ItemTemplateTypes.KIZAMIABLE, ItemComponentTypes.KIZAMIABLE,
    ) {
        handleSerialization {
            assertNotNull(it)
        }
    }

    @Test
    fun `component - level`() = componentLifecycleTest(
        "level", ItemTemplateTypes.LEVEL, ItemComponentTypes.LEVEL,
    ) {
        handleSerialization {
            assertNotNull(it)
        }
    }

    @Test
    fun `component - lore`() = componentLifecycleTest(
        "lore", ItemTemplateTypes.LORE, ItemComponentTypes.LORE,
    ) {
        handleSerialization {
            assertNotNull(it)
        }
    }

    @Test
    fun `component - rarity`() = componentLifecycleTest(
        "rarity", ItemTemplateTypes.RARITY, ItemComponentTypes.RARITY,
    ) {
        handleGenerationContext {
            it.level = 10
        }

        handleSerialization {
            assertNotNull(it)
        }

        handleGenerated {

        }
    }

    @Test
    fun `component - skillful`() = componentLifecycleTest(
        "skillful", ItemTemplateTypes.SKILLFUL, ItemComponentTypes.SKILLFUL,
    ) {
        handleSerialization {
            assertNotNull(it)
        }
    }

    @Test
    fun `component - skin`() {

    }

    @Test
    fun `component - skin_owner`() {

    }

    @Test
    fun `component - system_use`() {

    }

    @Test
    fun `component - tool`() = componentLifecycleTest(
        "tool",
        ItemTemplateTypes.TOOL,
        ItemComponentTypes.TOOL,
    ) {
        handleSerialization {
            assertNotNull(it)
        }

        handleGenerationResult {
            assertTrue(!it.isEmpty())
        }

        handleGenerated {
            assertEquals(2.1f, it.defaultMiningSpeed, 1e-5f)
            assertEquals(4, it.damagePerBlock)
            val rules = it.rules
            assertNotNull(rules.elementAtOrNull(0))
            assertTrue(rules[0].blockTypes.containsAll(setOf(Material.DIRT, Material.STONE)))
            assertEquals(4.0f, rules[0].speed)
            assertTrue(rules[0].correctForDrops.toBooleanOrElse(false))
        }
    }

    @Test
    fun `component - trackable`() {

    }

    @Test
    fun `component - unbreakable`() = componentLifecycleTest(
        "unbreakable",
        ItemTemplateTypes.UNBREAKABLE,
        ItemComponentTypes.UNBREAKABLE,
    ) {
        handleSerialization {
            assertNotNull(it)
        }
    }
    //</editor-fold>

    //<editor-fold desc="Use cases">
    @Test
    fun `use case - apple without food`() {
        val prototype = readPrototype("use_case", "apple_without_food")
    }

    @Test
    fun `use case - pickaxe without tool`() {
        val prototype = readPrototype("use_case", "pickaxe_without_tool")
    }

    @Test
    fun `use case - simple material`() {
        val prototype = readPrototype("use_case", "simple_material")
    }

    @Test
    fun `use case - sword without attribute modifiers`() {
        val prototype = readPrototype("use_case", "sword_without_attribute_modifiers")
    }
    //</editor-fold>
}

fun assertAny(vararg assertions: () -> Unit) {
    val errors = mutableListOf<Throwable>()
    for (assertion in assertions) {
        try {
            assertion()
            return // If any assertion succeeds, return without error.
        } catch (e: Throwable) {
            errors.add(e)
        }
    }
    // If all assertions fail, throw an exception containing all error messages.
    val message = errors.joinToString(separator = "\n") { it.message ?: "Unknown error" }
    fail("All assertions failed:\n$message")
}

private fun mockStaticNBT() {
    mockkStatic(
        ByteArrayTag::class, ByteTag::class, CompoundTag::class, DoubleTag::class, EndTag::class, FloatTag::class,
        IntArrayTag::class, IntTag::class, ListTag::class, LongArrayTag::class, LongTag::class, ShortTag::class,
        StringTag::class,
    )

    mockk<ByteArrayTag>().let {
        every { ByteArrayTag.create(any<ByteArray>()) } returns it
        every { ByteArrayTag.create(any<List<Byte>>()) } returns it
    }

    mockk<ByteTag>().let {
        every { ByteTag.valueOf(any<Boolean>()) } returns it
        every { ByteTag.valueOf(any<Byte>()) } returns it
    }

    mockk<CompoundTag>().let {
        every { CompoundTag.create() } returns it
    }

    mockk<DoubleTag>().let {
        every { DoubleTag.valueOf(any()) } returns it
    }

    mockk<EndTag>().let {
        every { EndTag.instance() } returns it
    }

    mockk<FloatTag>().let {
        every { FloatTag.valueOf(any()) } returns it
    }

    mockk<IntArrayTag>().let {
        every { IntArrayTag.create(any<IntArray>()) } returns it
        every { IntArrayTag.create(any<List<Int>>()) } returns it
    }

    mockk<IntTag>().let {
        every { IntTag.valueOf(any()) } returns it
    }

    mockk<ListTag>().let {
        every { ListTag.create() } returns it
        every { ListTag.create(any(), any()) } returns it
    }

    mockk<LongArrayTag>().let {
        every { LongArrayTag.create(any<LongArray>()) } returns it
        every { LongArrayTag.create(any<LongSet>()) } returns it
        every { LongArrayTag.create(any<List<Long>>()) } returns it
    }

    mockk<LongTag>().let {
        every { LongTag.valueOf(any()) } returns it
    }

    mockk<ShortTag>().let {
        every { ShortTag.valueOf(any()) } returns it
    }

    mockk<StringTag>().let {
        every { StringTag.valueOf(any()) } returns it
    }
}

private fun unmockStaticNBT() {
    unmockkStatic(
        ByteArrayTag::class, ByteTag::class, CompoundTag::class, DoubleTag::class, EndTag::class, FloatTag::class,
        IntArrayTag::class, IntTag::class, ListTag::class, LongArrayTag::class, LongTag::class, ShortTag::class,
        StringTag::class,
    )
}

private val ZERO_UUID = UUID(0, 0)

/**
 * 从指定的文件读取 [NekoItem].
 */
private fun KoinTest.readPrototype(namespace: String, path: String): NekoItem {
    val pluginDataDir = get<File>(named(PLUGIN_DATA_DIR))
    val itemsDir = pluginDataDir.resolve("items")
    val namespaceDir = itemsDir.resolve(namespace)
    val itemFile = namespaceDir.resolve("$path.yml")
    if (!itemFile.exists()) {
        fail("File not found: $namespace:$path")
    }

    val key = Key.key(namespace, path)
    val relPath = itemFile.toPath()
    val loaderBuilder = get<YamlConfigurationLoader.Builder>(named(ITEM_PROTO_CONFIG_LOADER)) // will be reused
    val node = loaderBuilder.buildAndLoadString(itemFile.readText())
    return NekoItemFactory.create(key, relPath, node)
}

/**
 * 开始一个物品组件的标准测试流程.
 */
private fun <T, S : ItemTemplate<T>> componentLifecycleTest(
    path: String,
    templateType: ItemTemplateType<S>,
    componentType: ItemComponentType<T>,
    block: ComponentLifecycleTest<T, S>.DSL.() -> Unit,
) {
    val lifecycle = ComponentLifecycleTest(path, templateType, componentType)
    lifecycle.configure(block)
    lifecycle.start()
}

/**
 * 一个适用于任何物品组件的测试流程.
 */
private class ComponentLifecycleTest<T, S : ItemTemplate<T>>(
    val path: String,
    val templateType: ItemTemplateType<S>,
    val componentType: ItemComponentType<T>,
) : KoinTest {

    private val logger: Logger by inject()

    // 生成过程的上下文
    private var handlerGenerationContext: ((GenerationContext) -> Unit)? = null

    // 模板配置文件的序列化
    private var handlerSerialization: ((S?) -> Unit)? = null

    // 模板生成的结果
    private var handlerGenerationResult: ((GenerationResult<T>) -> Unit)? = null

    // 模板生成的结果所包含的值
    private var handlerGenerated: ((T) -> Unit)? = null

    /**
     * 配置测试流程.
     */
    fun configure(block: DSL.() -> Unit) {
        block(DSL())
    }

    /**
     * 开始测试流程.
     */
    fun start() {
        val prototype = readPrototype("component", path)
        val nekoStack = MockNekoStack(prototype)
        val template = prototype.templates.get(templateType)

        handlerSerialization?.invoke(template)

        if (template == null) {
            return // 模板为空的话就不需要做接下来的测试了, 直接返回
        }

        val generationTrigger = GenerationTrigger.fake(10)
        val generationContext = MockGenerationContext.create(prototype, generationTrigger)

        handlerGenerationContext?.invoke(generationContext)

        val generationResult = template.generate(generationContext)

        handlerGenerationResult?.invoke(generationResult)

        val generated = generationResult.value
        nekoStack.components.set(componentType, generated)
        nekoStack.components.get(componentType) ?: fail("Failed to get the component from the map")

        handlerGenerated?.invoke(generated)

        logger.info("")
        logger.info(prototype.toString())
        logger.info("")
        logger.info(nekoStack.toString())
        logger.info("")
        logger.info(generated.toString())
    }

    inner class DSL {
        fun handleGenerationContext(block: (GenerationContext) -> Unit) {
            this@ComponentLifecycleTest.handlerGenerationContext = block
        }

        fun handleSerialization(block: (S?) -> Unit) {
            this@ComponentLifecycleTest.handlerSerialization = block
        }

        fun handleGenerationResult(block: (GenerationResult<T>) -> Unit) {
            this@ComponentLifecycleTest.handlerGenerationResult = block
        }

        fun handleGenerated(block: (T) -> Unit) {
            this@ComponentLifecycleTest.handlerGenerated = block
        }
    }
}
