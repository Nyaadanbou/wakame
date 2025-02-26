// FIXME 该文件还未完善, 只能编译成功, 但无法正常使用
//  等实现 TileEntity (家具...) 的时候再完成这个文件

@file:Suppress("MemberVisibilityCanBePrivate")

package cc.mewcraft.wakame.context.param

import cc.mewcraft.wakame.api.block.KoishBlock
import cc.mewcraft.wakame.api.block.KoishBlockState
import cc.mewcraft.wakame.api.tileentity.TileEntity
import cc.mewcraft.wakame.context.intention.DefaultContextIntentions.BlockBreak
import cc.mewcraft.wakame.context.intention.DefaultContextIntentions.BlockInteract
import cc.mewcraft.wakame.context.intention.DefaultContextIntentions.BlockPlace
import cc.mewcraft.wakame.context.param.DefaultContextParamTypes.BLOCK_DROPS
import cc.mewcraft.wakame.context.param.DefaultContextParamTypes.BLOCK_ITEM_STACK
import cc.mewcraft.wakame.context.param.DefaultContextParamTypes.BLOCK_POS
import cc.mewcraft.wakame.context.param.DefaultContextParamTypes.BLOCK_STATE_NEKO
import cc.mewcraft.wakame.context.param.DefaultContextParamTypes.BLOCK_TYPE
import cc.mewcraft.wakame.context.param.DefaultContextParamTypes.BLOCK_TYPE_NEKO
import cc.mewcraft.wakame.context.param.DefaultContextParamTypes.BLOCK_TYPE_VANILLA
import cc.mewcraft.wakame.context.param.DefaultContextParamTypes.BLOCK_WORLD
import cc.mewcraft.wakame.context.param.DefaultContextParamTypes.INTERACTION_HAND
import cc.mewcraft.wakame.context.param.DefaultContextParamTypes.INTERACTION_ITEM_STACK
import cc.mewcraft.wakame.context.param.DefaultContextParamTypes.SOURCE_DIRECTION
import cc.mewcraft.wakame.context.param.DefaultContextParamTypes.SOURCE_ENTITY
import cc.mewcraft.wakame.context.param.DefaultContextParamTypes.SOURCE_LOCATION
import cc.mewcraft.wakame.context.param.DefaultContextParamTypes.SOURCE_PLAYER
import cc.mewcraft.wakame.context.param.DefaultContextParamTypes.SOURCE_TILE_ENTITY
import cc.mewcraft.wakame.context.param.DefaultContextParamTypes.SOURCE_UUID
import cc.mewcraft.wakame.context.param.DefaultContextParamTypes.SOURCE_WORLD
import cc.mewcraft.wakame.context.param.DefaultContextParamTypes.TOOL_ITEM_STACK
import cc.mewcraft.wakame.util.item.takeUnlessEmpty
import cc.mewcraft.wakame.world.BlockPos
import net.minecraft.resources.ResourceLocation
import org.bukkit.*
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.*

object DefaultContextParamTypes {

    /**
     * The position of a block.
     *
     * Required in intentions:
     * - [BlockPlace]
     * - [BlockBreak]
     * - [BlockInteract]
     *
     * Optional in intentions: none
     *
     * Autofilled by: none
     *
     * Autofills:
     * - [BLOCK_WORLD]
     * - [BLOCK_DROPS] with and without [TOOL_ITEM_STACK]
     */
    val BLOCK_POS: ContextParamType<BlockPos> =
        ContextParamType.builder<BlockPos>("block_pos")
            .requiredIn(BlockPlace, BlockBreak, BlockInteract)
            .build()

    /**
     * The world of a block.
     *
     * Required in intentions:
     * - [BlockPlace]
     * - [BlockBreak]
     * - [BlockInteract]
     *
     * Optional in intentions: none
     *
     * Autofilled by:
     * - [BLOCK_POS]
     *
     * Autofills: none
     */
    val BLOCK_WORLD: ContextParamType<World> =
        ContextParamType.builder<World>("block_world")
            .requiredIn(BlockPlace, BlockBreak, BlockInteract)
            .autofilledBy(::BLOCK_POS) { it.world }
            .build()

    /**
     * The custom block type.
     *
     * Required in intentions: none
     *
     * Optional in intentions:
     * - [BlockPlace]
     * - [BlockBreak]
     * - [BlockInteract]
     *
     * Autofilled by:
     * - [BLOCK_TYPE] if Neko block
     *
     * Autofills:
     * - [BLOCK_STATE_NEKO]
     * - [BLOCK_TYPE]
     */
    val BLOCK_TYPE_NEKO: ContextParamType<KoishBlock> =
        ContextParamType.builder<KoishBlock>("block_type_neko")
            .optionalIn(BlockPlace, BlockBreak, BlockInteract)
            // FIXME
            // .autofilledBy(::BLOCK_TYPE) { NekoRegistries.BLOCK[it] }
            .autofilledBy(::BLOCK_STATE_NEKO) { it.block }
            .build()

    /**
     * The custom block state.
     *
     * Required in intentions: none
     *
     * Optional in intentions:
     * - [BlockPlace]
     * - [BlockBreak]
     * - [BlockInteract]
     *
     * Autofilled by:
     * - [BLOCK_TYPE_NEKO]
     *
     * Autofills:
     * - [BLOCK_TYPE_NEKO]
     */
    val BLOCK_STATE_NEKO: ContextParamType<KoishBlockState> =
        ContextParamType.builder<KoishBlockState>("block_state_neko")
            .optionalIn(BlockPlace, BlockBreak, BlockInteract)
            // FIXME
            // .autofilledBy(::BLOCK_TYPE_NEKO) { it.defaultBlockState }
            .build()

    /**
     * The neko tile-entity of a block.
     *
     * Required in intentions: none
     *
     * Optional in intentions:
     * - [BlockBreak]
     * - [BlockInteract]
     *
     * Autofilled by:
     * - [BLOCK_POS]
     *
     * Autofills: none
     */
    val TILE_ENTITY_NEKO: ContextParamType<TileEntity> =
        ContextParamType.builder<TileEntity>("tile_entity_neko")
            .optionalIn(BlockBreak, BlockInteract)
            // FIXME
            // .autofilledBy(::BLOCK_POS) { WorldDataManager.getTileEntity(it) }
            .build()

    // FIXME
    // /**
    //  * The tile-entity data of a neko tile-entity.
    //  *
    //  * Required in intentions: none
    //  *
    //  * Optional in intentions:
    //  * - [BlockPlace]
    //  *
    //  * Autofilled by:
    //  * - [BLOCK_ITEM_STACK] if data is present
    //  *
    //  * Autofills: none
    //  */
    // val TILE_ENTITY_DATA_NEKO: ContextParamType<CompoundTag> =
    //     ContextParamType.builder<Compound>("tile_entity_data_neko")
    //         .optionalIn(BlockPlace)
    //         .copiedBy(Compound::copy)
    //         .autofilledBy(::BLOCK_ITEM_STACK) { itemStack ->
    //             itemStack.nekoCompound
    //                 ?.get<Compound>(TileEntity.TILE_ENTITY_DATA_KEY)
    //                 ?.let { persistentData -> Compound().also { it["persistent"] = persistentData } }
    //         }.build()

    /**
     * The vanilla block type.
     *
     * Required in intentions: none
     *
     * Optional in intentions:
     * - [BlockPlace]
     * - [BlockBreak]
     * - [BlockInteract]
     *
     * Autofilled by:
     * - [BLOCK_TYPE] if vanilla block
     *
     * Autofills:
     * - [BLOCK_TYPE]
     */
    val BLOCK_TYPE_VANILLA: ContextParamType<Material> =
        ContextParamType.builder<Material>("block_type_vanilla")
            .optionalIn(BlockPlace, BlockBreak, BlockInteract)
            .require({ it.isBlock }, { "$it is not a block" })
            // FIXME
            // .autofilledBy(::BLOCK_TYPE) { BuiltInRegistries.BLOCK.getOptional(it).getOrNull()?.bukkitMaterial }
            .build()

    // TODO: block state vanilla
    // TODO: tile entity vanilla

    /**
     * The block type as id.
     *
     * Required in intentions: none
     *
     * Optional in intentions:
     * - [BlockPlace]
     * - [BlockBreak]
     * - [BlockInteract]
     *
     * Autofilled by:
     * - [BLOCK_TYPE_NEKO]
     * - [BLOCK_TYPE_VANILLA]
     * - [BLOCK_ITEM_STACK]
     *
     * Autofills:
     * - [BLOCK_TYPE_NEKO] if Neko block
     * - [BLOCK_TYPE_VANILLA] if vanilla block
     */
    val BLOCK_TYPE: ContextParamType<ResourceLocation> =
        ContextParamType.builder<ResourceLocation>("block_type")
            .optionalIn(BlockPlace, BlockBreak, BlockInteract)
            // FIXME
            // .autofilledBy(::BLOCK_TYPE_NEKO) { it.id }
            // .autofilledBy(::BLOCK_TYPE_VANILLA) { BuiltInRegistries.BLOCK.getKey(it.nmsBlock) }
            // .autofilledBy(::BLOCK_ITEM_STACK) { ResourceLocation.parse(ItemUtils.getId(it)) }
            .build()

    /**
     * The face of a block that was clicked.
     *
     * Required in intentions: none
     *
     * Optional in intentions:
     * - [BlockPlace]
     * - [BlockBreak]
     * - [BlockInteract]
     *
     * Autofilled by:
     * - [SOURCE_PLAYER]
     *
     * Autofills: none
     */
    val CLICKED_BLOCK_FACE: ContextParamType<BlockFace> =
        ContextParamType.builder<BlockFace>("clicked_block_face")
            .optionalIn(BlockPlace, BlockBreak, BlockInteract)
            // FIXME
            // .autofilledBy(::SOURCE_PLAYER) { BlockFaceUtils.determineBlockFaceLookingAt(it.eyeLocation) }
            .build()

    /**
     * The hand that was used to interact.
     *
     * Required in intentions: none
     *
     * Optional in intentions:
     * - [BlockPlace]
     * - [BlockBreak]
     * - [BlockInteract]
     *
     * Autofilled by: none
     *
     * Autofills:
     * - [BLOCK_ITEM_STACK] with [SOURCE_ENTITY]
     * - [TOOL_ITEM_STACK] with [SOURCE_ENTITY]
     * - [INTERACTION_ITEM_STACK] with [SOURCE_ENTITY]
     */
    val INTERACTION_HAND: ContextParamType<EquipmentSlot> =
        ContextParamType.builder<EquipmentSlot>("interaction_hand")
            .optionalIn(BlockInteract)
            .build()

    /**
     * The item stack to be placed as a block.
     *
     * Required in intentions: none
     *
     * Optional in intentions:
     * - [BlockPlace]
     *
     * Autofilled by:
     * - [SOURCE_ENTITY] and [INTERACTION_HAND]
     * - [BLOCK_TYPE_NEKO] if the block has an item type
     * - [BLOCK_TYPE_VANILLA] if the block has an item type
     *
     * Autofills:
     * - [TILE_ENTITY_DATA_NEKO] if data is present
     * - [BLOCK_TYPE_NEKO] if data is present
     */
    val BLOCK_ITEM_STACK: ContextParamType<ItemStack> =
        ContextParamType.builder<ItemStack>("block_item_stack")
            .optionalIn(BlockPlace)
            .copiedBy(ItemStack::clone)
            // TODO: Validate if item stack represents block. This is currently not supported by CustomItemServices.
            .autofilledBy(::SOURCE_ENTITY, ::INTERACTION_HAND) { entity, hand ->
                (entity as? LivingEntity)?.equipment?.getItem(hand)?.takeUnless { it.isEmpty || !it.type.isBlock }
            }
            .autofilledBy(::BLOCK_TYPE_NEKO) { it.item?.createItemStack() }
            .autofilledBy(::BLOCK_TYPE_VANILLA) { if (it.isBlock) ItemStack(it) else null }
            .build()

    /**
     * The item stack used as a tool.
     *
     * Required in intentions: none
     *
     * Optional in intentions:
     * - [BlockBreak]
     *
     * Autofilled by:
     * - [SOURCE_ENTITY] and [INTERACTION_HAND]
     *
     * Autofills:
     * - [BLOCK_DROPS] with [BLOCK_POS]
     */
    val TOOL_ITEM_STACK: ContextParamType<ItemStack> =
        ContextParamType.builder<ItemStack>("tool_item_stack")
            .optionalIn(BlockBreak)
            .copiedBy(ItemStack::clone)
            .autofilledBy(::SOURCE_ENTITY) { entity ->
                (entity as? LivingEntity)?.equipment?.itemInMainHand?.takeUnlessEmpty()
            }
            .build()

    /**
     * The item stack used to interact with a something.
     *
     * Required in intentions: none
     *
     * Optional in intentions:
     * - [BlockInteract]
     *
     * Autofilled by:
     * - [SOURCE_ENTITY] and [INTERACTION_HAND]
     *
     * Autofills: none
     */
    val INTERACTION_ITEM_STACK: ContextParamType<ItemStack> =
        ContextParamType.builder<ItemStack>("interaction_item_stack")
            .optionalIn(BlockInteract)
            .autofilledBy(::SOURCE_ENTITY, ::INTERACTION_HAND) { entity, hand ->
                (entity as? LivingEntity)?.equipment?.getItem(hand)?.takeUnlessEmpty()
            }
            .build()

    /**
     * The [UUID] of the source of an action.
     *
     * Required in intentions: none
     *
     * Optional in intentions:
     * - [BlockPlace]
     * - [BlockBreak]
     * - [BlockInteract]
     *
     * Autofilled by
     * - [SOURCE_ENTITY]
     * - [SOURCE_TILE_ENTITY]
     *
     * Autofills: none
     */
    val SOURCE_UUID: ContextParamType<UUID> =
        ContextParamType.builder<UUID>("source_uuid")
            .optionalIn(BlockPlace, BlockBreak, BlockInteract)
            .autofilledBy(::SOURCE_ENTITY) { it.uniqueId }
            // FIXME
            // .autofilledBy(::SOURCE_TILE_ENTITY) { it.uuid }
            .build()

    /**
     * The location of the source of an action.
     *
     * Required in intentions: none
     *
     * Optional in intentions:
     * - [BlockPlace]
     * - [BlockBreak]
     * - [BlockInteract]
     *
     * Autofilled by:
     * - [SOURCE_ENTITY]
     * - [SOURCE_TILE_ENTITY]
     *
     * Autofills:
     * - [SOURCE_WORLD]
     * - [SOURCE_DIRECTION]
     */
    val SOURCE_LOCATION: ContextParamType<Location> =
        ContextParamType.builder<Location>("source_location")
            .optionalIn(BlockPlace, BlockBreak, BlockInteract)
            .copiedBy(Location::clone)
            .autofilledBy(::SOURCE_ENTITY) { it.location }
            // FIXME
            // .autofilledBy(::SOURCE_TILE_ENTITY) { tileEntity ->
            //     val pos = tileEntity.pos
            //     val facing = tileEntity.blockState[DefaultBlockStateProperties.FACING]
            //     return@autofilledBy if (facing != null) {
            //         Location(pos.world, pos.x, pos.y, pos.z, facing.yaw, facing.pitch)
            //     } else pos.location
            // }
            .build()

    /**
     * The world of the source of an action.
     *
     * Required in intentions: none
     *
     * Optional in intentions:
     * - [BlockPlace]
     * - [BlockBreak]
     * - [BlockInteract]
     *
     * Autofilled by:
     * - [SOURCE_LOCATION]
     *
     * Autofills: none
     */
    val SOURCE_WORLD: ContextParamType<World> =
        ContextParamType.builder<World>("source_world")
            .optionalIn(BlockPlace, BlockBreak, BlockInteract)
            .autofilledBy(::SOURCE_LOCATION) { it.world }
            .build()

    /**
     * The direction that the source of an action is facing.
     *
     * Required in intentions: none
     *
     * Optional in intentions:
     * - [BlockPlace]
     * - [BlockBreak]
     * - [BlockInteract]
     *
     * Autofilled by:
     * - [SOURCE_LOCATION]
     *
     * Autofills: none
     */
    val SOURCE_DIRECTION: ContextParamType<Vector> =
        ContextParamType.builder<Vector>("source_direction")
            .optionalIn(BlockPlace, BlockBreak, BlockInteract)
            .copiedBy(Vector::clone)
            .autofilledBy(::SOURCE_LOCATION) { it.direction }
            .build()

    /**
     * The player that is the source of an action.
     *
     * Required in intentions: none
     *
     * Optional in intentions:
     * - [BlockPlace]
     * - [BlockBreak]
     * - [BlockInteract]
     *
     * Autofilled by:
     * - [SOURCE_ENTITY] if player
     */
    val SOURCE_PLAYER: ContextParamType<Player> =
        ContextParamType.builder<Player>("source_player")
            .optionalIn(BlockPlace, BlockBreak, BlockInteract)
            .autofilledBy(::SOURCE_ENTITY) { it as? Player }
            .build()

    /**
     * The entity that is the source of an action.
     *
     * Required in intentions: none
     *
     * Optional in intentions:
     * - [BlockPlace]
     * - [BlockBreak]
     * - [BlockInteract]
     *
     * Autofilled by:
     * - [SOURCE_PLAYER]
     *
     * Autofills:
     * - [SOURCE_UUID]
     * - [SOURCE_LOCATION]
     * - [SOURCE_PLAYER] if player
     * - [BLOCK_ITEM_STACK] with [INTERACTION_HAND]
     * - [TOOL_ITEM_STACK] with [INTERACTION_HAND]
     * - [INTERACTION_ITEM_STACK] with [INTERACTION_HAND]
     */
    val SOURCE_ENTITY: ContextParamType<Entity> =
        ContextParamType.builder<Entity>("source_entity")
            .optionalIn(BlockPlace, BlockBreak, BlockInteract)
            .autofilledBy(::SOURCE_PLAYER) { it }
            .build()

    /**
     * The [TileEntity] that is the source of an action.
     *
     * Required in intentions: none
     *
     * Optional in intentions:
     * - [BlockPlace]
     * - [BlockBreak]
     * - [BlockInteract]
     *
     * Autofilled by: none
     *
     * Autofills:
     * - [SOURCE_UUID]
     * - [SOURCE_LOCATION]
     */
    val SOURCE_TILE_ENTITY: ContextParamType<TileEntity> =
        ContextParamType.builder<TileEntity>("source_tile_entity")
            .optionalIn(BlockPlace, BlockBreak, BlockInteract)
            .build()

    /**
     * The player that is either the direct source or responsible for the action.
     *
     * Required in intentions: none
     *
     * Optional in intentions:
     * - [BlockPlace]
     * - [BlockBreak]
     * - [BlockInteract]
     *
     * Autofilled by:
     * - [SOURCE_ENTITY] if offline player
     * - [SOURCE_TILE_ENTITY] if owner present
     *
     * Autofills: none
     */
    val RESPONSIBLE_PLAYER: ContextParamType<OfflinePlayer> =
        ContextParamType.builder<OfflinePlayer>("responsible_player")
            .optionalIn(BlockPlace, BlockBreak, BlockInteract)
            .autofilledBy(::SOURCE_ENTITY) { it as? OfflinePlayer }
            .autofilledBy(::SOURCE_TILE_ENTITY) { it.owner }
            .build()

    /**
     * Whether block drops should be dropped.
     * Defaults to `false`.
     *
     * Required in intentions: none
     *
     * Optional in intentions:
     * - [BlockBreak]
     *
     * Autofilled by:
     * - [BLOCK_POS] with and without [TOOL_ITEM_STACK]
     *
     * Autofills: none
     */
    val BLOCK_DROPS: DefaultingContextParamType<Boolean> =
        ContextParamType.builder<Boolean>("block_drops")
            .optionalIn(BlockBreak)
            // FIXME
            // .autofilledBy(::BLOCK_POS, ::TOOL_ITEM_STACK) { pos, tool -> ToolUtils.isCorrectToolForDrops(pos.block, tool) }
            // .autofilledBy(::BLOCK_POS) { ToolUtils.isCorrectToolForDrops(it.block, null) }
            .build(false)

    /**
     * Whether block storage drops should be dropped.
     * Defaults to `true`
     *
     * Required in intentions: none
     *
     * Optional in intentions:
     * - [BlockBreak]
     *
     * Autofilled by:
     * - [SOURCE_PLAYER]
     *
     * Autofills: none
     */
    val BLOCK_STORAGE_DROPS: DefaultingContextParamType<Boolean> =
        ContextParamType.builder<Boolean>("block_storage_drops")
            .optionalIn(BlockBreak)
            .autofilledBy(::SOURCE_PLAYER) { it.gameMode != GameMode.CREATIVE }
            .build(true)

    /**
     * Whether block place effects should be played.
     * Defaults to `true`.
     *
     * Required in intentions: none
     *
     * Optional in intentions:
     * - [BlockPlace]
     *
     * Autofilled by: none
     *
     * Autofills: none
     */
    val BLOCK_PLACE_EFFECTS: DefaultingContextParamType<Boolean> =
        ContextParamType.builder<Boolean>("block_place_effects")
            .optionalIn(BlockPlace)
            .build(true)

    /**
     * Whether block break effects should be played.
     * Defaults to `true`
     *
     * Required in intentions: none
     *
     * Optional in intentions:
     * - [BlockBreak]
     *
     * Autofilled by: none
     *
     * Autofills: none
     */
    val BLOCK_BREAK_EFFECTS: DefaultingContextParamType<Boolean> =
        ContextParamType.builder<Boolean>("block_break_effects")
            .optionalIn(BlockBreak)
            .build(true)

    /**
     * Whether tile-entity limits should be bypassed when placing tile-entity blocks.
     * Placed blocks will still be counted.
     * Defaults to `false`.
     *
     * Required in intentions: none
     *
     * Optional in intentions:
     * - [BlockPlace]
     *
     * Autofilled by: none
     *
     * Autofills: none
     */
    val BYPASS_TILE_ENTITY_LIMITS: DefaultingContextParamType<Boolean> =
        ContextParamType.builder<Boolean>("bypass_tile_entity_limits")
            .optionalIn(BlockPlace)
            .build(false)

}