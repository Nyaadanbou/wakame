package cc.mewcraft.wakame.item.behavior

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.item.behavior.InteractableBlocks.INTERACTABLE_BLOCKS
import cc.mewcraft.wakame.item.behavior.InteractableEntities.INTERACTABLE_ENTITIES
import cc.mewcraft.wakame.util.UniversalBlocks
import cc.mewcraft.wakame.util.toLocation
import io.papermc.paper.entity.Shearable
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockType
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.*
import org.bukkit.craftbukkit.block.impl.CraftComposter
import org.bukkit.craftbukkit.block.impl.CraftSweetBerryBush
import org.bukkit.entity.*
import org.bukkit.entity.memory.MemoryKey
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ItemType
import org.joml.Vector3d

data class UseOnContext(
    override val player: Player,
    override val itemstack: ItemStack,
    val hand: InteractionHand,
    val context: BlockInteractContext,
    /**
     * 标记此次交互是否触发了方块的交互.
     * 例如, 玩家使用剪刀/玻璃瓶非潜行状态下交互蜂巢, 或某个自定义方块右键后执行了特定效果, 此时标记为 true.
     */
    val triggersBlockInteract: Boolean,
) : ItemBehaviorContext {
    val world: World
        get() = player.world

    val blockLocation: Location
        get() = this.context.blockPosition.toLocation(this.world)

    val blockPosition: Vector3d
        get() = context.blockPosition

    val interactFace: BlockFace
        get() = context.interactFace

    val interactPoint: Vector3d
        get() = context.interactPoint
}

data class UseContext(
    override val player: Player,
    override val itemstack: ItemStack,
    val hand: InteractionHand,
) : ItemBehaviorContext {
    val world: World
        get() = player.world
}

data class UseEntityContext(
    override val player: Player,
    override val itemstack: ItemStack,
    val hand: InteractionHand,
    val entity: Entity,

    /**
     * 标记此次交互是否触发了实体的交互.
     * 例如, 玩家右键村民试图交易, 此时标记为 true.
     */
    val triggersEntityInteract: Boolean,
) : ItemBehaviorContext {
    val world: World
        get() = player.world
}

data class AttackOnContext(
    override val player: Player,
    override val itemstack: ItemStack,
    val blockPosition: Vector3d,
    val interactFace: BlockFace,
) : ItemBehaviorContext {
    val world: World
        get() = player.world
}

data class AttackContext(
    override val player: Player,
    override val itemstack: ItemStack,
) : ItemBehaviorContext {
    val world: World
        get() = player.world
}

data class AttackEntityContext(
    override val player: Player,
    override val itemstack: ItemStack,
    val entity: Entity,
) : ItemBehaviorContext {
    val world: World
        get() = player.world
}

data class BlockInteractContext(
    val blockPosition: Vector3d,
    val interactFace: BlockFace,
    val interactPoint: Vector3d,
)

enum class InteractionHand {
    MAIN_HAND,
    OFF_HAND
}

enum class InteractionResult {
    /**
     * 交互成功.
     * 此时代码中断, 不会再执行后续的任何交互行为.
     * 此时取消事件.
     */
    SUCCESS_AND_CANCEL,

    /**
     * 交互失败.
     * 此时代码中断, 不会再执行后续的任何交互行为.
     * 此时取消事件.
     */
    FAIL_AND_CANCEL,

    /**
     * 交互成功.
     * 此时代码中断, 不会再执行后续的任何交互行为.
     * 此时不会取消事件.
     */
    SUCCESS,

    /**
     * 交互失败.
     * 此时代码中断, 不会再执行后续的任何交互行为.
     * 此时不会取消事件.
     */
    FAIL,

    /**
     * 跳过交互.
     * 此时仍然会继续尝试执行后续的交互行为.
     */
    PASS,
}

fun InteractionResult.isSuccess(): Boolean {
    return this == InteractionResult.SUCCESS || this == InteractionResult.SUCCESS_AND_CANCEL
}

fun InteractionResult.shouldCancel(): Boolean {
    return this == InteractionResult.SUCCESS_AND_CANCEL || this == InteractionResult.FAIL_AND_CANCEL
}

/**
 * 方块交互检查逻辑的函数接口.
 */
fun interface BlockInteractionTest {
    fun test(player: Player, itemstack: ItemStack, blockData: BlockData, context: BlockInteractContext): Boolean
}

/**
 * 实体交互检查逻辑的函数接口.
 */
fun interface EntityInteractionTest {
    fun test(player: Player, itemstack: ItemStack, entity: Entity): Boolean
}

private object InteractableBlocks {
    val INTERACTABLE_BLOCKS: Reference2ObjectMap<BlockType, BlockInteractionTest> = Reference2ObjectOpenHashMap()

    // 该列表并不全面, 但基本够用
    // 例如, 未考虑: 可参与营火配方的物品会触发营火的方块交互, 炼药锅的各种交互
    // 这些交互都很复杂, 但共同点是都有特定的物品要求
    // 也就是说, 只要不用这些特定的物品做自定义物品的基底, 就没有任何影响
    init {
        // 玩家可食用蛋糕方块时, 方块可交互
        // 该判断逻辑与 NMS 一致
        createInteractableBlocks(
            listOf(
                BlockType.CAKE,
                BlockType.CANDLE_CAKE,
                BlockType.WHITE_CANDLE_CAKE,
                BlockType.ORANGE_CANDLE_CAKE,
                BlockType.MAGENTA_CANDLE_CAKE,
                BlockType.LIGHT_BLUE_CANDLE_CAKE,
                BlockType.YELLOW_CANDLE_CAKE,
                BlockType.LIME_CANDLE_CAKE,
                BlockType.PINK_CANDLE_CAKE,
                BlockType.GRAY_CANDLE_CAKE,
                BlockType.LIGHT_GRAY_CANDLE_CAKE,
                BlockType.CYAN_CANDLE_CAKE,
                BlockType.PURPLE_CANDLE_CAKE,
                BlockType.BLUE_CANDLE_CAKE,
                BlockType.BROWN_CANDLE_CAKE,
                BlockType.GREEN_CANDLE_CAKE,
                BlockType.RED_CANDLE_CAKE,
                BlockType.BLACK_CANDLE_CAKE,
            )
        ) { player, itemstack, blockData, context ->
            player.isInvulnerable || player.foodLevel < 20
        }
        // 玩家为 OP 且处于创造模式时, 方块可交互
        createInteractableBlocks(
            listOf(
                BlockType.COMMAND_BLOCK,
                BlockType.CHAIN_COMMAND_BLOCK,
                BlockType.REPEATING_COMMAND_BLOCK,
                BlockType.JIGSAW,
                BlockType.STRUCTURE_BLOCK,
                BlockType.TEST_INSTANCE_BLOCK,
                BlockType.TEST_BLOCK
            )
        ) { player, itemstack, blockData, context ->
            player.isOp && player.gameMode == GameMode.CREATIVE
        }
        // 玩家手持光源方块物品时, 方块可交互
        createInteractableBlock(BlockType.LIGHT) { player, itemstack, blockData, context ->
            itemstack.checkItemType(ItemType.LIGHT)
        }
        // 玩家手持指南针时, 方块可交互
        createInteractableBlock(BlockType.LODESTONE) { player, itemstack, blockData, context ->
            itemstack.checkItemType(ItemType.COMPASS)
        }
        // 玩家手持剪刀时, 方块可交互
        createInteractableBlock(BlockType.PUMPKIN) { player, itemstack, blockData, context ->
            itemstack.checkItemType(ItemType.SHEARS)
        }
        // 玩家手持桶时, 方块可交互
        createInteractableBlock(BlockType.POWDER_SNOW) { player, itemstack, blockData, context ->
            itemstack.checkItemType(ItemType.BUCKET)
        }
        // 玩家手持打火石或火焰弹时, 方块可交互
        createInteractableBlock(BlockType.TNT) { player, itemstack, blockData, context ->
            itemstack.checkItemType(ItemType.FLINT_AND_STEEL, ItemType.FIRE_CHARGE)
        }
        // 玩家手持剪刀或玻璃瓶且蜂巢蜂蜜等级大于等于 5 时, 方块可交互
        createInteractableBlocks(
            listOf(
                BlockType.BEEHIVE,
                BlockType.BEE_NEST
            )
        ) { player, itemstack, blockData, context ->
            itemstack.checkItemType(ItemType.SHEARS, ItemType.GLASS_BOTTLE) && blockData is Beehive && blockData.honeyLevel >= blockData.maximumHoneyLevel
        }
        // 玩家交互钟特定侧面的特定位置时, 方块可交互
        createInteractableBlock(BlockType.BELL) { player, itemstack, blockData, context ->
            val blockPosition = context.blockPosition
            if (blockData is Bell) {
                val interactFace = context.interactFace
                val isSide = when (interactFace) {
                    BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST -> true
                    else -> false
                }
                val deltaY = context.interactPoint.y - blockPosition.y
                if (isSide && deltaY <= 0.8123999834060669) {
                    return@createInteractableBlock when (blockData.attachment) {
                        Bell.Attachment.FLOOR -> {
                            when (blockData.facing) {
                                BlockFace.NORTH, BlockFace.SOUTH -> interactFace == BlockFace.NORTH || interactFace == BlockFace.SOUTH
                                BlockFace.EAST, BlockFace.WEST -> interactFace == BlockFace.EAST || interactFace == BlockFace.WEST
                                else -> false
                            }
                        }

                        Bell.Attachment.SINGLE_WALL,
                        Bell.Attachment.DOUBLE_WALL,
                            -> {
                            when (blockData.facing) {
                                BlockFace.NORTH, BlockFace.SOUTH -> interactFace == BlockFace.EAST || interactFace == BlockFace.WEST
                                BlockFace.EAST, BlockFace.WEST -> interactFace == BlockFace.NORTH || interactFace == BlockFace.SOUTH
                                else -> false
                            }
                        }

                        Bell.Attachment.CEILING -> true
                    }
                }
            }
            return@createInteractableBlock false
        }
        // 玩家交互朝向面时, 方块可交互
        createInteractableBlock(BlockType.CHISELED_BOOKSHELF) { player, itemstack, blockData, context ->
            blockData is ChiseledBookshelf && blockData.facing == context.interactFace
        }
        // 玩家手持可堆肥物品时, 方块可交互
        // 堆肥桶堆满时, 方块可交互
        createInteractableBlock(BlockType.COMPOSTER) { player, itemstack, blockData, context ->
            (blockData is CraftComposter && blockData.level == blockData.maximumLevel) || itemstack.type.isCompostable
        }
        // 重生锚充能等级大于 0 时, 方块可交互
        // 玩家手持荧石块时, 方块可交互
        createInteractableBlock(BlockType.RESPAWN_ANCHOR) { player, itemstack, blockData, context ->
            (blockData is RespawnAnchor && blockData.charges > 0) || itemstack.checkItemType(ItemType.GLOWSTONE)
        }
        // 唱片机内有唱片时, 方块可交互
        createInteractableBlock(BlockType.JUKEBOX) { player, itemstack, blockData, context ->
            blockData is Jukebox && blockData.hasRecord()
        }
        // 甜浆果丛 age 为 2 和 3 时, 方块可交互
        // 玩家手持骨粉且 age 小于 3 时, 方块可交互
        createInteractableBlock(BlockType.SWEET_BERRY_BUSH) { player, itemstack, blockData, context ->
            blockData is CraftSweetBerryBush && (blockData.age >= 2 || (blockData.age < 3 && itemstack.checkItemType(ItemType.BONE_MEAL)))
        }
        // 洞穴藤蔓有浆果时, 方块可交互
        // 玩家手持骨粉且无浆果时, 方块可交互
        createInteractableBlocks(
            listOf(
                BlockType.CAVE_VINES,
                BlockType.CAVE_VINES_PLANT
            )
        ) { player, itemstack, blockData, context ->
            blockData is CaveVines && (blockData.hasBerries() || itemstack.checkItemType(ItemType.BONE_MEAL))
        }
        // 宝库处于 active 状态时, 方块可交互
        createInteractableBlock(BlockType.VAULT) { player, itemstack, blockData, context ->
            blockData is Vault && blockData.vaultState == Vault.State.ACTIVE
        }
        // 任何情况下, 方块可交互
        createInteractableBlocks(
            listOf(
                // 告示牌
                BlockType.OAK_SIGN,
                BlockType.SPRUCE_SIGN,
                BlockType.ACACIA_SIGN,
                BlockType.BIRCH_SIGN,
                BlockType.CHERRY_SIGN,
                BlockType.JUNGLE_SIGN,
                BlockType.DARK_OAK_SIGN,
                BlockType.PALE_OAK_SIGN,
                BlockType.MANGROVE_SIGN,
                BlockType.BAMBOO_SIGN,
                BlockType.WARPED_SIGN,
                BlockType.CRIMSON_SIGN,
                BlockType.OAK_WALL_SIGN,
                BlockType.SPRUCE_WALL_SIGN,
                BlockType.BIRCH_WALL_SIGN,
                BlockType.ACACIA_WALL_SIGN,
                BlockType.CHERRY_WALL_SIGN,
                BlockType.JUNGLE_WALL_SIGN,
                BlockType.DARK_OAK_WALL_SIGN,
                BlockType.PALE_OAK_WALL_SIGN,
                BlockType.MANGROVE_WALL_SIGN,
                BlockType.BAMBOO_WALL_SIGN,
                BlockType.CRIMSON_WALL_SIGN,
                BlockType.WARPED_WALL_SIGN,
                BlockType.OAK_HANGING_SIGN,
                BlockType.SPRUCE_HANGING_SIGN,
                BlockType.BIRCH_HANGING_SIGN,
                BlockType.ACACIA_HANGING_SIGN,
                BlockType.CHERRY_HANGING_SIGN,
                BlockType.JUNGLE_HANGING_SIGN,
                BlockType.DARK_OAK_HANGING_SIGN,
                BlockType.PALE_OAK_HANGING_SIGN,
                BlockType.CRIMSON_HANGING_SIGN,
                BlockType.WARPED_HANGING_SIGN,
                BlockType.MANGROVE_HANGING_SIGN,
                BlockType.BAMBOO_HANGING_SIGN,
                BlockType.OAK_WALL_HANGING_SIGN,
                BlockType.SPRUCE_WALL_HANGING_SIGN,
                BlockType.BIRCH_WALL_HANGING_SIGN,
                BlockType.ACACIA_WALL_HANGING_SIGN,
                BlockType.CHERRY_WALL_HANGING_SIGN,
                BlockType.JUNGLE_WALL_HANGING_SIGN,
                BlockType.DARK_OAK_WALL_HANGING_SIGN,
                BlockType.PALE_OAK_WALL_HANGING_SIGN,
                BlockType.MANGROVE_WALL_HANGING_SIGN,
                BlockType.CRIMSON_WALL_HANGING_SIGN,
                BlockType.WARPED_WALL_HANGING_SIGN,
                BlockType.BAMBOO_WALL_HANGING_SIGN,
            ) + listOf(
                // 床
                BlockType.WHITE_BED,
                BlockType.ORANGE_BED,
                BlockType.MAGENTA_BED,
                BlockType.LIGHT_BLUE_BED,
                BlockType.YELLOW_BED,
                BlockType.LIME_BED,
                BlockType.PINK_BED,
                BlockType.GRAY_BED,
                BlockType.LIGHT_GRAY_BED,
                BlockType.CYAN_BED,
                BlockType.PURPLE_BED,
                BlockType.BLUE_BED,
                BlockType.BROWN_BED,
                BlockType.GREEN_BED,
                BlockType.RED_BED,
                BlockType.BLACK_BED,
            ) + listOf(
                // 按钮
                BlockType.OAK_BUTTON,
                BlockType.SPRUCE_BUTTON,
                BlockType.BIRCH_BUTTON,
                BlockType.JUNGLE_BUTTON,
                BlockType.ACACIA_BUTTON,
                BlockType.CHERRY_BUTTON,
                BlockType.DARK_OAK_BUTTON,
                BlockType.PALE_OAK_BUTTON,
                BlockType.MANGROVE_BUTTON,
                BlockType.BAMBOO_BUTTON,
                BlockType.STONE_BUTTON,
                BlockType.POLISHED_BLACKSTONE_BUTTON,
                BlockType.CRIMSON_BUTTON,
                BlockType.WARPED_BUTTON,
            ) + listOf(
                // 门
                BlockType.OAK_DOOR,
                BlockType.SPRUCE_DOOR,
                BlockType.BIRCH_DOOR,
                BlockType.JUNGLE_DOOR,
                BlockType.ACACIA_DOOR,
                BlockType.CHERRY_DOOR,
                BlockType.DARK_OAK_DOOR,
                BlockType.PALE_OAK_DOOR,
                BlockType.MANGROVE_DOOR,
                BlockType.BAMBOO_DOOR,
                BlockType.CRIMSON_DOOR,
                BlockType.WARPED_DOOR,
                BlockType.COPPER_DOOR,
                BlockType.EXPOSED_COPPER_DOOR,
                BlockType.OXIDIZED_COPPER_DOOR,
                BlockType.WEATHERED_COPPER_DOOR,
                BlockType.WAXED_COPPER_DOOR,
                BlockType.WAXED_EXPOSED_COPPER_DOOR,
                BlockType.WAXED_OXIDIZED_COPPER_DOOR,
                BlockType.WAXED_WEATHERED_COPPER_DOOR,
            ) + listOf(
                // 栅栏门
                BlockType.OAK_FENCE_GATE,
                BlockType.SPRUCE_FENCE_GATE,
                BlockType.BIRCH_FENCE_GATE,
                BlockType.JUNGLE_FENCE_GATE,
                BlockType.ACACIA_FENCE_GATE,
                BlockType.CHERRY_FENCE_GATE,
                BlockType.DARK_OAK_FENCE_GATE,
                BlockType.PALE_OAK_FENCE_GATE,
                BlockType.MANGROVE_FENCE_GATE,
                BlockType.BAMBOO_FENCE_GATE,
                BlockType.CRIMSON_FENCE_GATE,
                BlockType.WARPED_FENCE_GATE,
            ) + listOf(
                // 花盆
                BlockType.FLOWER_POT,
                BlockType.POTTED_OPEN_EYEBLOSSOM,
                BlockType.POTTED_CLOSED_EYEBLOSSOM,
                BlockType.POTTED_POPPY,
                BlockType.POTTED_BLUE_ORCHID,
                BlockType.POTTED_ALLIUM,
                BlockType.POTTED_AZURE_BLUET,
                BlockType.POTTED_RED_TULIP,
                BlockType.POTTED_ORANGE_TULIP,
                BlockType.POTTED_WHITE_TULIP,
                BlockType.POTTED_PINK_TULIP,
                BlockType.POTTED_OXEYE_DAISY,
                BlockType.POTTED_DANDELION,
                BlockType.POTTED_OAK_SAPLING,
                BlockType.POTTED_SPRUCE_SAPLING,
                BlockType.POTTED_BIRCH_SAPLING,
                BlockType.POTTED_JUNGLE_SAPLING,
                BlockType.POTTED_ACACIA_SAPLING,
                BlockType.POTTED_DARK_OAK_SAPLING,
                BlockType.POTTED_PALE_OAK_SAPLING,
                BlockType.POTTED_RED_MUSHROOM,
                BlockType.POTTED_BROWN_MUSHROOM,
                BlockType.POTTED_DEAD_BUSH,
                BlockType.POTTED_FERN,
                BlockType.POTTED_CACTUS,
                BlockType.POTTED_CORNFLOWER,
                BlockType.POTTED_LILY_OF_THE_VALLEY,
                BlockType.POTTED_WITHER_ROSE,
                BlockType.POTTED_BAMBOO,
                BlockType.POTTED_CRIMSON_FUNGUS,
                BlockType.POTTED_WARPED_FUNGUS,
                BlockType.POTTED_CRIMSON_ROOTS,
                BlockType.POTTED_WARPED_ROOTS,
                BlockType.POTTED_AZALEA_BUSH,
                BlockType.POTTED_FLOWERING_AZALEA_BUSH,
                BlockType.POTTED_MANGROVE_PROPAGULE,
                BlockType.POTTED_CHERRY_SAPLING,
                BlockType.POTTED_TORCHFLOWER,
            ) + listOf(
                // 潜影盒
                BlockType.SHULKER_BOX,
                BlockType.WHITE_SHULKER_BOX,
                BlockType.ORANGE_SHULKER_BOX,
                BlockType.MAGENTA_SHULKER_BOX,
                BlockType.LIGHT_BLUE_SHULKER_BOX,
                BlockType.YELLOW_SHULKER_BOX,
                BlockType.LIME_SHULKER_BOX,
                BlockType.PINK_SHULKER_BOX,
                BlockType.GRAY_SHULKER_BOX,
                BlockType.LIGHT_GRAY_SHULKER_BOX,
                BlockType.CYAN_SHULKER_BOX,
                BlockType.PURPLE_SHULKER_BOX,
                BlockType.BLUE_SHULKER_BOX,
                BlockType.BROWN_SHULKER_BOX,
                BlockType.GREEN_SHULKER_BOX,
                BlockType.RED_SHULKER_BOX,
                BlockType.BLACK_SHULKER_BOX,
            ) + listOf(
                // 活板门
                BlockType.OAK_TRAPDOOR,
                BlockType.SPRUCE_TRAPDOOR,
                BlockType.BIRCH_TRAPDOOR,
                BlockType.JUNGLE_TRAPDOOR,
                BlockType.ACACIA_TRAPDOOR,
                BlockType.CHERRY_TRAPDOOR,
                BlockType.DARK_OAK_TRAPDOOR,
                BlockType.PALE_OAK_TRAPDOOR,
                BlockType.MANGROVE_TRAPDOOR,
                BlockType.BAMBOO_TRAPDOOR,
                BlockType.CRIMSON_TRAPDOOR,
                BlockType.WARPED_TRAPDOOR,
                BlockType.COPPER_TRAPDOOR,
                BlockType.EXPOSED_COPPER_TRAPDOOR,
                BlockType.OXIDIZED_COPPER_TRAPDOOR,
                BlockType.WEATHERED_COPPER_TRAPDOOR,
                BlockType.WAXED_COPPER_TRAPDOOR,
                BlockType.WAXED_EXPOSED_COPPER_TRAPDOOR,
                BlockType.WAXED_OXIDIZED_COPPER_TRAPDOOR,
                BlockType.WAXED_WEATHERED_COPPER_TRAPDOOR,
            ) + listOf(
                // 其他
                BlockType.ANVIL,
                BlockType.BARREL,
                BlockType.BEACON,
                BlockType.BLAST_FURNACE,
                BlockType.BREWING_STAND,
                BlockType.CARTOGRAPHY_TABLE,
                BlockType.CHEST,
                BlockType.CHIPPED_ANVIL,
                BlockType.COMPARATOR,
                BlockType.CRAFTER,
                BlockType.CRAFTING_TABLE,
                BlockType.DAMAGED_ANVIL,
                BlockType.DAYLIGHT_DETECTOR,
                BlockType.DECORATED_POT,
                BlockType.DEEPSLATE_REDSTONE_ORE,
                BlockType.DISPENSER,
                BlockType.DRAGON_EGG,
                BlockType.DROPPER,
                BlockType.ENCHANTING_TABLE,
                BlockType.ENDER_CHEST,
                BlockType.FURNACE,
                BlockType.GRINDSTONE,
                BlockType.HOPPER,
                BlockType.LECTERN,
                BlockType.LEVER,
                BlockType.LOOM,
                BlockType.NOTE_BLOCK,
                BlockType.REDSTONE_ORE,
                BlockType.REDSTONE_WIRE,
                BlockType.REPEATER,
                BlockType.SMITHING_TABLE,
                BlockType.SMOKER,
                BlockType.STONECUTTER,
                BlockType.TRAPPED_CHEST,
            )
        ) { player, itemstack, blockData, context -> true }
    }

    private fun createInteractableBlock(blockType: BlockType, test: BlockInteractionTest) {
        INTERACTABLE_BLOCKS.put(blockType, test)?.let {
            LOGGER.warn("Duplicate interactable block created: $blockType")
        }
    }

    private fun createInteractableBlocks(blockTypes: List<BlockType>, test: BlockInteractionTest) {
        blockTypes.forEach { createInteractableBlock(it, test) }
    }

    /**
     * 方便函数.
     */
    private fun ItemStack.checkItemType(vararg itemTypes: ItemType): Boolean {
        return itemTypes.any { this.type.asItemType() == it }
    }
}

private object InteractableEntities {
    val INTERACTABLE_ENTITIES: Reference2ObjectMap<EntityType, EntityInteractionTest> = Reference2ObjectOpenHashMap()

    init {
        // 玩家手持对应食物时, 可交互
        createInteractableEntity(
            listOf(
                EntityType.BEE,
                EntityType.CHICKEN,
                EntityType.FOX,
                EntityType.FROG,
                EntityType.HOGLIN,
                EntityType.OCELOT,
                EntityType.PANDA,
                EntityType.RABBIT,
                EntityType.SNIFFER,
                EntityType.TURTLE,
            )
        ) { player, itemstack, entity ->
            itemstack.checkIsCorrespondingFood(entity)
        }
        // 玩家手持水桶时, 可交互
        // 玩家手持对应食物时, 可交互
        createInteractableEntity(EntityType.AXOLOTL) { player, itemstack, entity ->
            itemstack.checkItemType(ItemType.WATER_BUCKET) || itemstack.checkIsCorrespondingFood(entity)
        }
        // 玩家手持水桶时, 可交互
        // 玩家手持 #frog_food 标签物品时 (蝌蚪食物使用此标签), 可交互
        createInteractableEntity(EntityType.TADPOLE) { player, itemstack, entity ->
            itemstack.checkItemType(ItemType.WATER_BUCKET) || itemstack.checkItemTag(Tag.ITEMS_FROG_FOOD)
        }
        // 玩家手持水桶时, 可交互
        createInteractableEntity(
            listOf(
                EntityType.COD,
                EntityType.PUFFERFISH,
                EntityType.SALMON,
                EntityType.TROPICAL_FISH,
            )
        ) { player, itemstack, entity ->
            itemstack.checkItemType(ItemType.WATER_BUCKET)
        }
        // 玩家可以修剪实体时, 可交互
        createInteractableEntity(
            listOf(
                EntityType.BOGGED,
                EntityType.SNOW_GOLEM
            )
        ) { player, itemstack, entity ->
            itemstack.checkCanShear(entity)
        }
        // 玩家可以修剪实体时, 可交互
        // 玩家手持对应食物时, 可交互
        // 染料的交互逻辑在物品上而非 Sheep 实体上
        createInteractableEntity(EntityType.SHEEP) { player, itemstack, entity ->
            itemstack.checkCanShear(entity) || itemstack.checkIsCorrespondingFood(entity)
        }
        // 玩家可以修剪实体时, 可交互
        // 玩家手持牛对应食物时(哞菇食物用的是牛标签), 可交互
        // 玩家手持桶或碗且实体已成年时, 可交互
        // 哞菇为棕色变种且玩家手持 #small_flowers 标签物品时, 可交互
        createInteractableEntity(EntityType.MOOSHROOM) { player, itemstack, entity ->
            itemstack.checkCanShear(entity) ||
                    itemstack.checkItemTag(Tag.ITEMS_COW_FOOD) ||
                    (itemstack.checkItemType(ItemType.BUCKET, ItemType.BOWL) && entity.checkIsAdult()) ||
                    (entity is MushroomCow && entity.variant == MushroomCow.Variant.BROWN && itemstack.checkItemTag(Tag.ITEMS_SMALL_FLOWERS))
        }
        // 玩家手持桶且实体已成年时, 可交互
        // 玩家手持对应食物时, 可交互
        createInteractableEntity(
            listOf(
                EntityType.COW,
                EntityType.GOAT
            )
        ) { player, itemstack, entity ->
            (itemstack.checkItemType(ItemType.BUCKET) && entity.checkIsAdult()) || itemstack.checkIsCorrespondingFood(entity)
        }
        // 玩家手持刷子时且实体已成年时, 可交互
        // 玩家手持对应食物时, 可交互
        createInteractableEntity(EntityType.ARMADILLO) { player, itemstack, entity ->
            (itemstack.checkItemType(ItemType.BRUSH) && entity.checkIsAdult()) || itemstack.checkIsCorrespondingFood(entity)
        }
        // 若实体已成年且未装备鞍, 玩家手持鞍时, 可交互
        // 若实体已成年且已装备鞍, 玩家非潜行时, 可交互
        // 玩家手持对应食物时, 可交互
        createInteractableEntity(
            listOf(
                EntityType.PIG,
                EntityType.STRIDER
            )
        ) { player, itemstack, entity ->
            val flag = if (entity is Steerable && entity.checkIsAdult()) {
                if (entity.hasSaddle()) {
                    !player.isSneaking
                } else {
                    itemstack.checkItemType(ItemType.SADDLE)
                }
            } else {
                false
            }
            flag || itemstack.checkIsCorrespondingFood(entity)
        }
        // 若快乐恶魂已成年且未装备挽具, 玩家手持挽具时, 可交互
        // 若快乐恶魂已成年且已装备挽具, 玩家非潜行时, 可交互
        // 玩家手持对应食物时, 可交互
        createInteractableEntity(EntityType.HAPPY_GHAST) { player, itemstack, entity ->
            val flag = if (entity is HappyGhast && entity.isAdult) {
                val itemInBody = entity.equipment.getItem(EquipmentSlot.BODY)
                if (itemInBody.checkItemTag(Tag.ITEMS_HARNESSES)) {
                    !player.isSneaking
                } else {
                    itemstack.checkItemTag(Tag.ITEMS_HARNESSES)
                }
            } else {
                false
            }
            flag || itemstack.checkIsCorrespondingFood(entity)
        }
        // 玩家手持金苹果时, 可交互
        createInteractableEntity(EntityType.ZOMBIE_VILLAGER) { player, itemstack, entity ->
            itemstack.checkItemType(ItemType.GOLDEN_APPLE)
        }
        // 玩家手持金锭且猪灵非幼年、不处于交易中、且可交易时, 可交互
        createInteractableEntity(EntityType.PIGLIN) { player, itemstack, entity ->
            itemstack.checkItemType(ItemType.GOLD_INGOT) &&
                    entity is Piglin &&
                    entity.isAdult &&
                    entity.getMemory(MemoryKey.ADMIRING_ITEM) == false &&
                    entity.getMemory(MemoryKey.ADMIRING_DISABLED) == false
        }
        // 玩家手持铁锭且铁傀儡生命值不等于最大生命值时, 可交互
        createInteractableEntity(EntityType.IRON_GOLEM) { player, itemstack, entity ->
            itemstack.checkItemType(ItemType.IRON_INGOT) && entity is IronGolem && entity.health == entity.getAttribute(Attribute.MAX_HEALTH)?.value
        }
        // 玩家手持creeper_igniters标签物品时, 可交互
        createInteractableEntity(EntityType.CREEPER) { player, itemstack, entity ->
            itemstack.checkItemTag(Tag.ITEMS_CREEPER_IGNITERS)
        }
        // 玩家手持fishes标签物品时, 可交互
        createInteractableEntity(EntityType.DOLPHIN) { player, itemstack, entity ->
            itemstack.checkItemTag(Tag.ITEMS_FISHES)
        }
        // 实体已驯服时, 可交互
        createInteractableEntity(
            listOf(
                EntityType.SKELETON_HORSE,
                EntityType.ZOMBIE_HORSE
            )
        ) { player, itemstack, entity ->
            entity is Tameable && entity.isTamed
        }
        // 实体已驯服且玩家是其主人时, 可交互
        // 玩家手持对应食物时, 可交互
        createInteractableEntity(
            listOf(
                EntityType.CAT,
                EntityType.WOLF
            )
        ) { player, itemstack, entity ->
            (entity is Tameable && entity.isTamed && entity.ownerUniqueId == player.uniqueId) || itemstack.checkIsCorrespondingFood(entity)
        }
        // 实体已驯服且玩家是其主人时, 可交互
        // 玩家手持 #parrot_poisonous_food 标签物品时, 可交互
        // 玩家手持对应食物时, 可交互
        createInteractableEntity(EntityType.PARROT) { player, itemstack, entity ->
            (entity is Tameable && entity.isTamed && entity.ownerUniqueId == player.uniqueId) || itemstack.checkItemTag(Tag.ITEMS_PARROT_POISONOUS_FOOD) || itemstack.checkIsCorrespondingFood(entity)
        }
        // 悦灵手中无物品时, 可交互
        // 悦灵手中有物品且玩家手中无物品时, 可交互
        // 玩家手持 #duplicates_allays 标签物品且悦灵可复制时, 可交互
        createInteractableEntity(EntityType.ALLAY) { player, itemstack, entity ->
            if (entity !is Allay) return@createInteractableEntity false
            entity.equipment.itemInMainHand.isEmpty || itemstack.isEmpty || (itemstack.checkItemTag(Tag.ITEMS_DUPLICATES_ALLAYS) && entity.canDuplicate())
        }
        // 玩家非潜行交互载具时，可交互
        createInteractableEntity(
            listOf(
                EntityType.OAK_BOAT,
                EntityType.SPRUCE_BOAT,
                EntityType.BIRCH_BOAT,
                EntityType.JUNGLE_BOAT,
                EntityType.ACACIA_BOAT,
                EntityType.DARK_OAK_BOAT,
                EntityType.MANGROVE_BOAT,
                EntityType.CHERRY_BOAT,
                EntityType.PALE_OAK_BOAT,
                EntityType.BAMBOO_RAFT,
                EntityType.MINECART,
            )
        ) { player, itemstack, entity -> !player.isSneaking }
        // 玩家为 OP 且处于创造模式时, 可交互
        createInteractableEntity(
            EntityType.COMMAND_BLOCK_MINECART
        ) { player, itemstack, entity ->
            player.isOp && player.gameMode == GameMode.CREATIVE
        }
        // 任何情况下, 可交互
        createInteractableEntity(
            listOf(
                EntityType.CAMEL,
                EntityType.DONKEY,
                EntityType.GLOW_ITEM_FRAME,
                EntityType.HORSE,
                EntityType.INTERACTION,
                EntityType.ITEM_FRAME,
                EntityType.LLAMA,
                EntityType.MULE,
                EntityType.TRADER_LLAMA,
                EntityType.VILLAGER,
                EntityType.WANDERING_TRADER,
                EntityType.OAK_CHEST_BOAT,
                EntityType.SPRUCE_CHEST_BOAT,
                EntityType.BIRCH_CHEST_BOAT,
                EntityType.JUNGLE_CHEST_BOAT,
                EntityType.ACACIA_CHEST_BOAT,
                EntityType.DARK_OAK_CHEST_BOAT,
                EntityType.MANGROVE_CHEST_BOAT,
                EntityType.CHERRY_CHEST_BOAT,
                EntityType.PALE_OAK_CHEST_BOAT,
                EntityType.BAMBOO_CHEST_RAFT,
                EntityType.CHEST_MINECART,
                EntityType.FURNACE_MINECART,
                EntityType.HOPPER_MINECART
            )
        ) { player, itemstack, entity -> true }
    }

    private fun createInteractableEntity(entityType: EntityType, test: EntityInteractionTest) {
        INTERACTABLE_ENTITIES.put(entityType, test)?.let {
            LOGGER.warn("Duplicate interactable entity created: $entityType")
        }
    }

    private fun createInteractableEntity(entityTypes: List<EntityType>, test: EntityInteractionTest) {
        entityTypes.forEach { createInteractableEntity(it, test) }
    }

    /**
     * 方便函数.
     */
    private fun ItemStack.checkIsCorrespondingFood(entity: Entity): Boolean {
        val tag = Bukkit.getTag(Tag.REGISTRY_ITEMS, NamespacedKey.minecraft("${entity.type.name.lowercase()}_food"), Material::class.java) ?: return false
        return checkItemTag(tag)
    }

    /**
     * 方便函数.
     */
    private fun ItemStack.checkCanShear(entity: Entity): Boolean {
        return checkItemType(ItemType.SHEARS) && entity is Shearable && entity.readyToBeSheared()
    }

    /**
     * 方便函数.
     */
    private fun ItemStack.checkItemTag(tag: Tag<Material>): Boolean {
        return tag.isTagged(this.type)
    }

    /**
     * 方便函数.
     */
    private fun ItemStack.checkItemType(vararg itemTypes: ItemType): Boolean {
        return itemTypes.any { this.type.asItemType() == it }
    }

    /**
     * 方便函数.
     */
    private fun Entity.checkIsAdult(): Boolean {
        return this is Ageable && this.isAdult
    }
}

/**
 * 判定该情形下原版方块是否具有原版交互.
 *
 * 方块原版交互永远高于物品原版交互, 此时玩家手中物品的原版行为和自定义行为都不应该执行.
 *
 * 一些原版方块交互需要特定物品才能触发, 如剪刀和玻璃瓶交互蜂巢.
 * 这种情况我们认为原版方块可以交互 (实际上 NMS 的代码也是将这些交互写在方块中), 函数返回 true.
 * 这样设计意味着自定义物品的基底若能对特定方块触发方块交互, 则不会再执行自定义行为.
 *
 * 举例说明:
 * 设计一个"海带球"物品, 使用原版可堆肥的物品作为基底, 并添加使用后投掷出弹射物的自定义行为, 这样"海带球"在玩家右键堆肥桶的时候不会被投掷.
 * 那如果希望堆肥的时候触发自定义行为该怎么办呢? 应该新增一个 "CompostFeature", 而不是在交互行为里面去实现.
 */
// 2026/1/19 芙兰
// 新的需求与实现:
// 某些情景下, 我们可能希望添加优先级等同于"剪刀和玻璃瓶交互蜂巢"这种交互代码位于方块中的高优先交互.
// 因此, 我们不再一刀切 (即默认触发方块交互就不触发物品交互), 而是将"是否触发方块交互"作为布尔值传递到后续代码中,
// 让后续代码决定是否执行物品交互/是否取消事件.
fun Block.isInteractable(player: Player, itemstack: ItemStack, context: BlockInteractContext): Boolean {
    // 如果方块是自定义方块, 认为不可交互
    // 自定义方块的交互判定在 Koish 的交互判定之前, 代码执行到此处, 自定义方块的交互早已检查过
    // 如果当时自定义方块可交互, 代码不会执行到此处
    // 因此, 此时自定义方块方块必然不可交互
    if (UniversalBlocks.isCustomBlock(this)) return false
    val blockType = this.type.asBlockType() ?: return false // 不可能在此处return
    val test = INTERACTABLE_BLOCKS[blockType] ?: return false
    return test.test(player, itemstack, blockData, context)
}

/**
 * 判定该情形下实体是否具有原版交互.
 * 实体原版交互的优先级高于原版物品交互.
 * 此时玩家手中物品的自定义行为不应该执行.
 */
// 2026/1/19 芙兰
// 与上述方块交互类似, 实体交互也传递"是否触发实体交互"的标记.
fun Entity.isInteractable(player: Player, itemstack: ItemStack): Boolean {
    val test = INTERACTABLE_ENTITIES[this.type] ?: return false
    return test.test(player, itemstack, this)
}
