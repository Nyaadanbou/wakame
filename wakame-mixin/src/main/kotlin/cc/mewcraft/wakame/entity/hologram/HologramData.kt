package cc.mewcraft.wakame.entity.hologram

import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Display.Billboard
import org.bukkit.entity.Display.Brightness
import org.bukkit.entity.TextDisplay
import org.bukkit.inventory.ItemStack
import org.joml.Vector3f

interface HologramData {
    val location: Location
    val type: Type

    enum class Type {
        TEXT,
        ITEM,
        BLOCK
    }
}

sealed class DisplayHologramData(
    override val type: HologramData.Type,
    override val location: Location,
) : HologramData {
    @JvmField
    var billboard: Billboard = DEFAULT_BILLBOARD
    @JvmField
    var scale: Vector3f = Vector3f(DEFAULT_SCALE)
    @JvmField
    var translation: Vector3f = Vector3f(DEFAULT_TRANSLATION)
    @JvmField
    var brightness: Brightness? = null
    @JvmField
    var shadowRadius: Float = DEFAULT_SHADOW_RADIUS
    @JvmField
    var shadowStrength: Float = DEFAULT_SHADOW_STRENGTH
    @JvmField
    var startInterpolation: Int = DEFAULT_START_INTERPOLATION
    @JvmField
    var interpolationDuration: Int = DEFAULT_INTERPOLATION_DURATION

    companion object {
        @JvmField
        val DEFAULT_BILLBOARD: Billboard = Billboard.CENTER
        @JvmField
        val DEFAULT_SCALE: Vector3f = Vector3f(1f, 1f, 1f)
        @JvmField
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
    @JvmField
    var opacity: Byte = DEFAULT_OPACITY

    companion object {
        const val DEFAULT_OPACITY: Byte = -1
    }
}

class ItemHologramData(
    location: Location,
) : DisplayHologramData(HologramData.Type.ITEM, location) {
    @JvmField
    var item: ItemStack = DEFAULT_ITEM

    companion object {
        @JvmField
        val DEFAULT_ITEM: ItemStack = ItemStack(Material.APPLE)
    }
}

class BlockHologramData(
    location: Location,
) : DisplayHologramData(HologramData.Type.BLOCK, location) {
    @JvmField
    var block: Material = DEFAULT_BLOCK

    companion object {
        @JvmField
        val DEFAULT_BLOCK: Material = Material.GRASS_BLOCK
    }
}