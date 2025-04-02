package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.bundle.ConstantAttributeBundle
import cc.mewcraft.wakame.config.configurate.TypeSerializer2
import cc.mewcraft.wakame.kizami2.KizamiEffect
import cc.mewcraft.wakame.serialization.configurate.RepresentationHints
import cc.mewcraft.wakame.user.attributeContainer
import cc.mewcraft.wakame.util.require
import org.bukkit.entity.Player
import org.spongepowered.configurate.serialize.SerializationException

/**
 * 铭刻效果：属性修饰器.
 */
internal class KizamiEffectAttributeModifier(
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
