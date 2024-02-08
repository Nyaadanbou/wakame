package cc.mewcraft.wakame.item.scheme.core

import cc.mewcraft.wakame.ability.AbilityBinaryValue
import cc.mewcraft.wakame.ability.AbilityCoreCodec
import cc.mewcraft.wakame.ability.AbilityCoreCodecRegistry
import cc.mewcraft.wakame.ability.AbilitySchemeValue
import cc.mewcraft.wakame.attribute.BinaryAttributeValue
import cc.mewcraft.wakame.attribute.SchemeAttributeValue
import cc.mewcraft.wakame.attribute.AttributeFacade
import cc.mewcraft.wakame.attribute.AttributeCoreCodecRegistry
import cc.mewcraft.wakame.item.Core
import cc.mewcraft.wakame.util.typedRequire
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode

/**
 * A factory used to create [SchemeCore].
 */
object SchemeCoreFactory {

    /**
     * 由配置文件调用，因此已知：
     * - [ConfigurationNode]
     *
     * 由此已知：
     * - Namespace
     * - Value
     *
     * 通过 Namespace + ID 我们可以唯一确定用什么实现来反序列化该 [ConfigurationNode].
     */
    fun schemeOf(node: ConfigurationNode): SchemeCore {
        val key = node.node("key").typedRequire<Key>()
        val ret: SchemeCore = when (key.namespace()) {
            Core.ABILITY_NAMESPACE -> {
                val codec: AbilityCoreCodec<AbilityBinaryValue, AbilitySchemeValue> = AbilityCoreCodecRegistry.getOrThrow(key)
                val value: AbilitySchemeValue = codec.schemeOf(node)
                SchemeAbilityCore(key, value)
            }

            Core.ATTRIBUTE_NAMESPACE -> {
                val codec: AttributeFacade<BinaryAttributeValue, SchemeAttributeValue> = AttributeCoreCodecRegistry.getOrThrow(key)
                val value: SchemeAttributeValue = codec.schemeOf(node)
                SchemeAttributeCore(key, value)
            }

            else -> throw IllegalArgumentException()
        }

        return ret
    }
}