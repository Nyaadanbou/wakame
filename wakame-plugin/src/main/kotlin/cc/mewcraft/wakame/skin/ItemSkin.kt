package cc.mewcraft.wakame.skin

import cc.mewcraft.wakame.BiIdentified
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.pack.Model
import cc.mewcraft.wakame.util.validatePathStringOrNull
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.StyleBuilderApplicable
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material
import org.koin.core.component.KoinComponent
import java.io.File

data class ItemSkin @InternalApi internal constructor(
    override val key: String,
    override val binary: Short,
    override val displayName: Component,
    override val styles: Array<StyleBuilderApplicable>,
) : Skin, Model, KoinComponent, BiIdentified<String, Short> {
    override val modelKey: Key = Key.key(key)
    override val modelFile: File?
        get() = validatePathStringOrNull("models/skin/${modelKey.namespace()}/${modelKey.value()}.json")
    override val overriddenMaterials: Set<Material> = setOf(Material.PLAYER_HEAD) // TODO: Item material

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is ItemSkin) return other.key == key
        return false
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }

    override fun toString(): String {
        return PlainTextComponentSerializer.plainText().serialize(displayName)
    }
}