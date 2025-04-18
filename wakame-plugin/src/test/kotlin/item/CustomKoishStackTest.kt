@file:Suppress("UnstableApiUsage")

package item

import assertAny
import cc.mewcraft.wakame.attack.HandAttack
import cc.mewcraft.wakame.attack.SpearAttack
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.entity.attribute.Attributes
import cc.mewcraft.wakame.entity.attribute.bundle.ConstantAttributeBundleS
import cc.mewcraft.wakame.entity.attribute.bundle.element
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.HideAdditionalTooltip
import cc.mewcraft.wakame.item.components.HideTooltip
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import cc.mewcraft.wakame.item.components.cells.EmptyCore
import cc.mewcraft.wakame.item.components.cells.VirtualCore
import cc.mewcraft.wakame.item.template.ItemGenerationTriggers
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.world.TimeControl
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.tag.TagKey
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.koin.test.KoinTest
import kotlin.test.*

class CustomKoishStackTest : KoinTest {
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
            val lifecycle = ItemComponentLifecycleTest("data", path, templateType, componentType)
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
    fun `component - attack`() = componentLifecycleTest(
        "attack", ItemTemplateTypes.ATTACK, ItemComponentTypes.EMPTY,
    ) {
        serialization {
            assertNotNull(it)
            assertIs<SpearAttack>(it.attackType)
        }

        result {
            assertTrue(it.isEmpty())
        }
    }

    @Test
    fun `component - attack_empty`() = componentLifecycleTest(
        "attack_empty", ItemTemplateTypes.ATTACK, ItemComponentTypes.EMPTY
    ) {
        serialization {
            assertNotNull(it)
            assertIs<HandAttack>(it.attackType)
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
            // TODO: finish it
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
            // 核孔: attack
            run {
                val cell = it.get("attack")
                assertNotNull(cell)

                // 测试核心
                val core = cell.core as? AttributeCore
                assertNotNull(core)

                fun assert(element: RegistryEntry<Element>, expectedMin: Double, expectedMax: Double) {
                    val modMap = core.data.createAttributeModifiers(ZERO_KEY)
                    val modMin = modMap[Attributes.MIN_ATTACK_DAMAGE.of(element)]
                    val modMax = modMap[Attributes.MAX_ATTACK_DAMAGE.of(element)]
                    assertNotNull(modMin)
                    assertNotNull(modMax)
                    assertEquals(expectedMin, modMin.amount)
                    assertEquals(expectedMax, modMax.amount)
                }

                val fire = BuiltInRegistries.ELEMENT.getEntryOrThrow("fire")
                val water = BuiltInRegistries.ELEMENT.getEntryOrThrow("water")
                when (val actual = core.data.element) {
                    fire -> assert(actual, 15.0, 20.0)
                    water -> assert(actual, 20.0, 25.0)
                }
            }

            // 核孔: bonus
            run {
                val cell = it.get("bonus")
                assertNotNull(cell)

                // 测试核心
                val core = cell.core as? AttributeCore
                assertNotNull(core)

                val modMap = core.data.createAttributeModifiers(ZERO_KEY)
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
            val core1 = assertIs<ConstantAttributeBundleS>((cell1.core as? AttributeCore)?.data)
            assertEquals(5.0, core1.value)
            val cell2 = assertNotNull(it.get("foo_2"))
            val core2 = assertIs<EmptyCore>(cell2.core)
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
                { assertIs<VirtualCore>(cell.core) },
                { assertIs<EmptyCore>(cell.core) },
            )
        }
    }

    @Test
    fun `component - cells check_core_registrations`() = componentLifecycleTest(
        "cells_core_registrations", ItemTemplateTypes.CELLS, ItemComponentTypes.CELLS
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
            assertIs<VirtualCore>(cell.core)
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
            val core = cell.core as? AttributeCore
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
            val core = cell.core as? AttributeCore
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

        val common = BuiltInRegistries.RARITY.getEntryOrThrow("common")
        context {
            it.rarity = common // 假设稀有度为 "common"
        }

        result {
            assertFalse(it.isEmpty())
        }

        unboxed {
            assertIs<TextComponent>(it)
            assertEquals("Foo", it.content())

            val expectedStyle = Style.style(*common.unwrap().displayStyles)
            val actualStyle = it.style().edit { builder ->
                // 把 italic 显式设置为 false, 剩下的 style 应该跟稀有度的完全一致
                builder.decoration(TextDecoration.ITALIC, TextDecoration.State.NOT_SET)
            }
            assertEquals(expectedStyle, actualStyle)
        }
    }

    @Test
    fun `component - custom_model_data`() {

    }

    @Test
    fun `component - damage`() = componentLifecycleTest("damage", ItemTemplateTypes.DAMAGE, ItemComponentTypes.DAMAGE) {
        serialization {
            assertNotNull(it)
        }

        result {
            assertFalse(it.isEmpty())
        }
    }

    @Test
    fun `component - dyed color`() = componentLifecycleTest(
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

    @Test
    fun `component - max_damage`() = componentLifecycleTest("max_damage", ItemTemplateTypes.MAX_DAMAGE, ItemComponentTypes.MAX_DAMAGE) {
        serialization {
            assertNotNull(it)
        }

        result {
            assertFalse(it.isEmpty())
        }
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
                BuiltInRegistries.ELEMENT.getEntryOrThrow("neutral"),
                BuiltInRegistries.ELEMENT.getEntryOrThrow("water"),
                BuiltInRegistries.ELEMENT.getEntryOrThrow("fire"),
                BuiltInRegistries.ELEMENT.getEntryOrThrow("wind"),
            )
            assertEquals(2, elements.size)
            assertTrue(elements.all { it in possibleElements })
        }
    }

    @Test
    fun `component - enchantments`() {
        val prototype = readCustomPrototype("data", "enchantments")
        val template = prototype.templates.get(ItemTemplateTypes.ENCHANTMENTS)

        assertNotNull(template)
        assertFalse(template.showInTooltip)
        assertEquals(2, template.enchantments.size)
        assertEquals(1, template.enchantments[TypedKey.create(RegistryKey.ENCHANTMENT, Identifier.key("sharpness"))])
        assertEquals(2, template.enchantments[TypedKey.create(RegistryKey.ENCHANTMENT, Identifier.key("knockback"))])
    }

    @Test
    fun `component - damage_resistant`() = componentLifecycleTest(
        "damage_resistant", ItemTemplateTypes.DAMAGE_RESISTANT, ItemComponentTypes.DAMAGE_RESISTANT
    ) {
        serialization {
            assertNotNull(it)
        }

        result {
            assertFalse(it.isEmpty())
        }

        val tagKey = TagKey.create(RegistryKey.DAMAGE_TYPE, Key.key("is_fire"))

        unboxed {
            assertEquals(tagKey, it.types)
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

            // 测试 abilities
            val possibleAbilities = setOf(
                Key.key("foo:bar/a"),
                Key.key("foo:bar/b")
            )
            assertTrue(it.abilities.all { it in possibleAbilities })
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

        val common = BuiltInRegistries.RARITY.getEntryOrThrow("common")
        context {
            it.rarity = common // 假设稀有度为 "common"
        }

        result {
            assertFalse(it.isEmpty())
        }

        unboxed {
            assertIs<TextComponent>(it)
            assertEquals("Common", it.content())

            val expectedStyle = Style.style(*common.unwrap().displayStyles)
            val actualStyle = it.style().edit { builder ->
                // 把 italic 显式设置为 false, 剩下的 style 应该跟稀有度的完全一致
                builder.decoration(TextDecoration.ITALIC, TextDecoration.State.NOT_SET)
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

        val rarity = BuiltInRegistries.RARITY.getEntryOrThrow("rare")
        context {
            it.rarity = rarity // 假设稀有度
        }

        unboxed {
            val kizamiz = it.kizamiz
            assertEquals(2, kizamiz.size)
            val possibleKizamiz = setOf(
                BuiltInRegistries.KIZAMI.getEntryOrThrow("inner/wind_lace"),
                BuiltInRegistries.KIZAMI.getEntryOrThrow("antigravity"),
            )
            assertTrue(kizamiz.all { it in possibleKizamiz })
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
        "lore", ItemTemplateTypes.LORE, ItemComponentTypes.EMPTY,
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
    fun `component - stored_enchantments`() {
        val prototype = readCustomPrototype("data", "stored_enchantments")
        val template = prototype.templates.get(ItemTemplateTypes.STORED_ENCHANTMENTS)

        assertNotNull(template)
        assertFalse(template.showInTooltip)
        assertEquals(2, template.enchantments.size)
        assertEquals(1, template.enchantments[TypedKey.create(RegistryKey.ENCHANTMENT, Identifier.key("sharpness"))])
        assertEquals(2, template.enchantments[TypedKey.create(RegistryKey.ENCHANTMENT, Identifier.key("knockback"))])
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

    @Test
    fun `component - world_time_control`() = componentLifecycleTest(
        "world_time_control", ItemTemplateTypes.WORLD_TIME_CONTROL, ItemComponentTypes.EMPTY
    ) {
        serialization {
            assertNotNull(it)
            assertEquals(TimeControl.ActionType.SET_TIME, it.type)
            assertEquals(24000, it.time)
        }

        result {
            assertTrue(it.isEmpty())
        }
    }

    @Test
    fun `component - world_weather_control`() = componentLifecycleTest(
        "world_weather_control", ItemTemplateTypes.WORLD_WEATHER_CONTROL, ItemComponentTypes.EMPTY
    ) {
        serialization {
            assertNotNull(it)
        }

        result {
            assertTrue(it.isEmpty())
        }
    }
    //</editor-fold>
}
