package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.ability.PlayerAbility
import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.bundle.ConstantAttributeBundle
import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.registry2.Registry
import cc.mewcraft.wakame.registry2.SimpleRegistry
import cc.mewcraft.wakame.serialization.configurate.RepresentationHints
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.util.require
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.serialize.SerializationException

/**
 * 铭刻的效果.
 */
interface KizamiEffect {
    val type: KizamiEffectType<*>
    fun apply(user: User<*>)
    fun remove(user: User<*>)
}

/**
 * 铭刻效果的类型.
 */
class KizamiEffectType<T : KizamiEffect>(val type: TypeToken<T>) {
    val id: String
        get() = REGISTRY.getId(this).toString()

    companion object {
        val REGISTRY: SimpleRegistry<KizamiEffectType<*>> = Registry.of("kizami_effect_type")
    }
}

/**
 * 铭刻效果类型的注册表.
 */
internal object KizamiEffectTypes {
    val PLAYER_ABILITY = register<KizamiEffectPlayerAbility>("player_ability")
    val ATTRIBUTE_MODIFIER = register<KizamiEffectAttributeModifier>("attribute_modifier")

    private inline fun <reified T : KizamiEffect> register(id: String): KizamiEffectType<T> {
        return Registry.register(KizamiEffectType.REGISTRY, id, KizamiEffectType(typeTokenOf()))
    }
}

/**
 * 铭刻效果：玩家技能.
 */
internal class KizamiEffectPlayerAbility(
    private val ability: PlayerAbility,
) : KizamiEffect {
    override val type: KizamiEffectType<*> = KizamiEffectTypes.PLAYER_ABILITY

    override fun apply(user: User<*>) {
        ability.recordBy(user.player(), null, null)
    }

    override fun remove(user: User<*>) {
        // do nothing
    }

    companion object {
        val SERIALIZER = TypeSerializer<KizamiEffectPlayerAbility> { type, node ->
            val id = node.node("id").require<Key>()
            val ability = PlayerAbility(id, node)

            KizamiEffectPlayerAbility(ability)
        }
    }
}

/**
 * 铭刻效果：属性修饰器.
 */
internal class KizamiEffectAttributeModifier(
    private val modifiers: Map<Attribute, AttributeModifier>,
) : KizamiEffect {
    override val type: KizamiEffectType<*> = KizamiEffectTypes.ATTRIBUTE_MODIFIER

    override fun apply(user: User<*>) {
        modifiers.forEach { (attribute, modifier) ->
            user.attributeMap.getInstance(attribute)?.addTransientModifier(modifier)
        }
    }

    override fun remove(user: User<*>) {
        modifiers.forEach { (attribute, modifier) ->
            user.attributeMap.getInstance(attribute)?.removeModifier(modifier)
        }
    }

    companion object {
        val SERIALIZER = TypeSerializer<KizamiEffectAttributeModifier> { type, node ->
            val id = node.node("id").require<String>()
            val attribute = ConstantAttributeBundle(id, node)
            val kizamiId = node.hint(RepresentationHints.KIZAMI_ID)
                ?: throw SerializationException(node, type, "No such hint '${RepresentationHints.KIZAMI_ID}' in node '$node'")
            val modifiers = attribute.createAttributeModifiers(kizamiId)

            KizamiEffectAttributeModifier(modifiers)
        }
    }
}
