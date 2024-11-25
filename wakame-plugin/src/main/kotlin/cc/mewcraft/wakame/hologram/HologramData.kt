package cc.mewcraft.wakame.hologram

import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.entity.Display
import org.bukkit.entity.Display.*
import org.bukkit.entity.TextDisplay
import org.bukkit.inventory.ItemStack
import org.joml.Vector3f

interface HologramData {
    val location: Location
    val type: Type

    enum class Type {
        TEXT,
        BLOCK,
        ITEM
    }
}

abstract class DisplayHologramData(
    final override val type: HologramData.Type,
    final override val location: Location,
) : HologramData {
    var billboard: Billboard = DEFAULT_BILLBOARD
    var scale: Vector3f = Vector3f(DEFAULT_SCALE)
    var translation: Vector3f = Vector3f(DEFAULT_TRANSLATION)
    var brightness: Display.Brightness? = null
    var shadowRadius: Float = DEFAULT_SHADOW_RADIUS
    var shadowStrength: Float = DEFAULT_SHADOW_STRENGTH
    var startInterpolation: Int = DEFAULT_START_INTERPOLATION
    var interpolationDuration: Int = DEFAULT_INTERPOLATION_DURATION

    companion object {
        val DEFAULT_BILLBOARD: Billboard = Billboard.CENTER
        val DEFAULT_SCALE: Vector3f = Vector3f(1f, 1f, 1f)
        val DEFAULT_TRANSLATION: Vector3f = Vector3f(0f, 0f, 0f)
        const val DEFAULT_SHADOW_RADIUS: Float = 0.0f
        const val DEFAULT_SHADOW_STRENGTH: Float = 1.0f
        const val DEFAULT_START_INTERPOLATION: Int = -1
        const val DEFAULT_INTERPOLATION_DURATION: Int = 1
    }
}

class TextHologramData(
    location: Location,
    val text: Component,
    val background: Color?,
    val hasTextShadow: Boolean,
    val textAlignment: TextDisplay.TextAlignment,
    val isSeeThrough: Boolean,
) : DisplayHologramData(HologramData.Type.TEXT, location) {
    // 此属性的数值无法跟 alpha-value 的正确效果相匹配
    // https://bugs.mojang.com/browse/MC-259823
    var opacity: Byte = DEFAULT_OPACITY

    companion object {
        const val DEFAULT_OPACITY: Byte = -1
    }
}

class ItemHologramData(
    location: Location,
) : DisplayHologramData(HologramData.Type.ITEM, location) {
    var itemStack: ItemStack = DEFAULT_ITEM
        set(item) {
            if (field != item) {
                field = item
            }
        }

    companion object {
        val DEFAULT_ITEM: ItemStack = ItemStack(Material.APPLE)
    }
}

class BlockHologramData(
    location: Location,
) : DisplayHologramData(HologramData.Type.BLOCK, location) {
    var block: Material = DEFAULT_BLOCK

    companion object {
        val DEFAULT_BLOCK: Material = Material.GRASS_BLOCK
    }
}