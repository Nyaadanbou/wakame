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
import cc.mewcraft.wakame.item.components.FireResistant
import cc.mewcraft.wakame.item.components.HideAdditionalTooltip
import cc.mewcraft.wakame.item.components.HideTooltip
import cc.mewcraft.wakame.item.components.cells.CoreTypes
import cc.mewcraft.wakame.item.components.cells.CurseTypes
import cc.mewcraft.wakame.item.components.cells.cores.attribute.element
import cc.mewcraft.wakame.item.components.cells.cores.empty.CoreEmpty
import cc.mewcraft.wakame.item.components.cells.cores.noop.CoreNoop
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
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
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
import kotlin.test.assertFalse
import kotlin.test.assertIs
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
        serialization { arrowTemplate ->
            assertNotNull(arrowTemplate)
            assertEquals(3, arrowTemplate.pierceLevel.calculate().toInt())
        }

        result { it ->
            assertFalse(it.isEmpty())
        }

        unboxed { itemArrow ->
            assertEquals(3, itemArrow.pierceLevel)
        }
    }

    @Test
    fun `component - attributable`() = componentLifecycleTest(
        "attributable", ItemTemplateTypes.ATTRIBUTABLE, ItemComponentTypes.ATTRIBUTABLE
    ) {
        serialization {
            assertNotNull(it)
        }

        result {
            assertFalse(it.isEmpty())
        }

        unboxed {
            assertEquals(Attributable.of(), it)
        }
    }

    // 针对 attribute_modifiers 写下一个 test
    @Test
    fun `component - attribute_modifiers`() = componentLifecycleTest(
        "attribute_modifiers", ItemTemplateTypes.ATTRIBUTE_MODIFIERS, ItemComponentTypes.ATTRIBUTE_MODIFIERS
    ) {
        serialization {
            assertNotNull(it)
        }

        result {
            assertFalse(it.isEmpty())
        }

        unboxed {
            assertFalse(it.showInTooltip)
        }
    }

    @Test
    fun `component - can_break`() = componentLifecycleTest(
        "can_break", ItemTemplateTypes.CAN_BREAK, ItemComponentTypes.CAN_BREAK
    ) {
        serialization {
            assertNotNull(it)
        }

        result {
            assertFalse(it.isEmpty())
        }

        unboxed {
            assertFalse(it.showInTooltip)
        }
    }

    @Test
    fun `component - can_place_on`() = componentLifecycleTest(
        "can_place_on", ItemTemplateTypes.CAN_PLACE_ON, ItemComponentTypes.CAN_PLACE_ON
    ) {
        serialization {
            assertNotNull(it)
        }

        result {
            assertFalse(it.isEmpty())
        }

        unboxed {
            assertFalse(it.showInTooltip)
        }
    }

    @Test
    fun `component - castable`() = componentLifecycleTest(
        "castable", ItemTemplateTypes.CASTABLE, ItemComponentTypes.CASTABLE
    ) {
        serialization {
            assertNotNull(it)
        }

        result {
            assertFalse(it.isEmpty())
        }

        unboxed {
            assertEquals(Castable.of(), it)
        }
    }

    @Test
    fun `component - cells simple`() = componentLifecycleTest(
        "cells_simple", ItemTemplateTypes.CELLS, ItemComponentTypes.CELLS
    ) {
        serialization {
            assertNotNull(it)
        }

        context {
            it.level = 10 // 预设物品等级为 10
        }

        result {
            assertFalse(it.isEmpty())
        }

        unboxed {
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
    fun `component - cells only noop or empty`() = componentLifecycleTest(
        "cells_only_noop_or_empty", ItemTemplateTypes.CELLS, ItemComponentTypes.CELLS
    ) {
        serialization {
            assertNotNull(it)
        }

        result {
            assertFalse(it.isEmpty())
        }

        unboxed {
            val cell = it.get("foo")
            assertNotNull(cell)
            assertAny(
                // 要么是 noop, 要么是 empty
                { assertIs<CoreNoop>(cell.getCore()) },
                { assertIs<CoreEmpty>(cell.getCore()) },
            )
        }
    }

    @Test
    fun `component - cells check_core_registrations`() = componentLifecycleTest(
        "cells_check_core_registrations", ItemTemplateTypes.CELLS, ItemComponentTypes.CELLS
    ) {
        serialization {
            assertNotNull(it)
        }

        context {
            it.level = 10
        }

        result {
            assertFalse(it.isEmpty())
        }

        unboxed {
            // 本 test 主要是用来检查 core 是否注册成功, 能否正常被 pool 调用
            val cell = it.get("foo")
            assertNotNull(cell)
            // CoreNoop 在第一个无条件的池中,
            // 因此生成出来的肯定是 CoreNoop
            assertIs<CoreNoop>(cell.getCore())
        }
    }

    @Test
    fun `component - cells check_curse_registrations`() = componentLifecycleTest(
        "cells_check_curse_registrations", ItemTemplateTypes.CELLS, ItemComponentTypes.CELLS
    ) {
        serialization {
            assertNotNull(it)
        }

        context {
            it.level = 10
        }

        result {
            assertFalse(it.isEmpty())
        }

        unboxed {
            // 本 test 主要是用来检查 core 是否注册成功, 能否正常被 pool 调用
        }
    }

    @Test
    fun `component - crate`() = componentLifecycleTest(
        "crate", ItemTemplateTypes.CRATE, ItemComponentTypes.CRATE
    ) {
        serialization {
            assertNotNull(it)
        }

        result {
            assertTrue(!it.isEmpty())
        }

        unboxed {
            assertEquals(Key.key("foo:bar"), it.key)
        }
    }


    @Test
    fun `component - custom_name`() = componentLifecycleTest(
        "custom_name", ItemTemplateTypes.CUSTOM_NAME, ItemComponentTypes.CUSTOM_NAME,
    ) {
        serialization {
            assertNotNull(it)
        }

        val common = RarityRegistry.INSTANCES["common"]
        context {
            it.rarity = common // 假设稀有度为 "common"
        }

        result {
            assertFalse(it.isEmpty())
        }

        unboxed {
            assertEquals(it.raw, "<!i><rarity:style>Foo")

            val expectedStyle = Style.style(*common.styles)
            val actualStyle = it.rich.style().edit {
                // 把 italic 显式设置为 false, 剩下的 style 应该跟稀有度的完全一致
                it.decoration(TextDecoration.ITALIC, TextDecoration.State.NOT_SET)
            }
            assertEquals(expectedStyle, actualStyle)
        }
    }

    @Test
    fun `component - custom_model_data`() {

    }

    @Test
    fun `component - damage`() {

    }

    @Test
    fun `component - dyed color`() {
        componentLifecycleTest(
            "dyed_color", ItemTemplateTypes.DYED_COLOR, ItemComponentTypes.DYED_COLOR,
        ) {
            serialization {
                assertNotNull(it)
            }

            result {
                assertFalse(it.isEmpty())
            }

            unboxed {
                assertFalse(it.showInTooltip)
                assertEquals(0xffffff, it.rgb)
            }
        }
    }

    @Test
    fun `component - damageable`() = componentLifecycleTest(
        "damageable", ItemTemplateTypes.DAMAGEABLE, ItemComponentTypes.DAMAGEABLE,
    ) {
        serialization {
            assertNotNull(it)
            assertTrue(it.disappearWhenBroken)
        }

        result {
            assertFalse(it.isEmpty())
        }

        unboxed {
            assertEquals(1, it.damage)
            assertEquals(512, it.maxDamage)
        }
    }

    @Test
    fun `component - max_damage`() {

    }

    @Test
    fun `component - elements`() = componentLifecycleTest(
        "elements", ItemTemplateTypes.ELEMENTS, ItemComponentTypes.ELEMENTS,
    ) {
        serialization {
            assertNotNull(it)
        }

        result {
            assertFalse(it.isEmpty())
        }

        unboxed {
            val elements = it.elements
            val possibleElements = setOf(
                ElementRegistry.INSTANCES["neutral"],
                ElementRegistry.INSTANCES["water"],
                ElementRegistry.INSTANCES["fire"],
                ElementRegistry.INSTANCES["wind"],
            )
            // assertEquals(2, elements.size) // FIXME 有时候2个,有时候1个
            assertTrue(elements.all { it in possibleElements })
        }
    }

    @Test
    fun `component - enchantments`() {
        val prototype = readPrototype("component", "enchantments")
        val template = prototype.templates.get(ItemTemplateTypes.ENCHANTMENTS)

        assertNotNull(template)
        assertFalse(template.showInTooltip)
        assertEquals(2, template.enchantments.size)
        assertEquals(1, template.enchantments[Key.key("sharpness")])
        assertEquals(2, template.enchantments[Key.key("knockback")])
    }

    @Test
    fun `component - fire_resistant`() = componentLifecycleTest(
        "fire_resistant", ItemTemplateTypes.FIRE_RESISTANT, ItemComponentTypes.FIRE_RESISTANT
    ) {
        serialization {
            assertNotNull(it)
        }

        result {
            assertFalse(it.isEmpty())
        }

        unboxed {
            assertEquals(FireResistant.of(), it)
        }
    }

    @Test
    fun `component - food`() = componentLifecycleTest(
        "food", ItemTemplateTypes.FOOD, ItemComponentTypes.FOOD,
    ) {
        serialization {
            assertNotNull(it)
        }

        result {
            assertFalse(it.isEmpty())
        }

        unboxed {
            assertEquals(5, it.nutrition)
            assertEquals(4.0f, it.saturation, 1e-5f)
            assertEquals(false, it.canAlwaysEat)
            assertEquals(3.2f, it.eatSeconds)

            // 测试环境没法初始化io.papermc.paper.registry.RegistryAccess, 因此这一块只能放到游戏里手动测试.
            /*
            // 测试 effects
            val effects = it.effects
            assertEquals(2, effects.size)
            // 检查第一个 effect
            effects[0].run {
                assertEquals(0.42f, this.probability, 1e-5f)
                val potionEffect = this.potionEffect
                assertEquals(PotionEffectType.SPEED, potionEffect.type)
                assertEquals(12, potionEffect.duration)
                assertEquals(4, potionEffect.amplifier)
                assertEquals(true, potionEffect.isAmbient)
                assertEquals(false, potionEffect.hasParticles())
                assertEquals(true, potionEffect.hasIcon())
            }
            // 检查第二个 effect
            effects[0].run {
                assertEquals(1f, this.probability, 1e-5f)
                val potionEffect = this.potionEffect
                assertEquals(PotionEffectType.LUCK, potionEffect.type)
                assertEquals(6, potionEffect.duration)
                assertEquals(2, potionEffect.amplifier)
                assertEquals(false, potionEffect.isAmbient)
                assertEquals(true, potionEffect.hasParticles())
                assertEquals(false, potionEffect.hasIcon())
            }
            */

            // 测试 skills
            val possibleSkills = setOf(
                Key.key("foo:bar/a"),
                Key.key("foo:bar/b")
            )
            assertTrue(it.skills.all { it in possibleSkills })
        }
    }

    @Test
    fun `component - hide_additional_tooltip`() = componentLifecycleTest(
        "hide_additional_tooltip", ItemTemplateTypes.HIDE_ADDITIONAL_TOOLTIP, ItemComponentTypes.HIDE_ADDITIONAL_TOOLTIP,
    ) {
        serialization {
            assertNotNull(it)
        }

        result {
            assertFalse(it.isEmpty())
        }

        unboxed {
            assertEquals(HideAdditionalTooltip.of(), it)
        }
    }

    @Test
    fun `component - hide_tooltip`() = componentLifecycleTest(
        "hide_tooltip", ItemTemplateTypes.HIDE_TOOLTIP, ItemComponentTypes.HIDE_TOOLTIP,
    ) {
        serialization {
            assertNotNull(it)
        }

        result {
            assertFalse(it.isEmpty())
        }

        unboxed {
            assertEquals(HideTooltip.of(), it)
        }
    }

    @Test
    fun `component - item_name`() = componentLifecycleTest(
        "item_name", ItemTemplateTypes.ITEM_NAME, ItemComponentTypes.ITEM_NAME,
    ) {
        serialization {
            assertNotNull(it)
        }

        val common = RarityRegistry.INSTANCES["common"]
        context {
            it.rarity = common // 假设稀有度为 "common"
        }

        result {
            assertFalse(it.isEmpty())
        }

        unboxed {
            assertEquals("<rarity:style><rarity:name>Foo", it.raw)

            val expectedStyle = Style.style(*common.styles)
            val actualStyle = it.rich.style().edit {
                // 把 italic 显式设置为 false, 剩下的 style 应该跟稀有度的完全一致
                it.decoration(TextDecoration.ITALIC, TextDecoration.State.NOT_SET)
            }
            assertEquals(expectedStyle, actualStyle)
        }
    }

    @Test
    fun `component - kizamiz`() = componentLifecycleTest(
        "kizamiz", ItemTemplateTypes.KIZAMIZ, ItemComponentTypes.KIZAMIZ,
    ) {
        serialization {
            assertNotNull(it)
        }

        val rarity = RarityRegistry.INSTANCES["rare"]
        context {
            it.rarity = rarity // 假设稀有度
        }

        unboxed {
            val kizamiz = it.kizamiz
            // assertEquals(2, kizamiz.size) // FIXME 有时候2个,有时候1个
            val possibleKizamiz = setOf(
                KizamiRegistry.INSTANCES["netherite"],
                KizamiRegistry.INSTANCES["luminite"],
            )
            assertTrue(kizamiz.all { it in possibleKizamiz })
        }
    }

    @Test
    fun `component - kizamiable`() = componentLifecycleTest(
        "kizamiable", ItemTemplateTypes.KIZAMIABLE, ItemComponentTypes.KIZAMIABLE,
    ) {
        serialization {
            assertNotNull(it)
        }

        result {
            assertFalse(it.isEmpty())
        }
    }

    @Test
    fun `component - level exact`() = componentLifecycleTest(
        "level_exact", ItemTemplateTypes.LEVEL, ItemComponentTypes.LEVEL,
    ) {
        // level 的生成基于其模板的具体设置.
        // 需要根据具体的设置, 执行对应的检查.

        serialization {
            assertNotNull(it)
        }

        bootstrap {
            createContext {
                MockGenerationContext.create(
                    Key.key("component:level_exact"),
                    GenerationTrigger.fake(1) // 故意设置源等级为 1
                )
            }
        }

        context {
            it.level = 2 // 上下文里的等级设置为 2
        }

        result {
            assertFalse(it.isEmpty())
        }

        unboxed {
            assertEquals(12, it.level)
        }
    }

    @Test
    fun `component - level context`() = componentLifecycleTest(
        "level_context", ItemTemplateTypes.LEVEL, ItemComponentTypes.LEVEL,
    ) {
        // level 的生成基于其模板的具体设置.
        // 需要根据具体的设置, 执行对应的检查.

        serialization {
            assertNotNull(it)
        }

        bootstrap {
            createContext {
                MockGenerationContext.create(
                    Key.key("component:level_context"),
                    GenerationTrigger.fake(4) // 设置源等级为 4, 如果一切正确, 最后的等级就是 4 级
                )
            }
        }

        result {
            assertFalse(it.isEmpty())
        }

        unboxed {
            assertEquals(4, it.level) // 因为等级是按照上下文生成, 所以应该跟上下文里的一样
        }
    }

    @Test
    fun `component - lore`() = componentLifecycleTest(
        "lore", ItemTemplateTypes.LORE, ItemComponentTypes.LORE,
    ) {
        serialization {
            assertNotNull(it)
        }
    }

    @Test
    fun `component - rarity`() = componentLifecycleTest(
        "rarity", ItemTemplateTypes.RARITY, ItemComponentTypes.RARITY,
    ) {
        serialization {
            assertNotNull(it)
        }

        context {
            it.level = 10
        }

        unboxed {

        }
    }

    @Test
    fun `component - skillful`() = componentLifecycleTest(
        "skillful", ItemTemplateTypes.SKILLFUL, ItemComponentTypes.SKILLFUL,
    ) {
        serialization {
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
    fun `component - stored_enchantments`() {
        val prototype = readPrototype("component", "stored_enchantments")
        val template = prototype.templates.get(ItemTemplateTypes.STORED_ENCHANTMENTS)

        assertNotNull(template)
        assertFalse(template.showInTooltip)
        assertEquals(2, template.enchantments.size)
        assertEquals(1, template.enchantments[Key.key("sharpness")])
        assertEquals(2, template.enchantments[Key.key("knockback")])
    }

    @Test
    fun `component - system_use`() {

    }

    @Test
    fun `component - tool`() = componentLifecycleTest(
        "tool", ItemTemplateTypes.TOOL, ItemComponentTypes.TOOL,
    ) {
        serialization {
            assertNotNull(it)
        }

        result {
            assertTrue(!it.isEmpty())
        }

        unboxed {
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
        val prototype = readPrototype("component", "trackable")
    }

    @Test
    fun `component - trim`() = componentLifecycleTest(
        "trim", ItemTemplateTypes.TRIM, ItemComponentTypes.TRIM,
    ) {
        serialization {
            assertNotNull(it)
        }

        result {
            assertFalse(it.isEmpty())
        }

        unboxed {
            assertFalse(it.showInTooltip)
        }
    }

    @Test
    fun `component - unbreakable`() = componentLifecycleTest(
        "unbreakable", ItemTemplateTypes.UNBREAKABLE, ItemComponentTypes.UNBREAKABLE,
    ) {
        serialization {
            assertNotNull(it)
        }

        result {
            assertFalse(it.isEmpty())
        }

        unboxed {
            assertTrue(it.showInTooltip)
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
    block: ComponentLifecycleTest.Lifecycle<T, S>.() -> Unit,
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
    private val lifecycle: LifecycleImpl<T, S> = LifecycleImpl()

    /**
     * 配置测试流程.
     */
    fun configure(block: Lifecycle<T, S>.() -> Unit) {
        block(lifecycle)
    }

    /**
     * 开始测试流程.
     */
    fun start() {
        val prototype = readPrototype("component", path)
        val nekoStack = MockNekoStack(prototype)
        val template = prototype.templates.get(templateType)

        // 执行对 ItemTemplate 反序列化的断言
        lifecycle.serializationBlock?.invoke(template)

        if (template == null) {
            return // 模板为空的话就不需要做接下来的测试了, 直接返回
        }

        // 如果有自定义 GenerationContext, 则使用自定义的; 如果没有, 则使用统一预设的
        val context = run {
            val contextCreator = lifecycle.bootstrap.contextCreator
            if (contextCreator != null) {
                contextCreator()
            } else {
                val generationTrigger = GenerationTrigger.fake(10)
                MockGenerationContext.create(prototype.key, generationTrigger)
            }
        }

        // 处理 GenerationContext
        lifecycle.generationContextBlock?.invoke(context)

        val generationResult = template.generate(context)

        // 处理 GenerationResult
        lifecycle.generationResultBlock?.invoke(generationResult)

        val generated = generationResult.value
        nekoStack.components.set(componentType, generated)
        nekoStack.components.get(componentType) ?: fail("Failed to get the component from the map")

        // 处理 GenerationResult 所封装的值
        lifecycle.unboxedBlock?.invoke(generated)

        logger.info("")
        logger.info(prototype.toString())
        logger.info("")
        logger.info(nekoStack.toString())
        logger.info("")
        logger.info(generated.toString())
    }

    interface Lifecycle<T, S : ItemTemplate<T>> {
        fun serialization(block: (S?) -> Unit)
        fun bootstrap(block: LifecycleBootstrap.() -> Unit)
        fun context(block: (GenerationContext) -> Unit)
        fun result(block: (GenerationResult<T>) -> Unit)
        fun unboxed(block: (T) -> Unit)
    }

    interface LifecycleBootstrap {
        fun createContext(block: () -> GenerationContext)
    }

    private class LifecycleImpl<T, S : ItemTemplate<T>> : Lifecycle<T, S> {
        var bootstrap: LifecycleBootstrapImpl = LifecycleBootstrapImpl()
        var generationContextBlock: ((GenerationContext) -> Unit)? = null
        var serializationBlock: ((S?) -> Unit)? = null
        var generationResultBlock: ((GenerationResult<T>) -> Unit)? = null
        var unboxedBlock: ((T) -> Unit)? = null

        override fun bootstrap(block: LifecycleBootstrap.() -> Unit) {
            block(bootstrap)
        }

        override fun serialization(block: (S?) -> Unit) {
            serializationBlock = block
        }

        override fun context(block: (GenerationContext) -> Unit) {
            generationContextBlock = block
        }

        override fun result(block: (GenerationResult<T>) -> Unit) {
            generationResultBlock = block
        }

        override fun unboxed(block: (T) -> Unit) {
            unboxedBlock = block
        }
    }

    private class LifecycleBootstrapImpl : LifecycleBootstrap {
        var contextCreator: (() -> GenerationContext)? = null
        override fun createContext(block: () -> GenerationContext) {
            contextCreator = block
        }
    }
}