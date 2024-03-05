package cc.mewcraft.wakame.item.scheme.meta

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.util.requireKt
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import org.bukkit.Material
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

/**
 * 物品的原版物品类型。
 */
sealed interface MaterialMeta : SchemeItemMeta<Material> {
    override fun generate(context: SchemeGenerationContext): Material // never null

    companion object : Keyed {
        override fun key(): Key = Key.key(NekoNamespaces.ITEM_META, "material")
    }
}

private class NonNullMaterialMeta(
    private val material: Material,
) : MaterialMeta {
    override fun generate(context: SchemeGenerationContext): Material {
        return material
    }
}

private data object DefaultMaterialMeta : MaterialMeta {
    override fun generate(context: SchemeGenerationContext): Material {
        return Material.STONE
    }
}

internal class MaterialMetaSerializer : SchemeItemMetaSerializer<MaterialMeta> {
    override val defaultValue: MaterialMeta = DefaultMaterialMeta

    override fun deserialize(type: Type, node: ConfigurationNode): MaterialMeta {
        val name = node.requireKt<String>()
        val material = Material.matchMaterial(name) ?: throw SerializationException("Can't parse material name $name")
        return NonNullMaterialMeta(material)
    }
}