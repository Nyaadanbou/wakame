package cc.mewcraft.wakame.item.feature

import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.*
import cc.mewcraft.wakame.util.item.toNMS
import net.minecraft.core.BlockPos
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.phys.Vec3
import org.bukkit.Location
import org.bukkit.damage.DamageSource
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack

/**
 * 实现了物品进行特定行为时触发额外战利品.
 */
@Init(
    stage = InitStage.POST_WORLD,
)
object ExtraLootFeature : Listener {

    init {
        registerEvents()
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun on(event: BlockBreakEvent) {
        val player = event.player

        val itemInMainHand = player.inventory.itemInMainHand
        if (itemInMainHand.isEmpty) return
        val extraLoot = itemInMainHand.getProp(ItemPropTypes.EXTRA_LOOT) ?: return
        val block = event.block

        // 匹配第一个符合条件的额外战利品
        extraLoot.breakBlock.firstOrNull {
            it.matches(block)
        }?.dropItemsNaturally(
            generatePlayerBreakBlockLootParams(block.location, itemInMainHand, player),
            block.location.add(0.5, 0.5, 0.5)
        )
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun on(event: EntityDeathEvent) {
        val player = event.damageSource.causingEntity as? Player ?: return
        val deceased = event.entity

        val itemInMainHand = player.inventory.itemInMainHand
        if (itemInMainHand.isEmpty) return
        val extraLoot = itemInMainHand.getProp(ItemPropTypes.EXTRA_LOOT) ?: return

        // 匹配第一个符合条件的额外战利品
        extraLoot.killEntity.firstOrNull {
            it.matches(deceased)
        }?.dropItemsNaturally(
            generatePlayerKillEntityLootParams(deceased, event.damageSource, player),
            deceased.location
        )
    }

    /**
     * 生成玩家破坏方块时的战利品上下文, 逻辑与 nms 一致.
     */
    private fun generatePlayerBreakBlockLootParams(blockLocation: Location, itemInMainHand: ItemStack, player: Player): MojangLootParams {
        val serverLevel = blockLocation.world.serverLevel
        val blockPos = BlockPos(blockLocation.blockX, blockLocation.blockY, blockLocation.blockZ)
        return MojangLootParamsBuilder(serverLevel)
            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos))
            .withParameter(LootContextParams.TOOL, itemInMainHand.toNMS().copy()) // 服务端代码如此, 此处进行了拷贝
            .withOptionalParameter(LootContextParams.THIS_ENTITY, player.serverPlayer)
            .withOptionalParameter(LootContextParams.BLOCK_ENTITY, serverLevel.getBlockEntity(blockPos))
            .withParameter(LootContextParams.BLOCK_STATE, serverLevel.getBlockState(blockPos))
            .create(LootContextParamSets.BLOCK)
    }

    /**
     * 生成玩家杀死生物时的战利品上下文, 逻辑与 nms 一致.
     */
    private fun generatePlayerKillEntityLootParams(deceased: LivingEntity, damageSource: DamageSource, player: Player): MojangLootParams {
        val serverLevel = deceased.world.serverLevel
        val mojangEntity = deceased.handle
        val serverPlayer = player.serverPlayer
        return MojangLootParamsBuilder(serverLevel)
            .withParameter(LootContextParams.THIS_ENTITY, mojangEntity)
            .withParameter(LootContextParams.ORIGIN, mojangEntity.position())
            .withParameter(LootContextParams.DAMAGE_SOURCE, damageSource.handle)
            .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, damageSource.causingEntity?.handle)
            .withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, damageSource.directEntity?.handle)
            .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, serverPlayer)
            .withLuck(serverPlayer.luck)
            .create(LootContextParamSets.ENTITY)
    }
}