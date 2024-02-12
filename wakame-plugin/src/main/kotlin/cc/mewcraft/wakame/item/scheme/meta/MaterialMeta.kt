package cc.mewcraft.wakame.item.scheme.meta

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.SchemeSerializer
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.util.typedRequire
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
class MaterialMeta(
    private val material: Material,
) : SchemeMeta<Material> {
    override fun generate(context: SchemeGenerationContext): Material {
        return material
    }

    companion object : Keyed {
        override fun key(): Key = Key.key(NekoNamespaces.META, "material")
    }
}

internal class MaterialMetaSerializer : SchemeSerializer<MaterialMeta> {
    override fun deserialize(type: Type, node: ConfigurationNode): MaterialMeta {
        val name = node.typedRequire<String>()
        val material = Material.matchMaterial(name) ?: throw SerializationException("Can't parse material name $name")
        return MaterialMeta(material)
    }
}