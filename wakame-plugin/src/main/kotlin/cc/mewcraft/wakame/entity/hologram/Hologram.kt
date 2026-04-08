package cc.mewcraft.wakame.entity.hologram

import cc.mewcraft.wakame.shadow.network.syncher.ShadowSynchedEntityData
import com.mojang.math.Transformation
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
import org.bukkit.Color
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.joml.Quaternionf
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or
import net.minecraft.world.entity.Display as MojangDisplay
import net.minecraft.world.item.ItemStack as MojangStack
import org.bukkit.entity.Display as BukkitDisplay


/**
 * 一个对于 [MojangDisplay] 的封装.
 */
class Hologram(
    private val data: HologramData,
) {
    companion object {
        private const val LINE_WIDTH = 1000
        private val TRANSPARENT = Color.fromARGB(0)
    }

    private val viewers: MutableSet<UUID> = HashSet()
    private var display: MojangDisplay? = null

    val displayEntity: BukkitDisplay?
        get() = display?.bukkitEntity as? BukkitDisplay

    fun <T : HologramData> data(): T {
        @Suppress("UNCHECKED_CAST")
        return data as T // check at runtime
    }

    fun create() {
        val location = data.location
        if (!location.isWorldLoaded)
            return

        val world = (location.world as CraftWorld).handle

        display = when (data.type) {
            HologramData.Type.TEXT -> MojangDisplay.TextDisplay(EntityType.TEXT_DISPLAY, world)
            HologramData.Type.ITEM -> MojangDisplay.ItemDisplay(EntityType.ITEM_DISPLAY, world)
            HologramData.Type.BLOCK -> MojangDisplay.BlockDisplay(EntityType.BLOCK_DISPLAY, world)
        }

        update()
    }

    fun update() {
        val data = this.data
        val display = this.display
        if (display == null) {
            return // doesn't exist, nothing to update
        }

        // location data
        val location = data.location
        if (location.world == null || !location.isWorldLoaded) {
            return
        } else {
            display.setPosRaw(location.x(), location.y(), location.z())
            display.yRot = location.yaw
            display.xRot = location.pitch
        }

        if (display is MojangDisplay.TextDisplay && data is TextHologramData) {
            // line width
            display.entityData.set(MojangDisplay.TextDisplay.DATA_LINE_WIDTH_ID, LINE_WIDTH)

            // background
            when (val background = data.background) {
                null -> {
                    display.entityData.set(MojangDisplay.TextDisplay.DATA_BACKGROUND_COLOR_ID, MojangDisplay.TextDisplay.INITIAL_BACKGROUND)
                }

                TRANSPARENT -> {
                    display.entityData.set(MojangDisplay.TextDisplay.DATA_BACKGROUND_COLOR_ID, 0)
                }

                else -> {
                    display.entityData.set(MojangDisplay.TextDisplay.DATA_BACKGROUND_COLOR_ID, background.asARGB())
                }
            }

            // text shadow
            if (data.hasTextShadow) {
                display.flags = (display.flags or MojangDisplay.TextDisplay.FLAG_SHADOW)
            } else {
                display.flags = (display.flags and MojangDisplay.TextDisplay.FLAG_SHADOW.inv())
            }

            // text alignment
            if (data.textAlignment == TextDisplay.TextAlignment.LEFT) {
                display.flags = (display.flags or MojangDisplay.TextDisplay.FLAG_ALIGN_LEFT)
            } else {
                display.flags = (display.flags and MojangDisplay.TextDisplay.FLAG_ALIGN_LEFT.inv())
            }

            // see through
            if (data.isSeeThrough) {
                display.flags = (display.flags or MojangDisplay.TextDisplay.FLAG_SEE_THROUGH)
            } else {
                display.flags = (display.flags and MojangDisplay.TextDisplay.FLAG_SEE_THROUGH.inv())
            }

            if (data.textAlignment == TextDisplay.TextAlignment.RIGHT) {
                display.flags = (display.flags or MojangDisplay.TextDisplay.FLAG_ALIGN_RIGHT)
            } else {
                display.flags = (display.flags and MojangDisplay.TextDisplay.FLAG_ALIGN_RIGHT.inv())
            }

            // text opacity
            display.textOpacity = data.opacity
        } else if (display is MojangDisplay.ItemDisplay && data is ItemHologramData) {
            // item
            display.itemStack = MojangStack.fromBukkitCopy(data.item)
        } else if (display is MojangDisplay.BlockDisplay && data is BlockHologramData) {
            val block = BuiltInRegistries.BLOCK.get(Identifier.bySeparator("minecraft:" + data.block.name.lowercase(), ':')).get().value()
            display.blockState = block.defaultBlockState()
        }

        if (data is DisplayHologramData) {
            // billboard data
            display.billboardConstraints = when (data.billboard) {
                BukkitDisplay.Billboard.FIXED -> MojangDisplay.BillboardConstraints.FIXED
                BukkitDisplay.Billboard.VERTICAL -> MojangDisplay.BillboardConstraints.VERTICAL
                BukkitDisplay.Billboard.HORIZONTAL -> MojangDisplay.BillboardConstraints.HORIZONTAL
                BukkitDisplay.Billboard.CENTER -> MojangDisplay.BillboardConstraints.CENTER
            }

            // brightness
            val brightness = data.brightness
            if (brightness != null) {
                display.brightnessOverride = Brightness(brightness.blockLight, brightness.skyLight)
            }

            // entity scale AND MORE!
            display.setTransformation(
                Transformation(
                    data.translation,
                    Quaternionf(),
                    data.scale,
                    Quaternionf()
                )
            )

            // entity shadow
            display.shadowRadius = data.shadowRadius
            display.shadowStrength = data.shadowStrength

            // entity interpolation
            display.transformationInterpolationDelay = data.startInterpolation
            display.transformationInterpolationDuration = data.interpolationDuration
        }
    }

    fun show(player: Player): Boolean {
        if (this.display == null) {
            create() // try to create it if it doesn't exist every time
        }

        val display = this.display
        if (display == null) {
            return false // could not be created, nothing to show
        }

        val location = data.location
        if (location.world.name != player.location.getWorld().name) {
            return false
        }

        val serverPlayer = (player as CraftPlayer).handle

        serverPlayer.connection.send(ClientboundAddEntityPacket(display, 0, BlockPos.containing(location.x, location.y, location.z)))
        this.viewers.add(player.uniqueId)
        refresh(player)

        return true
    }

    fun hide(player: Player): Boolean {
        val display = this.display
        if (display == null) {
            return false // doesn't exist, nothing to hide
        }

        (player as CraftPlayer).handle.connection.send(ClientboundRemoveEntitiesPacket(display.id))

        this.viewers.remove(player.uniqueId)
        return true
    }


    fun refresh(player: Player) {
        val data = this.data
        val display = this.display
        if (display == null) {
            return // doesn't exist, nothing to refresh
        }

        if (!this.viewers.contains(player.uniqueId)) {
            return
        }

        if (player !is CraftPlayer) {
            return
        }

        // we use bundle packet to send it all at once
        val packets = ArrayList<Packet<ClientGamePacketListener>>()
        packets += ClientboundTeleportEntityPacket(display.id, PositionMoveRotation.of(display), setOf(), display.onGround)

        if (display is MojangDisplay.TextDisplay && data is TextHologramData) {
            display.text = PaperAdventure.asVanilla(data.text)
        }

        val values = ArrayList<DataValue<*>>()
        for (item in BukkitShadowFactory.global().shadow<ShadowSynchedEntityData>(display.entityData).itemsById) {
            values.add(item.value())
        }

        packets += ClientboundSetEntityDataPacket(display.id, values)

        player.handle.connection.send(ClientboundBundlePacket(packets))
    }
}