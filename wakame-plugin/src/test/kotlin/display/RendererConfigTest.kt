package display

import cc.mewcraft.wakame.adventure.adventureModule
import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.LoreLineFlatter
import cc.mewcraft.wakame.display.RendererBootstrap
import cc.mewcraft.wakame.display.displayModule
import cc.mewcraft.wakame.element.elementModule
import cc.mewcraft.wakame.item.components.cells.cores.attribute.CoreAttributeBootstrap
import cc.mewcraft.wakame.item.components.cells.cores.empty.CoreEmpty
import cc.mewcraft.wakame.item.components.cells.cores.empty.CoreEmptyBootstrap
import cc.mewcraft.wakame.item.components.cells.cores.skill.CoreSkillBootstrap
import cc.mewcraft.wakame.item.components.legacy.ItemMetaBootstrap
import cc.mewcraft.wakame.item.itemModule
import cc.mewcraft.wakame.kizami.kizamiModule
import cc.mewcraft.wakame.molang.molangModule
import cc.mewcraft.wakame.rarity.rarityModule
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.registry.KizamiRegistry
import cc.mewcraft.wakame.registry.RarityRegistry
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.registry.registryModule
import cc.mewcraft.wakame.skill.skillModule
import cc.mewcraft.wakame.skin.skinModule
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get
import org.slf4j.Logger
import testEnv
import kotlin.time.measureTimedValue

private val Component.plain: String get() = PlainTextComponentSerializer.plainText().serialize(this)
private fun listText(vararg text: String): List<Component> = text.map { Component.text(it) }
private fun coreEmptyLore(): LoreLine = CoreEmpty.provideTooltipLore()
private fun coreSimpleLore(x: String): LoreLine = LoreLine.simple(Key(x), listText(x))
private fun metaSimpleLore(x: String): LoreLine = LoreLine.simple(Key(x), listText(x))

class RendererConfigTest : KoinTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            val app = startKoin {
                modules(
                    // env
                    testEnv(),

                    // this module
                    displayModule(),

                    // dependencies
                    adventureModule(),
                    elementModule(),
                    itemModule(),
                    kizamiModule(),
                    molangModule(),
                    registryModule(),
                    rarityModule(),
                    skillModule(),
                    skinModule(),
                )
            }

            // registries
            AttributeRegistry.onPreWorld()
            ElementRegistry.onPreWorld()
            SkillRegistry.onPreWorld()
            KizamiRegistry.onPreWorld()
            RarityRegistry.onPreWorld()

            // item cells
            CoreAttributeBootstrap.onPostWorld()
            CoreEmptyBootstrap.onPostWorld()
            CoreSkillBootstrap.onPostWorld()

            // item meta
            ItemMetaBootstrap.onPostWorld()

            // initialize renderer bootstrap
            RendererBootstrap.onPostWorld()
        }

        @JvmStatic
        @AfterAll
        fun shutdown() {
            stopKoin()
        }
    }

    private fun buildTest(vararg loreLines: LoreLine) {
        val logger = get<Logger>()
        val flatter = get<LoreLineFlatter>()
        val loreLineList = loreLines.toList()
        logger.info("Start flattening lines")
        val (components, duration) = measureTimedValue { flatter.flatten(loreLineList) }
        components.forEach { logger.info(" - " + it.plain) }
        logger.info("Flattened lines - ${duration.inWholeMicroseconds} µs elapsed")
    }

    @Test
    fun `test flatten lore lines 1`() {
        buildTest(
            metaSimpleLore("meta:level"),
            metaSimpleLore("meta:rarity"),
            metaSimpleLore("meta:elements"),
            metaSimpleLore("meta:kizamiz"),
            metaSimpleLore("meta:lore"),
        )
    }

    @Test
    fun `test flatten lore lines 2`() {
        buildTest(
            metaSimpleLore("meta:level"),
            metaSimpleLore("meta:rarity"),
            metaSimpleLore("meta:elements"),
            metaSimpleLore("meta:kizamiz"),
            metaSimpleLore("meta:lore"),
            coreSimpleLore("attribute:attack_effect_chance.add"),
            coreSimpleLore("attribute:attack_speed_level.add"),
            coreSimpleLore("attribute:critical_strike_chance.add"),
            coreSimpleLore("attribute:max_mana.add"),
            coreEmptyLore(),
            coreEmptyLore(),
            coreEmptyLore(),
        )
    }

    @Test
    fun `test flatten lore lines 3`() {
        buildTest(
            metaSimpleLore("meta:level"),
            metaSimpleLore("meta:rarity"),
            metaSimpleLore("meta:elements"),
            metaSimpleLore("meta:kizamiz"),
            metaSimpleLore("meta:lore"),
            coreSimpleLore("attribute:attack_effect_chance.add"),
            coreSimpleLore("attribute:attack_speed_level.add"),
            coreSimpleLore("attribute:critical_strike_chance.add"),
            coreSimpleLore("attribute:max_mana.add"),
            coreSimpleLore("skill:blink"),
            coreSimpleLore("skill:frost"),
            coreSimpleLore("skill:leapfrog"),
            coreEmptyLore(),
            coreEmptyLore(),
        )
    }

    @Test
    fun `test flatten lore lines 4`() {
        buildTest(
            metaSimpleLore("meta:level"),
            metaSimpleLore("meta:rarity"),
            coreSimpleLore("attribute:max_health.add"),
            coreSimpleLore("attribute:max_mana.add"),
            coreSimpleLore("skill:frost"),
            coreEmptyLore(),
            coreEmptyLore(),
        )
    }

    @Test
    fun `test flatten lore lines 5`() {
        buildTest(
            metaSimpleLore("meta:level"),
            metaSimpleLore("meta:rarity"),
            coreSimpleLore("attribute:attack_damage.add.fire"),
            coreSimpleLore("attribute:attack_damage.add.water"),
            coreSimpleLore("attribute:attack_damage.multiply_base.fire"),
            coreSimpleLore("attribute:attack_damage.multiply_base.water"),
            coreSimpleLore("attribute:attack_effect_chance.add"),
            coreSimpleLore("attribute:attack_speed_level.add"),
            coreSimpleLore("skill:frost"),
            coreEmptyLore(),
            coreEmptyLore(),
        )
    }

    @Test
    fun `test flatten lore lines 6`() {
        buildTest(
            metaSimpleLore("meta:level"),
            metaSimpleLore("meta:rarity"),
            coreSimpleLore("attribute:attack_damage.add.fire"),
            coreSimpleLore("attribute:attack_damage.add.water"),
            coreSimpleLore("attribute:attack_damage.multiply_base.fire"),
            coreSimpleLore("attribute:attack_damage.multiply_base.water"),
            coreSimpleLore("attribute:attack_effect_chance.add"),
            coreSimpleLore("attribute:attack_speed_level.add"),
            coreSimpleLore("skill:frost"),
            coreEmptyLore(),
            coreEmptyLore(),
        )
    }
}