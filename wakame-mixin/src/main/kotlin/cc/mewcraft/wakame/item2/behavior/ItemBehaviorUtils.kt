package cc.mewcraft.wakame.item2.behavior

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.item2.behavior.InteractableBlocks.INTERACTABLE_BLOCKS
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import org.bukkit.GameMode
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockType
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.Beehive
import org.bukkit.block.data.type.Bell
import org.bukkit.block.data.type.CaveVines
import org.bukkit.block.data.type.ChiseledBookshelf
import org.bukkit.block.data.type.Jukebox
import org.bukkit.block.data.type.RespawnAnchor
import org.bukkit.block.data.type.Vault
import org.bukkit.craftbukkit.block.impl.CraftComposter
import org.bukkit.craftbukkit.block.impl.CraftSweetBerryBush
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ItemType
import org.joml.Vector3d

data class UseOnContext(
    val player: Player,
    val world: World,
    val hand: InteractionHand,
    val itemStack: ItemStack,
    val interactContext: BlockInteractContext
) {
    constructor(player: Player, hand: InteractionHand, itemStack: ItemStack, interactContext: BlockInteractContext) : this(
        player, player.world, hand, itemStack, interactContext
    )
}

data class UseContext(
    val player: Player,
    val world: World,
    val hand: InteractionHand,
    val itemStack: ItemStack,
) {
    constructor(player: Player, hand: InteractionHand, item: ItemStack) : this(
        player, player.world, hand, item
    )
}

data class UseEntityContext(
    val player: Player,
    val world: World,
    val hand: InteractionHand,
    val itemStack: ItemStack,
    val entity: Entity
) {
    constructor(player: Player, hand: InteractionHand, item: ItemStack, entity: Entity) : this(
        player, player.world, hand, item, entity
    )
}

data class AttackOnContext(
    val player: Player,
    val world: World,
    val itemStack: ItemStack,
    val interactContext: BlockInteractContext
) {
    constructor(player: Player, itemStack: ItemStack, interactContext: BlockInteractContext) : this(
        player, player.world, itemStack, interactContext
    )
}

data class AttackContext(
    val player: Player,
    val world: World,
    val itemStack: ItemStack,
) {
    constructor(player: Player, item: ItemStack) : this(
        player, player.world, item
    )
}

data class AttackEntityContext(
    val player: Player,
    val world: World,
    val itemStack: ItemStack,
    val entity: Entity
) {
    constructor(player: Player,  item: ItemStack, entity: Entity) : this(
        player, player.world, item, entity
    )
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
     * 此时不会取消事件.
     */
    SUCCESS_NO_CANCEL,

    /**
     * 交互成功.
     * 此时代码中断, 不会再执行后续的任何交互行为.
     * 此时取消事件.
     */
    SUCCESS,

    /**
     * 交互失败.
     * 此时代码中断, 不会再执行后续的任何交互行为.
     * 此时取消事件.
     */
    FAIL,

    /**
     * 跳过交互.
     * 此时仍然会继续尝试执行后续的交互行为.
     */
    PASS,
}

fun InteractionResult.isSuccess(): Boolean {
    return this == InteractionResult.SUCCESS || this == InteractionResult.SUCCESS_NO_CANCEL
}

fun InteractionResult.shouldCancel(): Boolean {
    return this == InteractionResult.FAIL || this == InteractionResult.SUCCESS
}

private object InteractableBlocks {
    val INTERACTABLE_BLOCKS: Reference2ObjectMap<BlockType, (player: Player, itemStack: ItemStack, blockData: BlockData, interactContext: BlockInteractContext) -> Boolean> = Reference2ObjectOpenHashMap()

    // 该列表并不全面, 但基本够用
    // 例如, 未考虑: 可参与营火配方的物品会触发营火的方块交互, 炼药锅的各种交互
    // 这些交互都很复杂, 但共同点是都有特定的物品要求
    // 也就是说, 只要不用这些特定的物品做自定义物品的基底, 就没有任何影响
    init {
        // 玩家可食用蛋糕方块时, 方块可交互
        // 该判断逻辑与nms一致
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
        ) { player, itemStack, blockData, interactContext ->
            player.isInvulnerable || player.foodLevel < 20
        }
        // 玩家为op且处于创造模式时, 方块可交互
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
        ) { player, itemStack, blockData, interactContext ->
            player.isOp && player.gameMode == GameMode.CREATIVE
        }
        // 玩家手持光源方块物品时, 方块可交互
        createInteractableBlock(BlockType.LIGHT) { player, itemStack, blockData, interactContext ->
            itemStack.checkItemType(ItemType.LIGHT)
        }
        // 玩家手持指南针时, 方块可交互
        createInteractableBlock(BlockType.LODESTONE) { player, itemStack, blockData, interactContext ->
            itemStack.checkItemType(ItemType.COMPASS)
        }
        // 玩家手持剪刀时, 方块可交互
        createInteractableBlock(BlockType.PUMPKIN) { player, itemStack, blockData, interactContext ->
            itemStack.checkItemType(ItemType.SHEARS)
        }
        // 玩家手持桶时, 方块可交互
        createInteractableBlock(BlockType.POWDER_SNOW) { player, itemStack, blockData, interactContext ->
            itemStack.checkItemType(ItemType.BUCKET)
        }
        // 玩家手持打火石或火焰弹时, 方块可交互
        createInteractableBlock(BlockType.TNT) { player, itemStack, blockData, interactContext ->
            itemStack.checkItemType(ItemType.FLINT_AND_STEEL, ItemType.FIRE_CHARGE)
        }
        // 玩家手持剪刀或玻璃瓶且蜂巢蜂蜜等级大于等于5时, 方块可交互
        createInteractableBlocks(
            listOf(BlockType.BEEHIVE, BlockType.BEE_NEST)
        ) { player, itemStack, blockData, interactContext ->
            itemStack.checkItemType(ItemType.SHEARS, ItemType.GLASS_BOTTLE) && blockData is Beehive && blockData.honeyLevel >= blockData.maximumHoneyLevel
        }
        // 玩家交互钟特定侧面的特定位置时, 方块可交互
        createInteractableBlock(BlockType.BELL) { player, itemStack, blockData, interactContext ->
            val blockPosition = interactContext.blockPosition
            if (blockData is Bell) {
                val interactFace = interactContext.interactFace
                val isSide = when (interactFace) {
                    BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST -> true
                    else -> false
                }
                val deltaY = interactContext.interactPoint.y - blockPosition.y
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
                        Bell.Attachment.DOUBLE_WALL -> {
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
        createInteractableBlock(BlockType.CHISELED_BOOKSHELF) { player, itemStack, blockData, interactContext ->
            blockData is ChiseledBookshelf && blockData.facing == interactContext.interactFace
        }
        // 玩家手持可堆肥物品时, 方块可交互
        // 堆肥桶堆满时, 方块可交互
        createInteractableBlock(BlockType.COMPOSTER) { player, itemStack, blockData, interactContext ->
            (blockData is CraftComposter && blockData.level == blockData.maximumLevel) || itemStack.type.isCompostable
        }
        // 重生锚充能等级大于0时, 方块可交互
        // 玩家手持荧石块时, 方块可交互
        createInteractableBlock(BlockType.RESPAWN_ANCHOR) { player, itemStack, blockData, interactContext ->
            (blockData is RespawnAnchor && blockData.charges > 0) || itemStack.checkItemType(ItemType.GLOWSTONE)
        }
        // 唱片机内有唱片时, 方块可交互
        createInteractableBlock(BlockType.JUKEBOX) { player, itemStack, blockData, interactContext ->
            blockData is Jukebox && blockData.hasRecord()
        }
        // 甜浆果丛age为2和3时, 方块可交互
        // 玩家手持骨粉且age小于3时, 方块可交互
        createInteractableBlock(BlockType.JUKEBOX) { player, itemStack, blockData, interactContext ->
            blockData is CraftSweetBerryBush && (blockData.age >= 2 || (blockData.age < 3 && itemStack.checkItemType(ItemType.BONE_MEAL)))
        }
        // 洞穴藤蔓有浆果时, 方块可交互
        // 玩家手持骨粉且无浆果时, 方块可交互
        createInteractableBlock(BlockType.JUKEBOX) { player, itemStack, blockData, interactContext ->
            blockData is CaveVines && (blockData.hasBerries() || itemStack.checkItemType(ItemType.BONE_MEAL))
        }
        // 宝库处于active状态时, 方块可交互
        createInteractableBlock(BlockType.VAULT) { player, itemStack, blockData, interactContext ->
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
        ) { player, itemStack, blockData, interactContext -> true }
    }

    private fun createInteractableBlock(blockType: BlockType, checkLogic: (player: Player, itemStack: ItemStack, blockData: BlockData, interactContext: BlockInteractContext) -> Boolean) {
        INTERACTABLE_BLOCKS.put(blockType, checkLogic)?.let {
            LOGGER.warn("Duplicate interactable block created: $blockType")
        }
    }

    private fun createInteractableBlocks(blockTypes: List<BlockType>, checkLogic: (player: Player, itemStack: ItemStack, blockData: BlockData, interactContext: BlockInteractContext) -> Boolean) {
        blockTypes.forEach { createInteractableBlock(it, checkLogic) }
    }

    /**
     * 方便函数.
     */
    private fun ItemStack.checkItemType(vararg itemTypes: ItemType): Boolean {
        return itemTypes.any { this.type.asItemType() == it }
    }
}

/**
 * 判定该原版方块是否具有原版交互.
 * 方块原版交互的优先级最高, 触发时, 玩家手中物品的原版行为和自定义行为都不应该执行.
 *
 * 一些原版方块交互需要特定物品才能触发, 如剪刀和玻璃瓶交互蜂巢.
 * 这种情况我们认为原版方块可以交互(实际上nms的代码也是将这些交互写在方块中), 函数返回 true.
 * 这样设计意味着自定义物品的基底若能对特定方块触发方块交互, 则不会再执行自定义行为.
 * 举例说明:
 * 设计一个"海带球"物品, 使用原版可堆肥的物品作为基底, 并添加使用后投掷出弹射物的自定义行为, 这样"海带球"在玩家右键堆肥桶的时候不会被投掷.
 * 那如果希望堆肥的时候触发自定义行为该怎么办呢? 应该新增一个handlePlayerCompostItem(...), 而不是在交互行为里面去实现.
 */
fun Block.isInteractable(player: Player, itemStack: ItemStack, interactContext: BlockInteractContext): Boolean {
    val blockType = this.type.asBlockType() ?: return false // 不可能在此处return
    val checkLogic = INTERACTABLE_BLOCKS[blockType] ?: return false
    return checkLogic(player, itemStack, blockData, interactContext)
}
