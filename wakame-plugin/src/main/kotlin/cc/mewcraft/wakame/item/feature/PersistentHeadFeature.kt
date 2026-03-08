package cc.mewcraft.wakame.item.feature

import cc.mewcraft.wakame.item.isKoish
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.registerEvents
import cc.mewcraft.wakame.util.runTaskLater
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.block.Skull
import org.bukkit.entity.Item
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockFromToEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import kotlin.random.Random


@Init(InitStage.POST_WORLD)
object PersistentHeadFeature : Listener {
    private val STORED_ITEM_KEY = NamespacedKey("koish", "stored_player_head_item")

    @InitFun
    fun init() {
        registerEvents()
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: BlockPlaceEvent) {
        val itemInHand = event.itemInHand.takeIf { it.isKoish } ?: return
        val blockPlaced = event.blockPlaced.takeIf { it.type == Material.PLAYER_HEAD } ?: return
        val skull = blockPlaced.getState(true) as? Skull ?: return
        skull.persistentDataContainer.set(STORED_ITEM_KEY, ItemStackDataType, itemInHand)
        if (skull.isSnapshot) skull.update()
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(event: BlockDropItemEvent) {
        val skull = event.blockState as? Skull ?: return
        val storedItem = skull.persistentDataContainer.get(STORED_ITEM_KEY, ItemStackDataType) ?: return
        val itemEntity = skull.world.spawn(skull.location, Item::class.java) { item ->
            item.itemStack = storedItem
        }
        event.items.clear()
        event.items.add(itemEntity)
    }

    /**
     * Prevents player from removing player-head NBT by water logging them.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerBucketEmpty(event: PlayerBucketEmptyEvent) {
        this.handleBlock(event.block, event, false)
    }

    /**
     * Prevents player from removing player-head NBT using running water.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onLiquidFlow(event: BlockFromToEvent) {
        this.handleBlock(event.getToBlock(), event, true)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBlockExplosion(event: BlockExplodeEvent) {
        this.handleExplosionEvent(event.blockList(), event.getYield())
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEntityExplosion(event: EntityExplodeEvent) {
        this.handleExplosionEvent(event.blockList(), event.getYield())
    }

    private fun handleExplosionEvent(blocksExploded: MutableList<Block>, explosionYield: Float) {
        val iter = blocksExploded.iterator()
        while (iter.hasNext()) {
            val block = iter.next()
            if (block.state is Skull && Random.nextFloat() <= explosionYield) {
                this.handleBlock(block, null, false)
                iter.remove()
            }
        }
    }

    private fun handleBlock(block: Block, event: Cancellable?, shouldCancelEvent: Boolean) {
        val skull = block.state as? Skull ?: return
        val storedStack = skull.persistentDataContainer.get(STORED_ITEM_KEY, ItemStackDataType) ?: return
        if (shouldCancelEvent) requireNotNull(event) { "Can't cancel a null event" }.isCancelled = true
        block.type = Material.AIR
        block.state.update(true, true)
        runTaskLater(1) { block.world.dropItemNaturally(block.location, storedStack) }
    }
}

private object ItemStackDataType : PersistentDataType<ByteArray, ItemStack> {
    override fun getPrimitiveType(): Class<ByteArray> {
        return ByteArray::class.java
    }

    override fun getComplexType(): Class<ItemStack> {
        return ItemStack::class.java
    }

    override fun toPrimitive(complex: ItemStack, context: PersistentDataAdapterContext): ByteArray {
        return complex.serializeAsBytes()
    }

    override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): ItemStack {
        return if (primitive.isEmpty()) ItemStack.empty() else ItemStack.deserializeBytes(primitive)
    }
}
