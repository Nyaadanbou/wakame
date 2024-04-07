package cc.mewcraft.wakame.display

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.facade.AttributeComponent
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.StringCombiner
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.text.Component
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

/**
 * 代表一个自定义固定内容的 [LoreMeta].
 */
internal data class CustomFixedLoreMeta(
    override val rawIndex: RawIndex,
    override val companionNamespace: String?,
    override val components: List<Component>,
) : FixedLoreMeta

/**
 * 代表一个“无字符”固定内容的 [LoreMeta].
 */
internal data class EmptyFixedLoreMeta(
    override val rawIndex: RawIndex,
    override val companionNamespace: String?,
) : FixedLoreMeta {
    override val components: List<Component> = listOf(Component.empty())
}

/**
 * 代表一个元数据的 [LoreMeta].
 */
internal data class MetaLoreMeta(
    override val rawKey: RawKey,
    override val rawIndex: RawIndex,
    override val default: List<Component>?,
) : DynamicLoreMeta {
    override val fullKeys: List<FullKey> =
        listOf(Key(rawKey.namespace(), rawKey.value()))
}

/**
 * 代表一个技能的 [LoreMeta].
 */
internal data class SkillLoreMeta(
    override val rawKey: RawKey,
    override val rawIndex: RawIndex,
    override val default: List<Component>?,
) : DynamicLoreMeta {
    override val fullKeys: List<FullKey> =
        listOf(Key(rawKey.namespace(), rawKey.value()))
}

/**
 * 代表一个属性的 [LoreMeta].
 */
internal data class AttributeLoreMeta(
    override val rawKey: RawKey,
    override val rawIndex: RawIndex,
    override val default: List<Component>?,
    private val derivation: Derivation,
) : DynamicLoreMeta {
    class Derivation(
        /**
         * 运算模式的顺序。
         */
        operationIndex: Provider<List<String>>,
        /**
         * 元素种类的顺序。
         */
        elementIndex: Provider<List<String>>,
    ) : Examinable {
        val operationIndex: List<String> by operationIndex
        val elementIndex: List<String> by elementIndex

        init {
            // validate values
            this.operationIndex.forEach { AttributeModifier.Operation.byKey(it) }
            this.elementIndex.forEach { ElementRegistry.INSTANCES[it] }
        }

        override fun toString(): String {
            return toSimpleString()
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> {
            return Stream.of(
                ExaminableProperty.of("operationIndex", operationIndex),
                ExaminableProperty.of("elementIndex", elementIndex),
            )
        }
    }

    /**
     * 根据以下衍生规则:
     * - attribute:id:operation         <-- 第一种
     * - attribute:id:operation:element <-- 第二种
     *
     * 为该属性生成所有的 full keys
     */
    override val fullKeys: List<FullKey>
        get() {
            if (rawKey == AttributeRegistry.EMPTY_KEY) {
                return listOf(rawKey) // for `empty`, do not derive
            }

            val namespace = rawKey.namespace()
            val values = StringCombiner(rawKey.value(), ".") {
                addList(derivation.operationIndex)
                addList(derivation.elementIndex, AttributeRegistry.FACADES[rawKey].STRUCTURE_METADATA.hasComponent<AttributeComponent.Element<*>>())
            }.combine()

            return values.map { Key(namespace, it) }
        }
}
