import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.element.elementModule
import cc.mewcraft.wakame.entity.entityModule
import cc.mewcraft.wakame.item.binary.NekoStack
import cc.mewcraft.wakame.item.binary.cell.BinaryCellFactory
import cc.mewcraft.wakame.item.binary.cell.isNoop
import cc.mewcraft.wakame.item.itemModule
import cc.mewcraft.wakame.item.schema.*
import cc.mewcraft.wakame.item.schema.meta.*
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.kizami.kizamiModule
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.rarity.rarityModule
import cc.mewcraft.wakame.registry.*
import cc.mewcraft.wakame.skill.skillModule
import cc.mewcraft.wakame.skin.ItemSkin
import cc.mewcraft.wakame.skin.skinModule
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.util.Key
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import it.unimi.dsi.fastutil.longs.LongSet
import me.lucko.helper.shadows.nbt.*
import org.bukkit.inventory.ItemStack
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
                    entityModule(),
                    kizamiModule(),
                    registryModule(),
                    rarityModule(),
                    skillModule(),
                    skinModule()
                )
            }

            // initialize attribute facades
            AttributeRegistry.onPreWorld()

            // initialize necessary registry
            BehaviorRegistry.onPreWorld()
            ElementRegistry.onPreWorld()
            SkillRegistry.onPreWorld()
            KizamiRegistry.onPreWorld()
            RarityRegistry.onPreWorld()
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
    fun `schema item construction`() {
        val itemKey = Key("short_sword:demo")
        val demoItem = ItemRegistry.INSTANCES.find(itemKey)
        assertNotNull(demoItem)
    }

    @Test
    fun `binary item construction`() {
        val itemKey = Key("short_sword:demo")
        val demoItem = ItemRegistry.INSTANCES.find(itemKey)
        assertNotNull(demoItem)

        // mock NBT
        mockkStatic(
            ByteArrayShadowTag::class,
            ByteShadowTag::class,
            CompoundShadowTag::class,
            DoubleShadowTag::class,
            EndShadowTag::class,
            FloatShadowTag::class,
            IntArrayShadowTag::class,
            IntShadowTag::class,
            ListShadowTag::class,
            LongArrayShadowTag::class,
            LongShadowTag::class,
            ShortShadowTag::class,
            StringShadowTag::class,
        )
        val mockByteArrayTag = mockk<ByteArrayShadowTag>()
        every { ByteArrayShadowTag.create(any<ByteArray>()) } returns mockByteArrayTag
        every { ByteArrayShadowTag.create(any<List<Byte>>()) } returns mockByteArrayTag
        val mockByteTag = mockk<ByteShadowTag>()
        every { ByteShadowTag.valueOf(any<Boolean>()) } returns mockByteTag
        every { ByteShadowTag.valueOf(any<Byte>()) } returns mockByteTag
        val mockCompoundTag = mockk<CompoundShadowTag>()
        every { CompoundShadowTag.create() } returns mockCompoundTag
        every { mockCompoundTag.asString() } returns "EMPTY COMPOUND"
        val mockDoubleTag = mockk<DoubleShadowTag>()
        every { DoubleShadowTag.valueOf(any()) } returns mockDoubleTag
        val mockEndTag = mockk<EndShadowTag>()
        every { EndShadowTag.instance() } returns mockEndTag
        val mockFloatTag = mockk<FloatShadowTag>()
        every { FloatShadowTag.valueOf(any()) } returns mockFloatTag
        val mockIntArrayTag = mockk<IntArrayShadowTag>()
        every { IntArrayShadowTag.create(any<IntArray>()) } returns mockIntArrayTag
        every { IntArrayShadowTag.create(any<List<Int>>()) } returns mockIntArrayTag
        val mockIntTag = mockk<IntShadowTag>()
        every { IntShadowTag.valueOf(any()) } returns mockIntTag
        val mockListTag = mockk<ListShadowTag>()
        every { ListShadowTag.create() } returns mockListTag
        every { ListShadowTag.create(any(), any()) } returns mockListTag
        val mockLongArrayTag = mockk<LongArrayShadowTag>()
        every { LongArrayShadowTag.create(any<LongArray>()) } returns mockLongArrayTag
        every { LongArrayShadowTag.create(any<LongSet>()) } returns mockLongArrayTag
        every { LongArrayShadowTag.create(any<List<Long>>()) } returns mockLongArrayTag
        val mockLongTag = mockk<LongShadowTag>()
        every { LongShadowTag.valueOf(any()) } returns mockLongTag
        val mockShortTag = mockk<ShortShadowTag>()
        every { ShortShadowTag.valueOf(any()) } returns mockShortTag
        val mockStringTag = mockk<StringShadowTag>()
        every { StringShadowTag.valueOf(any()) } returns mockStringTag

        // mock player
        val mockUser = mockk<User<Nothing>>(relaxed = true)
        every { mockUser.level } returns 50

        // mock realizer (to avoid call on the Bukkit internals)
        val mockRealizer = mockk<NekoItemRealizer>(relaxed = true)
        every { mockRealizer.realize(demoItem, mockUser) } answers {
            val context = SchemaGenerationContext(SchemaGenerationTrigger.wrap(mockUser))

            generateAndSet<SCustomNameMeta, String>(demoItem, context)
            generateAndSet<SLoreMeta, List<String>>(demoItem, context)
            generateAndSet<SDurabilityMeta, Durability>(demoItem, context)
            generateAndSet<SFoodMeta, Food>(demoItem, context)
            generateAndSet<SLevelMeta, Int>(demoItem, context)
            generateAndSet<SRarityMeta, Rarity>(demoItem, context)
            generateAndSet<SElementMeta, Set<Element>>(demoItem, context)
            generateAndSet<SKizamiMeta, Set<Kizami>>(demoItem, context)
            generateAndSet<SSkinMeta, ItemSkin>(demoItem, context)
            generateAndSet<SSkinOwnerMeta, UUID>(demoItem, context)

            demoItem.cellMap.forEach { (id, schemaCell) ->
                val binaryCell = BinaryCellFactory.reify(schemaCell, context)
                if (!binaryCell.isNoop) {
                    logger.debug("write cell '{}': {}", id, binaryCell)
                } else {
                    logger.debug("skip writing noop cell")
                }
            }

            // just return an empty item
            mockk<NekoStack<ItemStack>>()
        }

        // call
        mockRealizer.realize(demoItem, mockUser)

        // verify
        verify { mockRealizer.realize(demoItem, mockUser) }
    }

    private inline fun <reified S : SchemaItemMeta<T>, T> generateAndSet(
        item: NekoItem,
        context: SchemaGenerationContext,
    ) {
        val meta = item.getMeta<S>()
        val value = meta.generate(context)
        if (value is GenerationResult.Thing) {
            logger.debug("write meta '{}': {}", S::class.simpleName, value.toString())
        }
    }
}