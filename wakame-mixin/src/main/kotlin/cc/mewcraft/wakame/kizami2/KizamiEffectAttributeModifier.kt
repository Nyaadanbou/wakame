package cc.mewcraft.wakame.kizami2

import cc.mewcraft.wakame.entity.attribute.Attribute
import cc.mewcraft.wakame.entity.attribute.AttributeModifier
import cc.mewcraft.wakame.entity.attribute.bundle.ConstantAttributeBundle
import cc.mewcraft.wakame.entity.player.attributeContainer
import cc.mewcraft.wakame.serialization.configurate.RepresentationHints
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.require
import org.bukkit.entity.Player
import org.spongepowered.configurate.serialize.SerializationException

/**
 * 铭刻效果：属性修饰器.
 */
class KizamiEffectAttributeModifier(
    private val modifiers: Map<Attribute, AttributeModifier>,
) : KizamiEffect {

    companion object {
        @JvmField
        val SERIALIZER: TypeSerializer2<KizamiEffectAttributeModifier> = TypeSerializer2 { type, node ->
            val attrId = node.node("id").require<String>()
            val attr = ConstantAttributeBundle(attrId, node)
            val kizamiId = node.hint(RepresentationHints.KIZAMI_ID)
                ?: throw SerializationException(node, type, "Cannot find hint '${RepresentationHints.KIZAMI_ID}' in node '$node'. This is a bug!")
            val modifiers = attr.createAttributeModifiers(kizamiId)

            KizamiEffectAttributeModifier(modifiers)
        }
    }

    override fun apply(player: Player) {
        val attributeContainer = player.attributeContainer
        modifiers.forEach { (attribute, modifier) ->
            attributeContainer.getInstance(attribute)?.addTransientModifier(modifier)
        }
    }

    override fun remove(player: Player) {
        val attributeContainer = player.attributeContainer
        modifiers.forEach { (attribute, modifier) ->
            attributeContainer.getInstance(attribute)?.removeModifier(modifier)
        }
    }
}