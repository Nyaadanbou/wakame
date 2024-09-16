package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.shadow.network.ShadowSynchedEntityData
import com.mojang.math.Transformation
import io.papermc.paper.adventure.PaperAdventure
import me.lucko.shadow.bukkit.BukkitShadowFactory
import me.lucko.shadow.shadow
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket
import net.minecraft.network.syncher.SynchedEntityData.DataValue
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Brightness
import net.minecraft.world.entity.EntityType
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
 * 一个对于 [MojangDisplay] 的封装, 用于指示伤害.
 */
class DamageIndicator(
    private var data: IndicatorData,
) {
    companion object {
        private const val LINE_WIDTH = 1000
        private val TRANSPARENT: Color = Color.fromARGB(0)
    }

    private val viewers: MutableSet<UUID> = HashSet()
    private var display: MojangDisplay? = null

    val displayEntity: BukkitDisplay?
        get() = display?.let { it.bukkitEntity as BukkitDisplay }

    fun create() {
        val location = data.location
        if (!location.isWorldLoaded)
            return

        val world = (location.world as CraftWorld).handle

        display = when (data.type) {
            IndicatorData.Type.TEXT -> MojangDisplay.TextDisplay(EntityType.TEXT_DISPLAY, world)
            IndicatorData.Type.BLOCK -> MojangDisplay.BlockDisplay(EntityType.BLOCK_DISPLAY, world)
            IndicatorData.Type.ITEM -> MojangDisplay.ItemDisplay(EntityType.ITEM_DISPLAY, world)
        }

        update()
    }

    fun update() {
        val data = this.data
        val display = this.display
            ?: return  // doesn't exist, nothing to update

        // location data
        val location = data.location
        if (location.world == null || !location.isWorldLoaded) {
            return
        } else {
            display.setPosRaw(location.x(), location.y(), location.z())
            display.yRot = location.yaw
            display.xRot = location.pitch
        }

        if (display is MojangDisplay.TextDisplay && data is TextIndicatorData) {
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
        } else if (display is MojangDisplay.ItemDisplay && data is ItemIndicatorData) {
            // item
            display.itemStack = MojangStack.fromBukkitCopy(data.itemStack)
        } else if (display is MojangDisplay.BlockDisplay && data is BlockIndicatorData) {
            val block = BuiltInRegistries.BLOCK.get(ResourceLocation.bySeparator("minecraft:" + data.block.name.lowercase(), ':'))
            display.blockState = block.defaultBlockState()
        }

        if (data is DisplayIndicatorData) {
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
            ?: return false // could not be created, nothing to show

        if (data.location.world.name != player.location.getWorld().name) {
            return false
        }

        val serverPlayer = (player as CraftPlayer).handle

        serverPlayer.connection.send(ClientboundAddEntityPacket(display, 0, BlockPos.ZERO))
        this.viewers.add(player.getUniqueId())
        refresh(player)

        return true
    }

    fun hide(player: Player): Boolean {
        val display = this.display
            ?: return false // doesn't exist, nothing to hide

        (player as CraftPlayer).handle.connection.send(ClientboundRemoveEntitiesPacket(display.id))

        this.viewers.remove(player.uniqueId)
        return true
    }


    fun refresh(player: Player) {
        val data = this.data
        val display = this.display
            ?: return  // doesn't exist, nothing to refresh

        if (!viewers.contains(player.uniqueId)) {
            return
        }

        if (player !is CraftPlayer) {
            return
        }

        player.handle.connection.send(ClientboundTeleportEntityPacket(display))

        if (display is MojangDisplay.TextDisplay && data is TextIndicatorData) {
            display.text = PaperAdventure.asVanilla(data.text)
        }

        val values = ArrayList<DataValue<*>>()

        for (item in BukkitShadowFactory.global().shadow<ShadowSynchedEntityData>(display.entityData).itemsById) {
            values.add(item.value())
        }

        player.handle.connection.send(ClientboundSetEntityDataPacket(display.id, values))
    }

    fun setEntityData(data: IndicatorData) {
        this.data = data
        update()
    }
}