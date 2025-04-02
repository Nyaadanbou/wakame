package cc.mewcraft.wakame.item.templates.filters

import cc.mewcraft.wakame.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.element.ElementType
import cc.mewcraft.wakame.item.template.AttributeContextData
import cc.mewcraft.wakame.item.template.ItemGenerationContext
import cc.mewcraft.wakame.item.templates.filters.FilterSerializer.NAMESPACE_FILTER
import cc.mewcraft.wakame.random3.Filter
import cc.mewcraft.wakame.random3.Mark
import cc.mewcraft.wakame.rarity2.Rarity
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.util.adventure.toSimpleString
import cc.mewcraft.wakame.util.toStableInt
import com.google.common.collect.Range
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream
import kotlin.random.Random

/**
 * Checks the population of *attribute*.
 *
 * @property invert whether to invert the original result
 * @property id the identifier of the attribute to check with
 * @property operation the operation of the attribute to check with
 * @property element the element of the attribute to check with
 *
 * @see cc.mewcraft.wakame.attribute.Attribute
 */
data class AttributeFilter(
    override val invert: Boolean,
    private val id: String,
    private val operation: Operation?,
    private val element: RegistryEntry<ElementType>?,
) : Filter<ItemGenerationContext>, Examinable {
    companion object {
        val TYPE = Key.key(NAMESPACE_FILTER, "attribute")
    }

    override val kind: Key = TYPE

    /**
     * Returns `true` if the [context] already has the attribute populated.
     */
    override fun testOriginal(context: ItemGenerationContext): Boolean {
        return AttributeContextData(id, operation, element) in context.attributes
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("invert", invert),
            ExaminableProperty.of("id", id),
            ExaminableProperty.of("operation", operation),
            ExaminableProperty.of("element", element),
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}

/**
 * Checks population of *element*.
 *
 * This filter can be used to ensure only **appropriate** elemental
 * attributes to be populated in the generation process. For example, you
 * can use the filter to only populate specific elemental attributes, which
 * avoids the situation where the item could have both "fire attack damage"
 * and "water attack damage rate" attributes simultaneously. In that case,
 * the "water attack damage rate" literally takes no effect, which doesn't
 * make sense to players.
 *
 * @property invert whether to invert the original result
 * @property element the element to check with
 *
 * @see cc.mewcraft.wakame.item.components.ItemElements
 */
data class ElementFilter(
    override val invert: Boolean,
    private val element: RegistryEntry<ElementType>,
) : Filter<ItemGenerationContext>, Examinable {
    companion object {
        val TYPE = Key.key(NAMESPACE_FILTER, "element")
    }

    override val kind: Key = TYPE

    /**
     * Returns `true` if the [context] already has the [element] populated.
     */
    override fun testOriginal(context: ItemGenerationContext): Boolean {
        return element in context.elements
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("invert", invert),
            ExaminableProperty.of("element", element),
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}

/**
 * Checks the population of *item level*.
 *
 * @property invert whether to invert the original result
 * @property level the level range to check with
 *
 * @see cc.mewcraft.wakame.item.components.ItemLevel
 */
data class ItemLevelFilter(
    override val invert: Boolean,
    private val level: Range<Int>,
) : Filter<ItemGenerationContext>, Examinable {
    companion object {
        val TYPE = Key.key(NAMESPACE_FILTER, "item_level")
    }

    override val kind: Key = TYPE

    /**
     * Returns `true` if the item level in the [context] is in the range of [level].
     */
    override fun testOriginal(context: ItemGenerationContext): Boolean {
        val level = context.level
        if (level != null) {
            return level.toStableInt() in this.level
        }
        return false
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("invert", invert),
            ExaminableProperty.of("level", level),
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}

/**
 * Checks the population of *mark*.
 *
 * @property mark the mark value in string to check with
 *
 * @see cc.mewcraft.wakame.random3.Mark
 */
data class MarkFilter(
    override val invert: Boolean,
    private val mark: String,
) : Filter<ItemGenerationContext>, Examinable {
    companion object {
        val TYPE = Key.key(NAMESPACE_FILTER, "mark")
    }

    override val kind: Key = TYPE

    /**
     * Returns `true` if the [context] already has the [mark] populated.
     */
    override fun testOriginal(context: ItemGenerationContext): Boolean {
        return Mark(mark) in context.marks
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("invert", invert),
            ExaminableProperty.of("mark", mark),
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}

/**
 * Checks the population of *rarity*.
 *
 * @property invert whether to invert the original result
 * @property rarity the item rarity to check with
 *
 * @see cc.mewcraft.wakame.item.components.ItemRarity
 */
data class RarityFilter(
    override val invert: Boolean,
    private val rarity: RegistryEntry<Rarity>,
) : Filter<ItemGenerationContext>, Examinable {
    companion object {
        val TYPE = Key.key(NAMESPACE_FILTER, "rarity")
    }

    override val kind: Key = TYPE

    /**
     * Returns `true` if the [context] already has the [rarity] populated.
     */
    override fun testOriginal(context: ItemGenerationContext): Boolean {
        return rarity == context.rarity
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("invert", invert),
            ExaminableProperty.of("rarity", rarity),
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}

/**
 * Checks the population of *source level*.
 *
 * @property invert whether to invert the original result
 * @property level the level range to check with
 */
data class SourceLevelFilter(
    override val invert: Boolean,
    private val level: Range<Int>,
) : Filter<ItemGenerationContext>, Examinable {
    companion object {
        val TYPE = Key.key(NAMESPACE_FILTER, "source_level")
    }

    override val kind: Key = TYPE

    /**
     * Returns `true` if the source level in the [context] is in the range of [level].
     */
    override fun testOriginal(context: ItemGenerationContext): Boolean {
        return (context.trigger.level) in level
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("invert", invert),
            ExaminableProperty.of("level", level),
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}

/**
 * Checks [probability].
 *
 * @property invert whether to invert the original result
 * @property probability the probability of success for this toss
 */
data class TossFilter(
    override val invert: Boolean,
    private val probability: Float,
) : Filter<ItemGenerationContext>, Examinable {
    companion object {
        val TYPE = Key.key(NAMESPACE_FILTER, "toss")
    }

    override val kind: Key = TYPE

    /**
     * Returns `true` if the toss is success.
     */
    override fun testOriginal(context: ItemGenerationContext): Boolean {
        return Random.nextFloat() < probability
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("invert", invert),
            ExaminableProperty.of("probability", probability),
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}