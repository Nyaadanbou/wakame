import cc.mewcraft.wakame.adventure.adventureModule
import cc.mewcraft.wakame.element.elementModule
import cc.mewcraft.wakame.entity.entityModule
import cc.mewcraft.wakame.item.itemModule
import cc.mewcraft.wakame.kizami.kizamiModule
import cc.mewcraft.wakame.lookup.ItemModelDataLookup
import cc.mewcraft.wakame.lookup.lookupModule
import cc.mewcraft.wakame.rarity.rarityModule
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.registry.EntityRegistry
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.registry.ItemRegistry.get
import cc.mewcraft.wakame.registry.KizamiRegistry
import cc.mewcraft.wakame.registry.LevelMappingRegistry
import cc.mewcraft.wakame.registry.RarityRegistry
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.registry.registryModule
import cc.mewcraft.wakame.skill.skillModule
import cc.mewcraft.wakame.skin.skinModule
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.koin.core.component.get
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import kotlin.test.assertEquals

class ItemModelDataLookupTest : KoinTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            startKoin {
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
                    registryModule(),
                    rarityModule(),
                    skillModule(),
                    skinModule(),
                    lookupModule()
                )
            }

            AttributeRegistry.onPreWorld()
            ElementRegistry.onPreWorld()
            KizamiRegistry.onPreWorld()
            RarityRegistry.onPreWorld()
            SkillRegistry.onPreWorld()
            LevelMappingRegistry.onPreWorld()
            EntityRegistry.onPreWorld()
            ItemRegistry.onPreWorld()
        }

        @JvmStatic
        @AfterAll
        fun shutdown() {
            stopKoin()
        }
    }

    @Test
    fun `test get item model data`() {
        val itemModelDataLookup = get<ItemModelDataLookup>()
        itemModelDataLookup.onPrePack()
        val demoItem = ItemRegistry.INSTANCES.get("short_sword:demo")
        val itemModelData = itemModelDataLookup[demoItem.key, 0]

        assertEquals(10000, itemModelData)
    }
}