package cc.mewcraft.wakame.item.components.cells.cores

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.item.components.cells.*
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

val Cell.virtualCore: VirtualCore?
    get() = getCoreAs(CoreType.VIRTUAL)

/**
 * [VirtualCore] 的标准实现.
 */
internal data object SimpleVirtualCore : VirtualCore, CoreKind<VirtualCore> {
    override val id: Key = GenericKeys.NOOP
    override val kind: CoreKind<VirtualCore> = this

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
internal data object SimpleEmptyCore : EmptyCore, CoreKind<EmptyCore> {
    override val id: Key = GenericKeys.EMPTY
    override val kind: CoreKind<EmptyCore> = this

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