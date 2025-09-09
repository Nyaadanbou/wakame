package cc.mewcraft.wakame.item2.behavior.impl

import cc.mewcraft.wakame.event.bukkit.WrappedPlayerInteractEvent
import cc.mewcraft.wakame.item2.*
import cc.mewcraft.wakame.item2.behavior.ItemBehavior
import cc.mewcraft.wakame.item2.config.property.ItemPropertyTypes
import cc.mewcraft.wakame.item2.data.ItemDataTypes
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.CustomModelData
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.block.BlockFace
import org.bukkit.entity.*
import org.bukkit.event.block.Action
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

/**
 * 用桶捕捉生物的逻辑.
 */
object EntityBucket : ItemBehavior {

    // 当玩家手持一个生物桶右键方块顶部时
    override fun handleInteract(player: Player, itemstack: ItemStack, action: Action, wrappedEvent: WrappedPlayerInteractEvent) {
        val entityData = itemstack.getData(ItemDataTypes.ENTITY_BUCKET_DATA) ?: return

        val loc = wrappedEvent.event.interactionPoint
        if (loc == null) {
            return // 没有交互点
        }

        if (!wrappedEvent.event.action.isRightClick /*|| !hasEntityBucketBehavior(itemstack)*/) {
            return // 不是右键点击
        }

        if (wrappedEvent.event.blockFace != BlockFace.UP) {
            return // 不是点击的方块顶部
        }

        if (wrappedEvent.event.clickedBlock == null) {
            return // 没有点击方块 (什么情况下 BlockFace 不为 null 但 clickedBlock 为 null ?)
        }

        wrappedEvent.event.isCancelled = true
        wrappedEvent.actionPerformed = true

        val deserializedEntity = Bukkit.getUnsafe().deserializeEntity(entityData, player.world)
        deserializedEntity.spawnAt(loc, CreatureSpawnEvent.SpawnReason.BUCKET)

        // 还原物品状态
        if (player.gameMode != GameMode.CREATIVE) {
            itemstack.resetData(DataComponentTypes.CUSTOM_MODEL_DATA)
            itemstack.resetData(DataComponentTypes.CUSTOM_NAME)
            itemstack.resetData(DataComponentTypes.MAX_STACK_SIZE)
            itemstack.removeData(ItemDataTypes.ENTITY_BUCKET_DATA)
        }

        // TODO 播放交互音效
    }

    // 当玩家手持一个生物桶右键生物时
    override fun handleInteractAtEntity(player: Player, itemstack: ItemStack, clicked: Entity, event: PlayerInteractAtEntityEvent) {
        val entityBucket = itemstack.getProp(ItemPropertyTypes.ENTITY_BUCKET) ?: return
        val entityBucketData = itemstack.getData(ItemDataTypes.ENTITY_BUCKET_DATA)

        // 已经是一个装有生物的生物桶了
        if (entityBucketData != null) {
            event.isCancelled = true
            return
        }

        // 检查是否可以捕捉该生物
        val entityTypeKey = clicked.type.key
        if (entityTypeKey !in entityBucket.allowedEntities ||
            !player.hasPermission("koish.item.behavior.entity_bucket.capture.${entityTypeKey.asString()}")
        ) {
            return
        }

        // 处理创造模式和多桶叠加的情况
        if (itemstack.amount > 1 || player.gameMode == GameMode.CREATIVE) {
            val newStack = itemstack.clone().asOne()
            asEntityBucket(newStack, clicked, player)
            if (player.gameMode != GameMode.CREATIVE) {
                itemstack.subtract(1)
            }
            player.inventory.addItem(newStack)
        } else {
            asEntityBucket(itemstack, clicked, player)
        }

        clicked.remove()
        event.isCancelled = true
    }

    private fun hasEntityBucketBehavior(itemstack: ItemStack): Boolean {
        return itemstack.hasProp(ItemPropertyTypes.ENTITY_BUCKET)
    }

    private fun hasEntityBucketData(itemstack: ItemStack): Boolean {
        return itemstack.hasData(ItemDataTypes.ENTITY_BUCKET_DATA)
    }

    private fun asEntityBucket(itemstack: ItemStack, clicked: Entity, player: Player) {
        when (clicked.type) {
            EntityType.CHICKEN ->
                asChickenBucket(itemstack, clicked, player)

            EntityType.COW ->
                asCowBucket(itemstack, clicked, player)

            EntityType.PIG ->
                asPigBucket(itemstack, clicked, player)

            EntityType.SHEEP ->
                asSheepBucket(itemstack, clicked, player)

            EntityType.VILLAGER, EntityType.ZOMBIE_VILLAGER, EntityType.WANDERING_TRADER ->
                asNpcBucket(itemstack, clicked, player)

            else ->
                TODO("unsupported entity type: ${clicked.type}")
        }
    }

    private fun asChickenBucket(itemstack: ItemStack, clicked: Entity, player: Player) {

    }

    private fun asCowBucket(itemstack: ItemStack, clicked: Entity, player: Player) {

    }

    private fun asPigBucket(itemstack: ItemStack, clicked: Entity, player: Player) {

    }

    private fun asSheepBucket(itemstack: ItemStack, clicked: Entity, player: Player) {

    }

    private fun asNpcBucket(itemstack: ItemStack, clicked: Entity, player: Player) {
        // 先让 clicked 处于静止状态
        clicked.velocity = Vector(0, 0, 0)
        clicked.fallDistance = 0.0f

        // 向 itemstack 写入实体数据
        val serializedEntity = Bukkit.getUnsafe().serializeEntity(clicked)
        itemstack.setData(ItemDataTypes.ENTITY_BUCKET_DATA, serializedEntity)

        // 确保 itemstack 不会叠加
        if (itemstack.getData(DataComponentTypes.MAX_STACK_SIZE) != 1) {
            itemstack.setData(DataComponentTypes.MAX_STACK_SIZE, 1)
        }

        // 设置 itemstack 外观
        when (clicked) {
            is Villager -> {
                itemstack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData().addString("villager/${clicked.villagerType.key.value()}"))
            }

            is ZombieVillager -> {
                itemstack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData().addString("villager/zombie"))
            }

            is WanderingTrader -> {
                itemstack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData().addString("villager/wandering_trader"))
            }
        }

        // TODO 播放交互音效
    }
}
