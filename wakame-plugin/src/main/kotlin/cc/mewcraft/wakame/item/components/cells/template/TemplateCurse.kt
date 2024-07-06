package cc.mewcraft.wakame.item.components.cells.template

import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.adventure.key.Keyed
import cc.mewcraft.wakame.config.configurate.TypeDeserializer
import cc.mewcraft.wakame.item.CurseConstants
import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.item.components.cells.Curse
import cc.mewcraft.wakame.item.components.cells.template.curses.TemplateCurseEmpty
import cc.mewcraft.wakame.item.components.cells.template.curses.TemplateCurseEntityKills
import cc.mewcraft.wakame.item.components.cells.template.curses.TemplateCursePeakDamage
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.templates.filter.CurseContextHolder
import cc.mewcraft.wakame.random2.Filter
import cc.mewcraft.wakame.random2.GroupSerializer
import cc.mewcraft.wakame.random2.Pool
import cc.mewcraft.wakame.random2.PoolSerializer
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

/**
 * 代表一个[诅咒][Curse]的模板.
 */
interface TemplateCurse : Keyed, Examinable {

    /**
     * 用于序列化确定诅咒的类型.
     */
    override val key: Key

    /**
     * Generates a value from this template.
     *
     * **The generation must correctly populate the [context]!**
     *
     * @param context the generation context
     * @return a new instance of [Core]
     */
    fun generate(context: GenerationContext): Curse
}

/**
 * ## Node structure
 * ```yaml
 * <node>:
 *   key: <key>
 *   ...
 * ```
 */
internal object TemplateCurseSerializer : TypeDeserializer<TemplateCurse> {
    override fun deserialize(type: Type, node: ConfigurationNode): TemplateCurse {
        val ret = when (val key = node.node("key").krequire<Key>()) {
            // 技术诅咒
            GenericKeys.EMPTY -> TemplateCurseEmpty

            // 普通诅咒
            CurseConstants.createKey { ENTITY_KILLS } -> TemplateCurseEntityKills(node)
            CurseConstants.createKey { PEAK_DAMAGE } -> TemplateCursePeakDamage(node)

            // 大概是配置文件写错了
            else -> throw IllegalArgumentException("Unknown curse: $key")
        }
        return ret
    }
}

/**
 * ## Node structure 1 (fallback)
 *
 * ```yaml
 * <node>:
 *   - key: curse:highest_damage
 *     weight: 1
 *     element: fire
 *     amount: 10
 *   - key: curse:highest_damage
 *     weight: 1
 *     element: water
 *     amount: 16
 * ```
 *
 * ## Node structure 2
 *
 * ```yaml
 * <node>:
 *   filters:
 *     - type: rarity
 *       rarity: common
 *   entries:
 *     - key: curse:highest_damage
 *       weight: 1
 *       element: fire
 *       amount: 20
 *     - key: curse:highest_damage
 *       weight: 1
 *       element: water
 *       amount: 32
 * ```
 */
internal object TemplateCursePoolSerializer : PoolSerializer<TemplateCurse, GenerationContext>() {
    override fun sampleFactory(node: ConfigurationNode): TemplateCurse {
        return node.krequire<TemplateCurse>()
    }

    override fun filterFactory(node: ConfigurationNode): Filter<GenerationContext> {
        return node.krequire<Filter<GenerationContext>>()
    }

    override fun onPickSample(content: TemplateCurse, context: GenerationContext) {
        context.curses += CurseContextHolder(content.key)
    }
}

/**
 * @see GroupSerializer
 */
internal object TemplateCurseGroupSerializer : GroupSerializer<TemplateCurse, GenerationContext>() {
    override fun poolFactory(node: ConfigurationNode): Pool<TemplateCurse, GenerationContext> {
        return node.krequire<Pool<TemplateCurse, GenerationContext>>()
    }

    override fun filterFactory(node: ConfigurationNode): Filter<GenerationContext> {
        return node.krequire<Filter<GenerationContext>>()
    }
}