package item

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.NekoItem
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.behavior.ItemBehaviorMap
import cc.mewcraft.wakame.item.component.ItemComponentMap
import cc.mewcraft.wakame.item.template.ItemTemplateMap
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.ExaminableProperty
import org.bukkit.inventory.ItemStack
import java.util.stream.Stream

class MockNekoStack(
    override val prototype: NekoItem,
) : NekoStack {
    override val nbt: CompoundTag
        get() = throw NotImplementedError("Not implemented")
    override val handle: ItemStack
        get() = throw NotImplementedError("Not implemented")
    override val itemStack: ItemStack
        get() = throw NotImplementedError("Not implemented")
    override val namespace: String = prototype.key.namespace()
    override val path: String = prototype.key.value()
    override val key: Key = prototype.key
    override var variant: Int = 0
    override val slot: ItemSlot = prototype.slot
    override val components: ItemComponentMap = ItemComponentMap.builder().build()
    override val templates: ItemTemplateMap = prototype.templates
    override val behaviors: ItemBehaviorMap = prototype.behaviors
    override fun clone(): NekoStack = throw NotImplementedError("Not implemented")
    override fun erase() = Unit

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key.asString()),
        ExaminableProperty.of("components", components),
        ExaminableProperty.of("behaviors", behaviors),
    )

    override fun toString(): String = toSimpleString()
}