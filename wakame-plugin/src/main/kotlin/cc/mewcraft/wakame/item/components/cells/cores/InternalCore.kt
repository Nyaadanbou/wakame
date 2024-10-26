package cc.mewcraft.wakame.item.components.cells.cores

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.components.cells.*
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

val Cell.virtualCore: VirtualCore?
    get() = getCore() as? VirtualCore

val Cell.emptyCore: EmptyCore?
    get() = getCore() as? EmptyCore

/**
 * [VirtualCore] 的标准实现.
 */
internal data object SimpleVirtualCore : VirtualCore {
    private val config = ItemComponentConfig.provide(ItemConstants.CELLS).root.node("virtual_core")

    override val id: Key = GenericKeys.NOOP
    override val displayName: Component by config.entry<Component>("display_name")
    override val description: List<Component> by config.entry<List<Component>>("description")

    override fun similarTo(other: Core): Boolean {
        return other === this
    }

    override fun serializeAsTag(): Nothing {
        error("VirtualCore does not support serialization")
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(ExaminableProperty.of("id", id))
    }

    override fun toString(): String {
        return toSimpleString()
    }
}

/**
 * [EmptyCore] 的标准实现.
 */
internal data object SimpleEmptyCore : EmptyCore {
    private val config = ItemComponentConfig.provide(ItemConstants.CELLS).root.node("empty_core")

    override val id: Key = GenericKeys.EMPTY
    override val displayName: Component by config.entry<Component>("display_name")
    override val description: List<Component> by config.entry<List<Component>>("description")

    override fun similarTo(other: Core): Boolean {
        return other === this
    }

    override fun serializeAsTag(): CompoundTag {
        return CompoundTag.create()
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(ExaminableProperty.of("id", id))
    }

    override fun toString(): String {
        return toSimpleString()
    }
}