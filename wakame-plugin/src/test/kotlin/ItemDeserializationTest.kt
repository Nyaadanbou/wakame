import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.element.elementModule
import cc.mewcraft.wakame.item.binary.NekoStackFactory
import cc.mewcraft.wakame.item.binary.cell.BinaryCellFactory
import cc.mewcraft.wakame.item.itemModule
import cc.mewcraft.wakame.item.scheme.*
import cc.mewcraft.wakame.item.scheme.meta.*
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.kizami.kizamiModule
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.rarity.rarityModule
import cc.mewcraft.wakame.reference.referenceModule
import cc.mewcraft.wakame.registry.*
import cc.mewcraft.wakame.skin.ItemSkin
import cc.mewcraft.wakame.skin.skinModule
import cc.mewcraft.wakame.user.User
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import org.slf4j.Logger
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertNotNull

class ItemDeserializationTest : KoinTest {

    private val logger: Logger by inject()

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            startKoin {
                modules(testEnvironment())

                // this module
                modules(itemModule())

                // dependencies
                modules(
                    elementModule(),
                    kizamiModule(),
                    referenceModule(),
                    registryModule(),
                    rarityModule(),
                    skinModule()
                )
            }

            // initialize attribute facades
            AttributeRegistry.onPreWorld()

            // initialize necessary registry
            ElementRegistry.onPreWorld()
            KizamiRegistry.onPreWorld()
            RarityRegistry.onPreWorld()
            LevelMappingRegistry.onPreWorld()
            NekoItemRegistry.onPreWorld()
        }

        @JvmStatic
        @AfterAll
        fun shutdown() {
            stopKoin()
        }
    }

    @Test
    fun `scheme item construction`() {
        val key = Key.key("short_sword:demo")
        val demo = NekoItemRegistry.INSTANCES.find(key)
        assertNotNull(demo, "The item '$key' is not loaded correctly")
    }

    @Test
    fun `binary item construction`() {
        val key = Key.key("short_sword:demo")
        val demo = NekoItemRegistry.INSTANCES.find(key)
        assertNotNull(demo, "The item '$key' is not loaded correctly")

        val user = mockk<User>()
        val realizer = mockk<NekoItemRealizer>()

        // mock player
        every { user.level } returns 1
        // mock realizer (to avoid call on the Bukkit internals)
        every { realizer.realize(demo, user) } answers {
            val context = SchemeGenerationContext(SchemaGenerationTrigger.wrap(user))

            generateAndSet<DisplayNameMeta, String>(demo, context)
            generateAndSet<DisplayLoreMeta, List<String>>(demo, context)
            generateAndSet<DurabilityMeta, Durability>(demo, context)
            generateAndSet<LevelMeta, Int>(demo, context)
            generateAndSet<RarityMeta, Rarity>(demo, context)
            generateAndSet<ElementMeta, Set<Element>>(demo, context)
            generateAndSet<KizamiMeta, Set<Kizami>>(demo, context)
            generateAndSet<SkinMeta, ItemSkin>(demo, context)
            generateAndSet<SkinOwnerMeta, UUID>(demo, context)

            demo.cells.forEach { (id, scheme) ->
                val binary = BinaryCellFactory.generate(context, scheme)
                if (binary != null) {
                    logger.debug("write cell '{}': {}", id, binary)
                }
            }

            // just return an empty item
            NekoStackFactory.new(Material.AIR)
        }

        // call
        realizer.realize(demo, user)

        // verify
        verify { realizer.realize(demo, user) }
    }

    private inline fun <reified S : SchemeItemMeta<T>, T> generateAndSet(
        item: NekoItem,
        context: SchemeGenerationContext,
    ) {
        val meta = item.getItemMetaBy<S>()
        val value = meta.generate(context)
        if (value != null) {
            // set the meta only if something is generated
            logger.debug("write meta '{}': {}", S::class.simpleName, value.toString())
        }
    }
}