package cc.mewcraft.wakame.entity.display

import cc.mewcraft.wakame.bridge.MojangStack
import cc.mewcraft.wakame.bridge.serverLevel
import cc.mewcraft.wakame.bridge.serverPlayer
import cc.mewcraft.wakame.shadow.network.syncher.ShadowSynchedEntityData
import io.papermc.paper.adventure.PaperAdventure
import me.lucko.shadow.bukkit.BukkitShadowFactory
import me.lucko.shadow.shadow
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.*
import net.minecraft.network.syncher.SynchedEntityData.DataValue
import net.minecraft.resources.Identifier
import net.minecraft.util.Brightness
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.PositionMoveRotation
import net.minecraft.world.item.ItemDisplayContext
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or
import com.mojang.math.Transformation as MojangTransformation
import net.minecraft.world.entity.Display as MojangDisplay
import org.bukkit.entity.TextDisplay as BukkitTextDisplay

abstract class CommonDisplay<T : MojangDisplay>(
    protected var location: Location,
) {
    protected var mojangDisplay: T? = null

    abstract val data: CommonDisplayData
    private val viewers: MutableSet<UUID> = HashSet()
    private var isLocationChanged: Boolean = false

    /**
     * 更新展示实体的坐标.
     */
    fun updateLocation(newLocation: Location) {
        location = newLocation
        isLocationChanged = true
    }

    /**
     * 创建对应的 [mojangDisplay].
     *
     * 仅创建, 不会更新各属性.
     * 由具体的展示实体子类实现.
     */
    abstract fun create()

    /**
     * 更新对应的 [mojangDisplay] 中的各属性.
     *
     * 即将 [data] 中的属性同步至 [mojangDisplay] 中.
     * 由具体的展示实体子类实现.
     */
    abstract fun update()

    /**
     * 向 [player] 显示展示实体.
     *
     * 本质上是向玩家发送添加实体的网络包(该包不会设置实体属性).
     * 会调用一次 [refresh].
     */
    fun show(player: Player): Boolean {
        // 对应的 mojangDisplay 不存在 - 直接返回
        val display = this.mojangDisplay ?: return false

        // 展示实体与玩家不处于同一世界 - 直接返回
        if (location.world.name != player.location.getWorld().name) {
            return false
        }

        // 向玩家发送添加实体的网络包
        player.serverPlayer.connection.send(
            ClientboundAddEntityPacket(display, 0, BlockPos.containing(location.x, location.y, location.z))
        )
        this.viewers.add(player.uniqueId)
        refresh(player)

        return true
    }

    /**
     * 向 [player] 刷新展示实体的数据.
     * 本质上是向玩家发送传送实体的网络包(若需要)和设置数据的网络包.
     */
    fun refresh(player: Player): Boolean {
        // 对应的 mojangDisplay 不存在 - 直接返回
        val display = this.mojangDisplay ?: return false

        // 玩家不在 viewers 中 - 直接返回
        if (!this.viewers.contains(player.uniqueId)) return false

        val values = ArrayList<DataValue<*>>()
        for (item in BukkitShadowFactory.global().shadow<ShadowSynchedEntityData>(display.entityData).itemsById) {
            values.add(item.value())
        }
        val setEntityDataPacket = ClientboundSetEntityDataPacket(display.id, values)
        if (this.isLocationChanged) {
            // 若坐标发生了变化, 则同时发送传送实体网络包和实体设置数据网络包
            // 使用 bundle packet 一次性发送
            val packets = ArrayList<Packet<ClientGamePacketListener>>()
            packets += ClientboundTeleportEntityPacket(display.id, PositionMoveRotation.of(display), setOf(), display.onGround)
            packets += setEntityDataPacket
            player.serverPlayer.connection.send(ClientboundBundlePacket(packets))
            this.isLocationChanged = false
        } else {
            // 若坐标未发生变化, 则只需要发送实体设置数据网络包
            player.serverPlayer.connection.send(setEntityDataPacket)
        }

        return true
    }

    /**
     * 向 [player] 隐藏展示实体.
     *
     * 本质上是向玩家发送移除实体的网络包.
     */
    fun hide(player: Player): Boolean {
        // 对应的 mojangDisplay 不存在 - 直接返回
        val display = this.mojangDisplay ?: return false

        player.serverPlayer.connection.send(ClientboundRemoveEntitiesPacket(display.id))
        this.viewers.remove(player.uniqueId)

        return true
    }

    protected fun updateCommon(data: CommonDisplayData, mojangDisplay: T) {
        // billboard
        mojangDisplay.billboardConstraints = data.billboard.toMojang()

        // brightness
        val brightness = data.brightness
        mojangDisplay.brightnessOverride = when (brightness) {
            is DefaultBrightness -> null
            is SpecifiedBrightness -> Brightness(brightness.blockLight, brightness.skyLight)
        }

        // glow color override
        mojangDisplay.glowColorOverride = data.glowColorOverride

        // height
        mojangDisplay.height = data.height

        // width
        mojangDisplay.width = data.width

        // shadow radius
        mojangDisplay.shadowRadius = data.shadowRadius

        // shadow strength
        mojangDisplay.shadowStrength = data.shadowStrength

        // start interpolation
        mojangDisplay.transformationInterpolationDelay = data.startInterpolation

        // interpolation duration
        mojangDisplay.transformationInterpolationDuration = data.interpolationDuration

        // teleport duration
        mojangDisplay.entityData.set(MojangDisplay.DATA_POS_ROT_INTERPOLATION_DURATION_ID, data.teleportDuration)

        // transformation
        mojangDisplay.setTransformation(
            MojangTransformation(
                data.transformation.translation,
                data.transformation.leftRotation,
                data.transformation.scale,
                data.transformation.rightRotation
            )
        )

        // view range
        mojangDisplay.viewRange = data.viewRange
    }
}

class TextDisplay(
    override val data: TextDisplayData,
    location: Location,
) : CommonDisplay<MojangDisplay.TextDisplay>(location) {

    override fun create() {
        // 若区块未加载, 什么也不做
        if (!location.isWorldLoaded) return

        // 创建文本展示实体
        val world = location.world.serverLevel
        mojangDisplay = MojangDisplay.TextDisplay(EntityType.TEXT_DISPLAY, world)
    }

    override fun update() {
        val data = this.data
        // 对应的 mojangDisplay 不存在, 没有东西可以更新, 直接返回
        val mojangDisplay = this.mojangDisplay ?: return

        // 更新展示实体共通属性
        updateCommon(data, mojangDisplay)

        // text alignment
        when (data.textAlignment) {
            BukkitTextDisplay.TextAlignment.LEFT -> {
                mojangDisplay.setFlag(MojangDisplay.TextDisplay.FLAG_ALIGN_LEFT, true)
                mojangDisplay.setFlag(MojangDisplay.TextDisplay.FLAG_ALIGN_RIGHT, false)
            }

            BukkitTextDisplay.TextAlignment.RIGHT -> {
                mojangDisplay.setFlag(MojangDisplay.TextDisplay.FLAG_ALIGN_LEFT, false)
                mojangDisplay.setFlag(MojangDisplay.TextDisplay.FLAG_ALIGN_RIGHT, true)
            }

            BukkitTextDisplay.TextAlignment.CENTER -> {
                mojangDisplay.setFlag(MojangDisplay.TextDisplay.FLAG_ALIGN_LEFT, false)
                mojangDisplay.setFlag(MojangDisplay.TextDisplay.FLAG_ALIGN_RIGHT, false)
            }
        }

        // background
        mojangDisplay.entityData.set(MojangDisplay.TextDisplay.DATA_BACKGROUND_COLOR_ID, data.background)

        // default background
        mojangDisplay.setFlag(MojangDisplay.TextDisplay.FLAG_USE_DEFAULT_BACKGROUND, data.defaultBackground)

        // line width
        mojangDisplay.entityData.set(MojangDisplay.TextDisplay.DATA_LINE_WIDTH_ID, data.lineWidth)

        // see through
        mojangDisplay.setFlag(MojangDisplay.TextDisplay.FLAG_SEE_THROUGH, data.isSeeThrough)

        // text shadow
        mojangDisplay.setFlag(MojangDisplay.TextDisplay.FLAG_SHADOW, data.hasTextShadow)

        // text
        mojangDisplay.text = PaperAdventure.asVanilla(data.text)

        // text opacity
        mojangDisplay.textOpacity = data.textOpacity
    }

    private fun MojangDisplay.TextDisplay.setFlag(flag: Byte, set: Boolean) {
        this.flags = if (set) {
            this.flags or flag
        } else {
            this.flags and flag.inv()
        }
    }
}

class ItemDisplay(
    override val data: ItemDisplayData,
    location: Location,
) : CommonDisplay<MojangDisplay.ItemDisplay>(location) {

    override fun create() {
        // 若区块未加载, 什么也不做
        if (!location.isWorldLoaded) return

        // 创建物品展示实体
        val world = location.world.serverLevel
        mojangDisplay = MojangDisplay.ItemDisplay(EntityType.ITEM_DISPLAY, world)
    }

    override fun update() {
        val data = this.data
        // 对应的 mojangDisplay 不存在, 没有东西可以更新, 直接返回
        val mojangDisplay = this.mojangDisplay ?: return

        // 更新展示实体共通属性
        updateCommon(data, mojangDisplay)

        // item
        mojangDisplay.itemStack = MojangStack.fromBukkitCopy(data.item)

        // item transform
        mojangDisplay.itemTransform = ItemDisplayContext.BY_ID.apply(data.itemDisplayTransform.ordinal)
    }
}

class BlockDisplay(
    override val data: BlockDisplayData,
    location: Location,
) : CommonDisplay<MojangDisplay.BlockDisplay>(location) {

    override fun create() {
        // 若区块未加载, 什么也不做
        if (!location.isWorldLoaded) return

        // 创建方块展示实体
        val world = location.world.serverLevel
        mojangDisplay = MojangDisplay.BlockDisplay(EntityType.BLOCK_DISPLAY, world)
    }

    override fun update() {
        val data = this.data
        // 对应的 mojangDisplay 不存在, 没有东西可以更新, 直接返回
        val mojangDisplay = this.mojangDisplay ?: return

        // 更新展示实体共通属性
        updateCommon(data, mojangDisplay)

        // block state
        val block = BuiltInRegistries.BLOCK.get(Identifier.bySeparator(data.material.key.toString(), ':')).get().value()
        mojangDisplay.blockState = block.defaultBlockState()

        // TODO 还有其他属性, 由于方块展示实体使用较少, 暂时搁置
    }
}