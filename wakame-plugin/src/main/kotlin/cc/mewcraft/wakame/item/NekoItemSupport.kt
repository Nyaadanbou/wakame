package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.item.behavior.ItemBehaviorMap
import cc.mewcraft.wakame.item.template.ItemTemplateMap
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.util.toSimpleString
import cc.mewcraft.wakame.util.unsafeNbt
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.examination.ExaminableProperty
import org.bukkit.inventory.ItemStack
import org.koin.core.component.get
import java.util.stream.Stream

val ItemStack.nekoItem: NekoItem?
    get() = this.unsafeNbt?.let(NekoStackImplementations::getPrototype)

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

    // 本实现将尽最大努力获取该萌芽物品类型的名字
    override val name: Component
        get() {
            val archetype = templates.get(ItemTemplateTypes.ITEM_NAME)
            if (archetype == null) {
                // 如果模板里没有指定名字, 则使用 base 的 type
                return Component.translatable(base.type)
            }
            val miniMessage = Injector.get<MiniMessage>()
            return miniMessage.deserialize(archetype.plainName)
        }

    override val plainName: String
        get() {
            val archetype = templates.get(ItemTemplateTypes.ITEM_NAME)
            if (archetype == null) {
                // 如果模板里没有指定名字, 则使用 base 的 type
                return base.type.name
            }
            return archetype.plainName
        }

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
    override val id: Key
        get() = GenericKeys.EMPTY
    override val base: ItemBase
        get() = ItemBase.nop()
    override val name: Component
        get() = Component.empty()
    override val plainName: String
        get() = ""
    override val slotGroup: ItemSlotGroup
        get() = ItemSlotGroup.empty()
    override val templates: ItemTemplateMap
        get() = ItemTemplateMap.empty()
    override val behaviors: ItemBehaviorMap
        get() = ItemBehaviorMap.empty()
}