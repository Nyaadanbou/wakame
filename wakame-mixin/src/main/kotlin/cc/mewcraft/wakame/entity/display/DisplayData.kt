package cc.mewcraft.wakame.entity.display

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Display.Billboard
import org.bukkit.entity.Display.Brightness
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.TextDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f
import net.minecraft.world.entity.Display as MojangDisplay

/**
 * 展示实体共通数据.
 * 默认值与 wiki 一致.
 */
sealed class CommonDisplayData {
    @JvmField
    var billboard: Billboard = Billboard.FIXED

    @JvmField
    var brightness: Brightness? = null

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
    var transformation: Transformation = DEFAULT_TRANSFORMATION

    @JvmField
    var viewRange: Float = 1.0f

    companion object {
        @JvmField
        // 单位变换, 即无变换
        val DEFAULT_TRANSFORMATION: Transformation = Transformation(
            Vector3f(0.0f, 0.0f, 0.0f),
            Quaternionf(0.0f, 0.0f, 0.0f, 1.0f),
            Vector3f(1.0f, 1.0f, 1.0f),
            Quaternionf(0.0f, 0.0f, 0.0f, 1.0f)
        )
    }
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
