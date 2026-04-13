package cc.mewcraft.wakame.entity.attribute.source

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.lazyconfig.configurate.require
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.entity.attribute.Attribute
import cc.mewcraft.wakame.entity.attribute.AttributeMapAccess
import cc.mewcraft.wakame.entity.attribute.AttributeModifier
import cc.mewcraft.wakame.entity.attribute.bundle.ConstantAttributeBundle
import cc.mewcraft.wakame.serialization.configurate.RepresentationHints
import cc.mewcraft.wakame.util.KoishKeys
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import org.bukkit.entity.LivingEntity
import org.bukkit.potion.PotionEffectType
import org.spongepowered.configurate.serialize.SerializationException

/**
 * 属性来源.
 */
sealed interface AttributeSource {
    fun apply(entity: LivingEntity)
    fun remove(entity: LivingEntity)
}


/**
 * 状态效果属性来源.
 */
class EffectAttributeSource(
    private val effectType: PotionEffectType,
    private val baseModifiers: Map<Attribute, AttributeModifier>,
    private val numberModifier: NumberModifier
) : AttributeSource {
    companion object {
        fun serializer(): SimpleSerializer<EffectAttributeSource> {
            return SimpleSerializer { type, node ->
                val attributeId = node.node("id").require<String>()
                val attributeBundle = ConstantAttributeBundle(attributeId, node)
                val effectType = node.hint(RepresentationHints.EFFECT_TYPE)
                    ?: throw SerializationException(node, type, "Cannot find hint '${RepresentationHints.EFFECT_TYPE}' in node '$node'. This is a bug!")
                val baseModifiers = attributeBundle.createAttributeModifiers(KoishKeys.of("effect.${effectType.key().value()}"))
                val numberModifier = node.node("number_modifier").require<NumberModifier>()

                EffectAttributeSource(effectType, baseModifiers, numberModifier)
            }
        }
    }

    val cache: MutableMap<Attribute, Int2ObjectOpenHashMap<AttributeModifier>> = baseModifiers.mapValues { Int2ObjectOpenHashMap<AttributeModifier>() }.toMutableMap()

    /**
     * 方便函数.
     * 查找缓存中有没有对应效果等级的 AttributeModifier, 没有的话就置入一个再返回.
     */
    fun getModifier(attribute: Attribute, baseModifier: AttributeModifier, effectLevel: Int): AttributeModifier {
        return cache[attribute]!!.computeIfAbsent(effectLevel) { effectLevel ->
            val context = NumberModifierContext(baseModifier.amount, effectLevel)
            val newAmount = numberModifier.modify(context)
            baseModifier.copy(amount = newAmount)
        }
    }

    override fun apply(entity: LivingEntity) {
        val attributeMap = AttributeMapAccess.get(entity).getOrElse {
            LOGGER.warn("Failed to apply effect attribute because the entity $entity does not have an attribute map.")
            return
        }
        val potionEffect = entity.getPotionEffect(effectType) ?: return
        val effectLevel = potionEffect.amplifier + 1
        baseModifiers.forEach { (attribute, baseModifier) ->
            // 应用状态效果等级对应的属性修饰符
            attributeMap.getInstance(attribute)?.addModifier(getModifier(attribute, baseModifier, effectLevel))
        }
    }

    override fun remove(entity: LivingEntity) {
        val attributeMap = AttributeMapAccess.get(entity).getOrElse {
            LOGGER.warn("Failed to remove effect attribute because the entity $entity does not have an attribute map.")
            return
        }
        baseModifiers.forEach { (attribute, baseModifier) ->
            // 不同状态效果等级对应的属性修饰符 id 一致, 直接基于 id 移除
            attributeMap.getInstance(attribute)?.removeModifier(baseModifier.id)
        }
    }
}