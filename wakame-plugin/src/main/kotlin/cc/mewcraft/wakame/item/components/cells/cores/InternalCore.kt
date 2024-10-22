package cc.mewcraft.wakame.item.components.cells.cores

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.item.components.cells.*
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

val Cell.virtualCore: VirtualCore?
    get() = getCore() as? VirtualCore

/**
 * [VirtualCore] 的标准实现.
 */
internal data object SimpleVirtualCore : VirtualCore {
    override val id: Key = GenericKeys.NOOP
    override val displayName: Component
        get() = error("Cannot generate display name for virtual core")
    override val description: List<Component>
        get() = error("Cannot generate tooltip for virtual core")

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
    override val id: Key = GenericKeys.EMPTY
    override val displayName: Component
        get() = error("Cannot generate display name for empty core")
    override val description: List<Component>
        get() = error("Cannot generate tooltip for empty core")

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