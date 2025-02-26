package item

import cc.mewcraft.wakame.item.ItemSlotGroup
import cc.mewcraft.wakame.item.NekoItem
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.behavior.ItemBehaviorMap
import cc.mewcraft.wakame.item.component.ItemComponentMap
import cc.mewcraft.wakame.item.template.ItemTemplateMap
import cc.mewcraft.wakame.util.MojangStack
import net.kyori.adventure.key.Key
import net.kyori.examination.ExaminableProperty
import net.kyori.examination.string.StringExaminer
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.stream.Stream

class MockNekoStack(
    override val prototype: NekoItem,
) : NekoStack {
    override val isEmpty: Boolean
        get() = false

    override val itemType: Material
        get() = throw NotImplementedError("Not implemented")

    override val mojangStack: MojangStack
        get() = throw NotImplementedError("Not implemented")

    override val bukkitStack: ItemStack
        get() = throw NotImplementedError("Not implemented")

    override val id: Key = prototype.id

    override var variant: Int = 0

    override val slotGroup: ItemSlotGroup = prototype.slotGroup

    override val components: ItemComponentMap = ItemComponentMap.builder().build()

    override val templates: ItemTemplateMap = prototype.templates

    override val behaviors: ItemBehaviorMap = prototype.behaviors

    override fun clone(): NekoStack = throw NotImplementedError("Not implemented")

    override fun erase() = Unit

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", id.asString()),
        ExaminableProperty.of("components", components),
        ExaminableProperty.of("behaviors", behaviors),
    )

    override fun toString(): String = StringExaminer.simpleEscaping().examine(this)
}