package cc.mewcraft.wakame.item.scheme.meta

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.condition.Condition
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.item.scheme.filter.FilterFactory
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.random.AbstractPoolSerializer
import cc.mewcraft.wakame.random.Pool
import cc.mewcraft.wakame.util.requireKt
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

typealias KizamiPool = Pool<Kizami, SchemeGenerationContext>

/**
 * 物品的铭刻标识。
 *
 * @property kizamiPool 铭刻池
 */
class KizamiMeta(
    private val kizamiPool: KizamiPool = Pool.empty(),
) : SchemeMeta<Set<Kizami>> {
    override fun generate(context: SchemeGenerationContext): Set<Kizami> {
        return kizamiPool.pick(context).toSet()
    }

    companion object : Keyed {
        override fun key(): Key = Key.key(NekoNamespaces.META, "kizami")
    }
}

internal class KizamiMetaSerializer : SchemeMetaSerializer<KizamiMeta> {
    override val emptyValue: KizamiMeta = KizamiMeta()

    override fun deserialize(type: Type, node: ConfigurationNode): KizamiMeta {
        return KizamiMeta(node.requireKt<KizamiPool>())
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
    override fun contentFactory(node: ConfigurationNode): Kizami =
        node.node("value").requireKt<Kizami>()

    override fun conditionFactory(node: ConfigurationNode): Condition<SchemeGenerationContext> {
        return FilterFactory.create(node)
    }

    override fun traceApply(content: Kizami, context: SchemeGenerationContext) {
        context.kizamis += content
    }
}