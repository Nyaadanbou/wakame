package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.Arrow
import cc.mewcraft.wakame.item.components.Attributable
import cc.mewcraft.wakame.item.components.Damageable
import cc.mewcraft.wakame.item.components.FireResistant
import cc.mewcraft.wakame.item.components.ItemCells
import cc.mewcraft.wakame.item.components.ItemLevel
import cc.mewcraft.wakame.item.components.ItemName
import cc.mewcraft.wakame.item.components.Unbreakable
import cc.mewcraft.wakame.item.components.cell.Cell
import cc.mewcraft.wakame.item.components.cell.CoreType
import cc.mewcraft.wakame.item.components.cell.CoreTypes
import cc.mewcraft.wakame.item.components.cell.cores.skill.CoreSkill
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.skill.trigger.TriggerVariant
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack

fun template(item: NekoItem) {

}

fun bukkit(stack: ItemStack) {
    stack.isNeko
}

fun stack(stack: NekoStack) {
    val damageableTemplate: Damageable.Template = stack.templates.get(ItemTemplateTypes.DAMAGEABLE) ?: return
    val disappearWhenBroken: Boolean = damageableTemplate.disappearWhenBroken

    val itemLevel: ItemLevel = stack.components.get(ItemComponentTypes.LEVEL) ?: return
    val level: Short = itemLevel.level

    val itemCells: ItemCells = stack.components.get(ItemComponentTypes.CELLS) ?: return
    val newItemCells: ItemCells = itemCells.modify("base_attack") { cell: Cell? ->
        cell ?: return@modify null
        val coreType: CoreType<*> = cell.core.type
        val coreSkill: CoreSkill = cell.getTypedCore(CoreTypes.SKILL) ?: return@modify null
        val newCoreSkill = coreSkill.copy(variant = TriggerVariant.of(3))
        cell.setCore(newCoreSkill)
    }
    stack.components.set(ItemComponentTypes.CELLS, newItemCells)

    // 射箭...
    // 获取 arrow
    val arrow: Arrow = stack.components.get(ItemComponentTypes.ARROW) ?: return
    if (arrow.pierceLevel == 1.toByte()) {
        stack.components.set(ItemComponentTypes.ARROW, Arrow.of(1))
    }

    // 设置免疫火焰 (对应原版组件: `minecraft:fire_resistant`)
    val fireResistant: FireResistant? = stack.components.get(ItemComponentTypes.FIRE_RESISTANT)
    stack.components.set(ItemComponentTypes.FIRE_RESISTANT, FireResistant)
    stack.components.unset(ItemComponentTypes.FIRE_RESISTANT)

    // 设置 cells (所有的词条栏)
    val cells: ItemCells? = stack.components.get(ItemComponentTypes.CELLS)
    val newCells: ItemCells? = cells?.remove("base_attack")
    newCells?.let { stack.components.set(ItemComponentTypes.CELLS, it) }
    // 移除 cells (所有的词条栏)
    stack.components.unset(ItemComponentTypes.CELLS)

    // 生成组件对应的提示框文本
    val attributable: Attributable? = stack.components.get(ItemComponentTypes.ATTRIBUTABLE)
    // val attributableLoreLine: LoreLine? = nekoStack.components.render(ItemComponentTypes.ATTRIBUTABLE)
    if (attributable != null) {
        val loreLine: LoreLine = attributable.provideDisplayLore()
    }

    // 获取 item_name
    val itemName: ItemName? = stack.components.get(ItemComponentTypes.ITEM_NAME)
    val unbreakable: Unbreakable? = stack.components.get(ItemComponentTypes.UNBREAKABLE)
    val hasFood: Boolean = stack.components.has(ItemComponentTypes.FOOD)
    // 设置 item_name
    stack.components.set(ItemComponentTypes.ITEM_NAME, ItemName.Value(Component.text("You can't change this name with anvils!")))
}