package item

import assertAny
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.Attributable
import cc.mewcraft.wakame.item.components.Castable
import cc.mewcraft.wakame.item.components.FireResistant
import cc.mewcraft.wakame.item.components.HideAdditionalTooltip
import cc.mewcraft.wakame.item.components.HideTooltip
import cc.mewcraft.wakame.item.components.ItemGlowable
import cc.mewcraft.wakame.item.components.cells.CoreTypes
import cc.mewcraft.wakame.item.components.cells.CurseTypes
import cc.mewcraft.wakame.item.components.cells.cores.attribute.element
import cc.mewcraft.wakame.item.components.cells.cores.empty.CoreEmpty
import cc.mewcraft.wakame.item.components.cells.cores.noop.CoreNoop
import cc.mewcraft.wakame.item.template.GenerationTrigger
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.registry.EntityRegistry
import cc.mewcraft.wakame.registry.KizamiRegistry
import cc.mewcraft.wakame.registry.RarityRegistry
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.koin.test.KoinTest
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

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

        private val ZERO_UUID = UUID(0, 0)

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
    fun `unit - least configuration`() {
        val item = readCustomPrototype("unit", "least_configuration")
        assertEquals(UUID.fromString("8729823f-8b80-4efd-bb9e-1c0f9b2eecc3"), item.uuid)
        assertEquals(Key.key("wooden_sword"), item.itemType)
    }

    @Test
    fun `unit - remove_components`() {
        val item = readCustomPrototype("unit", "remove_components")
        val removeComponents = item.removeComponents
        assertTrue { removeComponents.has("attribute_modifiers") }
        assertTrue { removeComponents.has("food") }
        assertTrue { removeComponents.has("tool") }
    }

    @Test
    fun `unit - slot`() {
        val item = readCustomPrototype("unit", "slot")
        val slot = item.slot
        assertEquals("MAIN_HAND", slot.id())
    }
    //</editor-fold>

    //<editor-fold desc="Item Components">
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
    fun `component - bow`() = componentLifecycleTest(
        "bow", ItemTemplateTypes.BOW, ItemComponentTypes.BOW
    ) {
        serialization {
            assertNotNull(it)
        }

        result {
            assertFalse(it.isEmpty())
        }

        unboxed {
            assertEquals(Unit, it)
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
            val core = cell.getTypedCore(CoreTypes.ATTRIBUTE)
            assertNotNull(core)
            assertEquals(Key.key("attribute:attack_damage_rate"), core.key)
        }
    }

    // TODO 编写更多关于 random3 引用的测试

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
    fun `component - glowable`() = componentLifecycleTest(
        "glowable", ItemTemplateTypes.GLOWABLE, ItemComponentTypes.GLOWABLE,
    ) {
        serialization {
            assertNotNull(it)
        }

        result {
            assertFalse(it.isEmpty())
        }

        unboxed {
            assertEquals(ItemGlowable.of(), it)
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
    fun `component - trackable`() {
        val prototype = readCustomPrototype("component", "trackable")
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

    //<editor-fold desc="Use Cases">
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
