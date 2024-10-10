package item

import assertAny
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.attribute.composite.ConstantCompositeAttributeS
import cc.mewcraft.wakame.attribute.composite.element
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.*
import cc.mewcraft.wakame.item.components.cells.*
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.player.attackspeed.AttackSpeedLevel
import cc.mewcraft.wakame.registry.*
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.koin.test.KoinTest
import kotlin.test.*

class CustomNekoStackTest : KoinTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            CommonNekoStackTest.beforeAll()
        }

        @JvmStatic
        @AfterAll
        fun shutdown() {
            CommonNekoStackTest.afterAll()
        }

        private val ZERO_KEY = Key.key("noop:noop")

        private fun <T, S : ItemTemplate<T>> componentLifecycleTest(
            path: String,
            templateType: ItemTemplateType<S>,
            componentType: ItemComponentType<T>,
            block: ItemComponentLifecycleTest.Lifecycle<T, S>.() -> Unit,
        ) {
            val lifecycle = ItemComponentLifecycleTest("component", path, templateType, componentType)
            lifecycle.configure(block)
            lifecycle.start()
        }
    }

    //<editor-fold desc="Intrinsic Properties">
    @Test
    fun `intrinsics - base`() {
        val item = readCustomPrototype("intrinsics", "base")
        val base = item.base
        assertEquals(Material.BEDROCK, base.type)
        assertEquals("[!attribute_modifiers,!food,!tool]", base.format)
    }

    @Test
    fun `intrinsics - slot`() {
        val item = readCustomPrototype("intrinsics", "slot")
        val slotGroup = item.slotGroup
        assertTrue(slotGroup.contains(Key.key("vanilla:mainhand")), "mainhand")
    }

    @Test
    fun `intrinsics - slot list`() {
        val item = readCustomPrototype("intrinsics", "slot_list")
        val slotGroup = item.slotGroup
        assertTrue(slotGroup.contains(Key.key("custom:9")), "9")
        assertTrue(slotGroup.contains(Key.key("custom:10")), "10")
        assertTrue(slotGroup.contains(Key.key("custom:11")), "11")
        assertTrue(slotGroup.contains(Key.key("custom:12")), "12")
    }
    //</editor-fold>

    //<editor-fold desc="Item Components">
    @Test
    fun `component - arrow`() = componentLifecycleTest(
        "arrow", ItemTemplateTypes.ARROW, ItemComponentTypes.EMPTY
    ) {
        serialization { arrowTemplate ->
            assertNotNull(arrowTemplate)
            assertEquals(3, arrowTemplate.pierceLevel)
        }

        result {
            assertTrue(it.isEmpty())
        }
    }

    @Test
    fun `component - attack_speed`() = componentLifecycleTest(
        "attack_speed", ItemTemplateTypes.ATTACK_SPEED, ItemComponentTypes.ATTACK_SPEED
    ) {
        serialization {
            assertNotNull(it)
        }

        result {
            assertFalse(it.isEmpty())
        }

        unboxed {
            assertEquals(AttackSpeedLevel.NORMAL, it.level)
        }
    }

    @Test
    fun `component - attributable`() = componentLifecycleTest(
        "attributable", ItemTemplateTypes.ATTRIBUTABLE, ItemComponentTypes.EMPTY
    ) {
        serialization {
            assertNotNull(it)
        }

        result {
            assertTrue(it.isEmpty())
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
    fun `component - bow`() = componentLifecycleTest(
        "bow", ItemTemplateTypes.BOW, ItemComponentTypes.EMPTY
    ) {
        serialization {
            assertNotNull(it)
        }

        result {
            assertTrue(it.isEmpty())
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
        "castable", ItemTemplateTypes.CASTABLE, ItemComponentTypes.EMPTY
    ) {
        serialization {
            assertNotNull(it)
        }

        result {
            assertTrue(it.isEmpty())
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
                val core = cell.getCoreAs(CoreType.ATTRIBUTE)
                assertNotNull(core)

                fun assert(element: Element, expectedMin: Double, expectedMax: Double) {
                    val modMap = core.attribute.provideAttributeModifiers(ZERO_KEY)
                    val modMin = modMap[Attributes.element(element).MIN_ATTACK_DAMAGE]
                    val modMax = modMap[Attributes.element(element).MAX_ATTACK_DAMAGE]
                    assertNotNull(modMin)
                    assertNotNull(modMax)
                    assertEquals(expectedMin, modMin.amount)
                    assertEquals(expectedMax, modMax.amount)
                }

                val fire = ElementRegistry.INSTANCES["fire"]
                val water = ElementRegistry.INSTANCES["water"]
                when (val actual = core.attribute.element) {
                    fire -> assert(actual, 15.0, 20.0)
                    water -> assert(actual, 20.0, 25.0)
                }
            }

            // 词条栏: bonus
            run {
                val cell = it.get("bonus")
                assertNotNull(cell)

                // 测试核心
                val core = cell.getCoreAs(CoreType.ATTRIBUTE)
                assertNotNull(core)

                val modMap = core.attribute.provideAttributeModifiers(ZERO_KEY)
                val mod = modMap[Attributes.CRITICAL_STRIKE_CHANCE]
                assertNotNull(mod)
                assertEquals(0.75, mod.amount, 1e-5)
            }
        }
    }

    @Test
    fun `component - cells intrinsics filter`() = componentLifecycleTest(
        "cells_intrinsics_filters", ItemTemplateTypes.CELLS, ItemComponentTypes.CELLS
    ) {
        serialization {
            assertNotNull(it)
        }

        context {
            it.level = 1 // 预设物品等级为 1
        }

        result {
            assertFalse(it.isEmpty())
        }

        unboxed {
            assertEquals(2, it.size)
            val cell1 = assertNotNull(it.get("foo_1"))
            val core1 = assertIs<ConstantCompositeAttributeS>(cell1.getCoreAs(CoreType.ATTRIBUTE)?.attribute)
            assertEquals(5.0, core1.value)
            val cell2 = assertNotNull(it.get("foo_2"))
            val core2 = assertIs<EmptyCore>(cell2.getCore())
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
                { assertIs<VirtualCore>(cell.getCore()) },
                { assertIs<EmptyCore>(cell.getCore()) },
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
            assertIs<VirtualCore>(cell.getCore())
        }
    }

    @Test
    fun `component - cells check references 1`() = componentLifecycleTest(
        "cells_references_1", ItemTemplateTypes.CELLS, ItemComponentTypes.CELLS
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
            val cell = it.get("foo")
            assertNotNull(cell)
            val core = cell.getCoreAs(CoreType.ATTRIBUTE)
            assertNotNull(core)
            assertEquals(Key.key("attribute:attack_damage_rate"), core.id)
        }
    }

    @Test
    fun `component - cells check references 2`() = componentLifecycleTest(
        "cells_references_2", ItemTemplateTypes.CELLS, ItemComponentTypes.CELLS
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
            val cell = it.get("foo_b")
            assertNotNull(cell)
            val core = cell.getCoreAs(CoreType.ATTRIBUTE)
            assertNotNull(core)
            assertAny(
                { assertEquals(Key.key("attribute:critical_strike_chance"), core.id) },
                { assertEquals(Key.key("attribute:critical_strike_power"), core.id) }
            )
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
            assertEquals("foo", it.identity)
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
            assertEquals(2, elements.size)
            assertTrue(elements.all { it in possibleElements })
        }
    }

    @Test
    fun `component - enchantments`() {
        val prototype = readCustomPrototype("component", "enchantments")
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
            assertEquals(FireResistant.instance(), it)
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
    fun `component - glowable`() = componentLifecycleTest(
        "glowable", ItemTemplateTypes.GLOWABLE, ItemComponentTypes.EMPTY,
    ) {
        serialization {
            assertNotNull(it)
        }

        result {
            assertTrue(it.isEmpty())
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
            assertEquals(HideAdditionalTooltip.instance(), it)
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
            assertEquals(HideTooltip.instance(), it)
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
            assertEquals(2, kizamiz.size)
            val possibleKizamiz = setOf(
                KizamiRegistry.INSTANCES["wind_lace"],
                KizamiRegistry.INSTANCES["antigravity"],
            )
            assertTrue(kizamiz.all { it in possibleKizamiz })
        }
    }

    @Test
    fun `component - kizamiable`() = componentLifecycleTest(
        "kizamiable", ItemTemplateTypes.KIZAMIABLE, ItemComponentTypes.EMPTY,
    ) {
        serialization {
            assertNotNull(it)
        }

        result {
            assertTrue(it.isEmpty())
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
                    ItemGenerationTriggers.direct(1) // 故意设置源等级为 1
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
                    ItemGenerationTriggers.direct(4) // 设置源等级为 4, 如果一切正确, 最后的等级就是 4 级
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
    fun `component - portable core`() = componentLifecycleTest(
        "portable_core", ItemTemplateTypes.PORTABLE_CORE, ItemComponentTypes.PORTABLE_CORE
    ) {
        serialization {
            assertNotNull(it)
        }

        context {
            it.level = 10 // 单元测试一次只读取/生成一个模板, 因此这里得手动设置等级
        }

        unboxed {
            val core = it.wrapped
            assertNotNull(core)
            assertEquals(Key.key("attribute:attack_damage_rate"), core.id)
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
        "skillful", ItemTemplateTypes.SKILLFUL, ItemComponentTypes.EMPTY,
    ) {
        serialization {
            assertNotNull(it)
        }

        result {
            assertTrue(it.isEmpty())
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
        val prototype = readCustomPrototype("component", "stored_enchantments")
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
    fun `component - tracks`() {
        val prototype = readCustomPrototype("component", "tracks")
    }

    // 序列化会加载 RegistryAccess, 因此无法在测试环境中执行
    /* @Test
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
    } */

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

    //<editor-fold desc="Use Cases">
    @Test
    fun `use case - least configuration`() {
        val item = readCustomPrototype("use_case", "least_configuration")
        assertEquals(Material.WOODEN_SWORD, item.base.type)
    }

    @Test
    fun `use case - apple without food`() {
        val prototype = readCustomPrototype("use_case", "apple_without_food")
    }

    @Test
    fun `use case - pickaxe without tool`() {
        val prototype = readCustomPrototype("use_case", "pickaxe_without_tool")
    }

    @Test
    fun `use case - simple material`() {
        val prototype = readCustomPrototype("use_case", "simple_material")
    }

    @Test
    fun `use case - sword without attribute modifiers`() {
        val prototype = readCustomPrototype("use_case", "sword_without_attribute_modifiers")
    }
    //</editor-fold>
}
