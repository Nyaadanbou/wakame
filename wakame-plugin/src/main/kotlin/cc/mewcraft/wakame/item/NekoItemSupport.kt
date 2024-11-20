package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.item.behavior.ItemBehaviorMap
import cc.mewcraft.wakame.item.template.ItemTemplateMap
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

/**
 * 一个标准的 [NekoItem].
 */
internal class SimpleNekoItem(
    override val id: Key,
    override val base: ItemBase,
    override val slotGroup: ItemSlotGroup,
    override val templates: ItemTemplateMap,
    override val behaviors: ItemBehaviorMap,
) : NekoItem {

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("id", id.asString()),
        ExaminableProperty.of("base", base),
        ExaminableProperty.of("slot_group", slotGroup),
        ExaminableProperty.of("templates", templates),
        ExaminableProperty.of("behaviors", behaviors),
    )

    override fun toString(): String = toSimpleString()
}

/**
 * 空的 [NekoItem], 某些特殊情况下使用.
 */
internal object EmptyNekoItem : NekoItem {
    override val id: Key = GenericKeys.EMPTY
    override val base: ItemBase = ItemBase.nop()
    override val slotGroup: ItemSlotGroup = ItemSlotGroup.empty()
    override val templates: ItemTemplateMap = ItemTemplateMap.empty()
    override val behaviors: ItemBehaviorMap = ItemBehaviorMap.empty()
}