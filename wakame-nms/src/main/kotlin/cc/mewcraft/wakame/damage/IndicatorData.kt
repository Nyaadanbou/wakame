package cc.mewcraft.wakame.damage

import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Display
import org.bukkit.entity.Display.Billboard
import org.bukkit.entity.TextDisplay
import org.bukkit.inventory.ItemStack
import org.joml.Vector3f

interface IndicatorData {
    val name: String
    val location: Location
    val type: Type

    enum class Type {
        TEXT,
        BLOCK,
        ITEM
    }
}

abstract class DisplayIndicatorData(
    override val name: String,
    override val type: IndicatorData.Type,
    override val location: Location,
) : IndicatorData {
    var billboard: Billboard = DEFAULT_BILLBOARD
    var scale: Vector3f = Vector3f(DEFAULT_SCALE)
    var translation: Vector3f = Vector3f(DEFAULT_TRANSLATION)
    var brightness: Display.Brightness? = null
    var shadowRadius: Float = DEFAULT_SHADOW_RADIUS
    var shadowStrength: Float = DEFAULT_SHADOW_STRENGTH

    companion object {
        val DEFAULT_BILLBOARD: Billboard = Billboard.CENTER
        val DEFAULT_SCALE: Vector3f = Vector3f(1f, 1f, 1f)
        val DEFAULT_TRANSLATION: Vector3f = Vector3f(0f, 0f, 0f)
        const val DEFAULT_SHADOW_RADIUS: Float = 0.0f
        const val DEFAULT_SHADOW_STRENGTH: Float = 1.0f
    }
}

data class TextIndicatorData(
    override val name: String,
    override val location: Location,
    val text: Component,
    val background: Color?,
    val hasTextShadow: Boolean,
    val textAlignment: TextDisplay.TextAlignment,
    val isSeeThrough: Boolean,
) : DisplayIndicatorData(name, IndicatorData.Type.TEXT, location)

data class ItemIndicatorData(
    override val name: String,
    override val location: Location,
) : DisplayIndicatorData(name, IndicatorData.Type.ITEM, location) {
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

data class BlockIndicatorData(
    override val name: String,
    override val location: Location,
) : DisplayIndicatorData(name, IndicatorData.Type.BLOCK, location) {
    var block: Material = DEFAULT_BLOCK

    companion object {
        val DEFAULT_BLOCK: Material = Material.GRASS_BLOCK
    }
}