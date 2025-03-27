package cc.mewcraft.wakame.item.extension

import cc.mewcraft.wakame.item.wrap
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.item.toNMS
import io.papermc.paper.adventure.PaperAdventure
import net.minecraft.core.component.DataComponents
import org.bukkit.inventory.ItemStack
import kotlin.jvm.optionals.getOrNull

// 物品冷却相关
// 更方便的操控物品的冷却

/**
 * 获取物品的冷却组.
 * 冷却组id按顺序取决于:
 * **冷却组件冷却组id** -> **neko物品类型id** -> **原版物品类型id**
 * 原版套皮物品将返回 **原版物品类型id**.
 */
fun ItemStack.getCooldownGroup(): Identifier {
    val mojangStack = toNMS()

    // 尝试返回冷却组件上的冷却组id
    val useCooldown = mojangStack.get(DataComponents.USE_COOLDOWN)
    val resourceLocation = useCooldown?.cooldownGroup()?.getOrNull()
    if (resourceLocation != null) {
        return PaperAdventure.asAdventure(resourceLocation)
    }

    // 尝试返回neko物品类型id
    // 原版套皮物品将返回原版类型id
    val nekoStack = wrap()
    if (nekoStack != null) {
        return nekoStack.id
    }

    // 最后返回物品原版类型id
    return this.type.key
}

