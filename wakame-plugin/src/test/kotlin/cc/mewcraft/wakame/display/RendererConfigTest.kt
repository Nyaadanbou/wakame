package cc.mewcraft.wakame.display

// TODO 新渲染系统完成后, 重写 RendererConfigTest
/*
import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.adventure.adventureModule
import cc.mewcraft.wakame.damage.damageModule
import cc.mewcraft.wakame.display.*
import cc.mewcraft.wakame.element.elementModule
import cc.mewcraft.wakame.item.components.cells.cores.*
import cc.mewcraft.wakame.item.components.legacy.ItemMetaBootstrap
import cc.mewcraft.wakame.item.itemModule
import cc.mewcraft.wakame.kizami.kizamiModule
import cc.mewcraft.wakame.molang.molangModule
import cc.mewcraft.wakame.rarity.rarityModule
import cc.mewcraft.wakame.registry.*
import cc.mewcraft.wakame.ability.abilityModule
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.world.worldModule
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.junit.jupiter.api.*
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get
import org.slf4j.Logger
import cc.mewcraft.wakame.testEnv
import kotlin.time.measureTimedValue

private val Component.plain: String get() = PlainTextComponentSerializer.plainText().serialize(this)
private fun listText(vararg text: String): List<Component> = text.map { Component.text(it) }
private fun emptyCoreLoreLine(): LoreLine = LoreLine.simple(GenericKeys.EMPTY, listText("empty core"))
private fun simpleCoreLoreLine(x: String): LoreLine = LoreLine.simple(Key(x), listText(x))
private fun componentLoreLine(x: String): LoreLine = LoreLine.simple(Key(x), listText(x))

class RendererConfigTest : KoinTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            val app = startKoin {
                modules(
                    // env
                    cc.mewcraft.wakame.testEnv(),

                    // this module
                    displayModule(),

                    // dependencies
                    adventureModule(),
                    damageModule(),
                    elementModule(),
                    itemModule(),
                    kizamiModule(),
                    molangModule(),
                    registryModule(),
                    rarityModule(),
                    abilityModule(),
                    worldModule()
                )
            }

            // registries
            AttributeRegistry.onPreWorld()
            ElementRegistry.onPreWorld()
            KizamiRegistry.onPreWorld()
            RarityRegistry.onPreWorld()

            // item cells
            AttributeCoreBootstrap.onPostWorld()
            EmptyCoreBootstrap.onPostWorld()

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
            componentLoreLine("meta:level"),
            componentLoreLine("meta:rarity"),
            componentLoreLine("meta:elements"),
            componentLoreLine("meta:kizamiz"),
            componentLoreLine("meta:lore"),
        )
    }

    @Test
    fun `test flatten lore lines 2`() {
        buildTest(
            componentLoreLine("meta:level"),
            componentLoreLine("meta:rarity"),
            componentLoreLine("meta:elements"),
            componentLoreLine("meta:kizamiz"),
            componentLoreLine("meta:lore"),
            simpleCoreLoreLine("attribute:attack_effect_chance.add"),
            simpleCoreLoreLine("attribute:critical_strike_chance.add"),
            simpleCoreLoreLine("attribute:max_mana.add"),
            emptyCoreLoreLine(),
            emptyCoreLoreLine(),
            emptyCoreLoreLine(),
        )
    }

    @Test
    fun `test flatten lore lines 3`() {
        buildTest(
            componentLoreLine("meta:level"),
            componentLoreLine("meta:rarity"),
            componentLoreLine("meta:elements"),
            componentLoreLine("meta:kizamiz"),
            componentLoreLine("meta:lore"),
            simpleCoreLoreLine("attribute:attack_effect_chance.add"),
            simpleCoreLoreLine("attribute:critical_strike_chance.add"),
            simpleCoreLoreLine("attribute:max_mana.add"),
            simpleCoreLoreLine("ability:blink"),
            simpleCoreLoreLine("ability:frost"),
            simpleCoreLoreLine("ability:leapfrog"),
            emptyCoreLoreLine(),
            emptyCoreLoreLine(),
        )
    }

    @Test
    fun `test flatten lore lines 4`() {
        buildTest(
            componentLoreLine("meta:level"),
            componentLoreLine("meta:rarity"),
            simpleCoreLoreLine("attribute:max_health.add"),
            simpleCoreLoreLine("attribute:max_mana.add"),
            simpleCoreLoreLine("ability:frost"),
            emptyCoreLoreLine(),
            emptyCoreLoreLine(),
        )
    }

    @Test
    fun `test flatten lore lines 5`() {
        buildTest(
            componentLoreLine("meta:level"),
            componentLoreLine("meta:rarity"),
            simpleCoreLoreLine("attribute:attack_damage.add.fire"),
            simpleCoreLoreLine("attribute:attack_damage.add.water"),
            simpleCoreLoreLine("attribute:attack_damage.multiply_base.fire"),
            simpleCoreLoreLine("attribute:attack_damage.multiply_base.water"),
            simpleCoreLoreLine("attribute:attack_effect_chance.add"),
            simpleCoreLoreLine("ability:frost"),
            emptyCoreLoreLine(),
            emptyCoreLoreLine(),
        )
    }

    @Test
    fun `test flatten lore lines 6`() {
        buildTest(
            componentLoreLine("meta:level"),
            componentLoreLine("meta:rarity"),
            simpleCoreLoreLine("attribute:attack_damage.add.fire"),
            simpleCoreLoreLine("attribute:attack_damage.add.water"),
            simpleCoreLoreLine("attribute:attack_damage.multiply_base.fire"),
            simpleCoreLoreLine("attribute:attack_damage.multiply_base.water"),
            simpleCoreLoreLine("attribute:attack_effect_chance.add"),
            simpleCoreLoreLine("ability:frost"),
            emptyCoreLoreLine(),
            emptyCoreLoreLine(),
        )
    }
}*/
