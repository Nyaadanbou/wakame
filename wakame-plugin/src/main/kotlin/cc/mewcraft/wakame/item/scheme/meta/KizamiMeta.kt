package cc.mewcraft.wakame.item.scheme.meta

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.adventure.Keyed
import cc.mewcraft.wakame.condition.Condition
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.item.scheme.filter.FilterFactory
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.random.AbstractGroupSerializer
import cc.mewcraft.wakame.random.AbstractPoolSerializer
import cc.mewcraft.wakame.random.Group
import cc.mewcraft.wakame.random.Pool
import cc.mewcraft.wakame.util.requireKt
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

typealias KizamiPool = Pool<Kizami, SchemeGenerationContext>
typealias KizamiGroup = Group<Kizami, SchemeGenerationContext>

/**
 * 物品的铭刻标识。
 */
sealed interface KizamiMeta : SchemeItemMeta<Set<Kizami>> {
    companion object : Keyed {
        override val key: Key = Key.key(NekoNamespaces.ITEM_META, "kizami")
    }
}

private class NonNullKizamiMeta(
    private val kizamiGroup: KizamiGroup,
) : KizamiMeta {
    override fun generate(context: SchemeGenerationContext): Set<Kizami>? {
        return kizamiGroup.pick(context).toSet().takeIf { it.isNotEmpty() }
    }
}

private data object DefaultKizamiMeta : KizamiMeta {
    override fun generate(context: SchemeGenerationContext): Set<Kizami>? = null
}

internal class KizamiMetaSerializer : SchemeItemMetaSerializer<KizamiMeta> {
    override val defaultValue: KizamiMeta = DefaultKizamiMeta
    override fun deserialize(type: Type, node: ConfigurationNode): KizamiMeta {
        return NonNullKizamiMeta(node.requireKt<KizamiGroup>())
    }
}

/**
 * @see AbstractGroupSerializer
 */
internal class KizamiGroupSerializer : AbstractGroupSerializer<Kizami, SchemeGenerationContext>() {
    override fun poolFactory(node: ConfigurationNode): Pool<Kizami, SchemeGenerationContext> {
        return node.requireKt<KizamiPool>()
    }

    override fun conditionFactory(node: ConfigurationNode): Condition<SchemeGenerationContext> {
        return FilterFactory.create(node)
    }
}

/**
 * ## Node structure
 *
 * ```yaml
 * <node>:
 *   sample: 2
 *   filters: [ ]
 *   entries:
 *     - value: wood
 *       weight: 3
 *     - value: iron
 *       weight: 2
 *     - value: gold
 *       weight: 1
 *     - value: diamond
 *       weight: 1
 *     - value: netherite
 *       weight: 1
 * ```
 */
internal class KizamiPoolSerializer : AbstractPoolSerializer<Kizami, SchemeGenerationContext>() {
    override fun contentFactory(node: ConfigurationNode): Kizami {
        return node.node("value").requireKt<Kizami>()
    }

    override fun conditionFactory(node: ConfigurationNode): Condition<SchemeGenerationContext> {
        return FilterFactory.create(node)
    }

    override fun onPickSample(content: Kizami, context: SchemeGenerationContext) {
        context.kizamis += content
    }
}