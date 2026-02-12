package cc.mewcraft.wakame.core

import cc.mewcraft.wakame.item.ItemRef
import cc.mewcraft.wakame.util.KoishKey
import cc.mewcraft.wakame.util.KoishKeys
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ItemRefMock(
    override val id: KoishKey,
) : ItemRef {
    constructor(uid: String) : this(
        KoishKeys.of(uid)
    )

    constructor(namespace: String, path: String) : this(
        KoishKeys.of(namespace, path)
    )

    override val name: Component
        get() = throw UnsupportedOperationException()

    override fun matches(id: KoishKey): Boolean {
        return this.id == id
    }

    override fun matches(ref: ItemRef): Boolean {
        return this.id == ref.id
    }

    override fun matches(stack: ItemStack): Boolean {
        throw UnsupportedOperationException()
    }

    override fun createItemStack(amount: Int, player: Player?): ItemStack {
        throw UnsupportedOperationException()
    }

    override fun toString(): String {
        return "ItemRefMock(id=$id)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ItemRefMock) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

}