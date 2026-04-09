package cc.mewcraft.wakame.entity.display

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.lazyconfig.configurate.require
import cc.mewcraft.wakame.util.math.Transformation
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Display.Billboard
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.TextDisplay
import org.bukkit.inventory.ItemStack
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.lang.reflect.Type
import net.minecraft.world.entity.Display as MojangDisplay

/**
 * 展示实体共通数据.
 *
 * 默认值与 wiki 一致.
 */
sealed class CommonDisplayData {
    @JvmField
    var billboard: Billboard = Billboard.FIXED

    @JvmField
    var brightness: Brightness = DefaultBrightness

    @JvmField
    var glowColorOverride: Int = -1

    @JvmField
    var height: Float = 0.0f

    @JvmField
    var width: Float = 0.0f

    @JvmField
    var shadowRadius: Float = 0.0f

    @JvmField
    var shadowStrength: Float = 1.0f

    @JvmField
    var startInterpolation: Int = -1

    @JvmField
    var interpolationDuration: Int = 0

    @JvmField
    var teleportDuration: Int = 0

    @JvmField
    var transformation: Transformation = Transformation.identity()

    @JvmField
    var viewRange: Float = 1.0f
}

/**
 * 文本展示实体数据.
 */
class TextDisplayData : CommonDisplayData() {
    @JvmField
    var textAlignment: TextDisplay.TextAlignment = TextDisplay.TextAlignment.CENTER

    @JvmField
    var background: Int = 0x40000000

    @JvmField
    var defaultBackground: Boolean = false

    @JvmField
    var lineWidth: Int = 200

    @JvmField
    var isSeeThrough: Boolean = false

    @JvmField
    var hasTextShadow: Boolean = false

    @JvmField
    var text: Component = DEFAULT_TEXT

    @JvmField
    var textOpacity: Byte = -1

    companion object {
        val DEFAULT_TEXT: Component = Component.text("请输入文本")
    }
}

/**
 * 物品展示实体数据.
 */
class ItemDisplayData : CommonDisplayData() {
    @JvmField
    var item: ItemStack = ItemStack(Material.BARRIER)

    @JvmField
    var itemDisplayTransform: ItemDisplay.ItemDisplayTransform = ItemDisplay.ItemDisplayTransform.NONE
}

/**
 * 方块展示实体数据.
 */
class BlockDisplayData : CommonDisplayData() {
    @JvmField
    // TODO 应为 BlockState, 由于方块状态实现复杂, 且方块展示实体使用较少, 暂时搁置
    var material: Material = Material.BARRIER
}

fun Billboard.toMojang(): MojangDisplay.BillboardConstraints {
    return when (this) {
        Billboard.FIXED -> MojangDisplay.BillboardConstraints.FIXED
        Billboard.VERTICAL -> MojangDisplay.BillboardConstraints.VERTICAL
        Billboard.HORIZONTAL -> MojangDisplay.BillboardConstraints.HORIZONTAL
        Billboard.CENTER -> MojangDisplay.BillboardConstraints.CENTER
    }
}

/**
 * 对展示实体 brightness 属性的封装.
 *
 * 不直接使用 [org.bukkit.entity.Display.Brightness] 的原因:
 * Bukkit 使用 null 代表默认值, 即使用当前展示实体所在位置的亮度. 而 null 可能导致序列化问题.
 */
sealed interface Brightness

/**
 * 对应配置文件:
 * ```yaml
 * brightness: default
 * ```
 */
object DefaultBrightness : Brightness {
    const val PLACEHOLDER: String = "default"
}

/**
 * 对应配置文件:
 * ```yaml
 * brightness:
 *   block_light: 7
 *   sky_light: 9
 * ```
 */
@ConfigSerializable
data class SpecifiedBrightness(
    val blockLight: Int,
    val skyLight: Int,
) : Brightness

object BrightnessSerializer : SimpleSerializer<Brightness> {
    override fun deserialize(type: Type, node: ConfigurationNode): Brightness {
        val str = node.rawScalar().toString()
        if (str == DefaultBrightness.PLACEHOLDER) {
            return DefaultBrightness
        }

        val blockLight = node.node("block_light").require<Int>().also {
            require(it in 0..15) { "The block light must be between 0 and 15" }
        }
        val skyLight = node.node("block_light").require<Int>().also {
            require(it in 0..15) { "The sky light must be between 0 and 15" }
        }
        return SpecifiedBrightness(blockLight, skyLight)
    }
}