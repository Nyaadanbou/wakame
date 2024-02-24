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
 *
 * @property material 原版物品类型
 */
data class MaterialMeta(
    private val material: Material = Material.STONE,
) : SchemeMeta<Material> {
    override fun generate(context: SchemeGenerationContext): Material {
        return material
    }

    companion object : Keyed {
        override fun key(): Key = Key.key(NekoNamespaces.META, "material")
    }
}

internal class MaterialMetaSerializer : SchemeMetaSerializer<MaterialMeta> {
    override val emptyValue: MaterialMeta = MaterialMeta()

    override fun deserialize(type: Type, node: ConfigurationNode): MaterialMeta {
        val name = node.requireKt<String>()
        val material = Material.matchMaterial(name) ?: throw SerializationException("Can't parse material name $name")
        return MaterialMeta(material)
    }
}