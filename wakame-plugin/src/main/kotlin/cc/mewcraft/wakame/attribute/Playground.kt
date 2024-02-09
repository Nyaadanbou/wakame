package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.item.*
import cc.mewcraft.wakame.util.*
import com.google.common.collect.ImmutableMap
import me.lucko.helper.nbt.ShadowTagType
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent

object PlaygroundRegistry {
    fun initialize() {
        AttributeFacadeRegistry.build(
            key = "lifesteal",
            type = ShadowTagType.DOUBLE,
        ).single().bind(
            component = Attributes.LIFESTEAL,
        )

        AttributeFacadeRegistry.build(
            key = "attack_damage",
            type = ShadowTagType.SHORT,
        ).ranged().bind(
            component1 = Attributes.MIN_ATTACK_DAMAGE,
            component2 = Attributes.MAX_ATTACK_DAMAGE,
        )

        AttributeFacadeRegistry.build(
            key = "lifesteal",
            type = ShadowTagType.SHORT,
        ).single().element().bind(
            component = Attributes.byElement { LIFESTEAL },
        )

        AttributeFacadeRegistry.build(
            key = "attack_damage",
            type = ShadowTagType.SHORT,
        ).ranged().element().bind(
            component1 = Attributes.byElement { MIN_ATTACK_DAMAGE },
            component2 = Attributes.byElement { MAX_ATTACK_DAMAGE },
        )
    }
}

object AttributeFacadeRegistry {
    @InternalApi
    val schemeBuilderRegistry: MutableMap<Key, SchemeBuilder> = hashMapOf()

    @InternalApi
    val schemeBakerRegistry: MutableMap<Key, SchemeBaker> = hashMapOf()

    @InternalApi
    val shadowTagEncoder: MutableMap<Key, ShadowTagEncoder> = hashMapOf()

    @InternalApi
    val shadowTagDecoder: MutableMap<Key, ShadowTagDecoder> = hashMapOf()

    @InternalApi
    val attributeFactoryRegistry: MutableMap<Key, AttributeFactory<*>> = hashMapOf()

    fun build(key: String, type: ShadowTagType): FormatSelection {
        return FormatSelection(Key.key(Core.ATTRIBUTE_NAMESPACE, key), type)
    }
}

@OptIn(InternalApi::class)
class FormatSelection(
    val key: Key,
    val type: ShadowTagType,
) {
    fun ranged(): RangedSelection {
        return RangedSelection(key, type)
    }

    fun single(): SingleSelection {
        return SingleSelection(key, type)
    }
}

@OptIn(InternalApi::class)
class SingleSelection(
    val key: Key,
    val type: ShadowTagType,
) {
    fun element(): SingleElementAttributeBinder {
        return SingleElementAttributeBinder(key, type)
    }

    @Suppress("UNCHECKED_CAST")
    fun bind(component: Attribute) {
        // register scheme builder
        AttributeFacadeRegistry.schemeBuilderRegistry[key] = SchemeBuilder { node -> SchemeAttributeValueS.deserialize(node) }

        // register scheme baker
        AttributeFacadeRegistry.schemeBakerRegistry[key] = SchemeBaker { scheme, factor ->
            scheme as SchemeAttributeValueS
            val value: Double = scheme.value.calculate(factor)
            val ret: BinaryAttributeValueS<out Number> = when (type) {
                ShadowTagType.BYTE -> BinaryAttributeValueS(value.toStableByte(), scheme.operation)
                ShadowTagType.SHORT -> BinaryAttributeValueS(value.toStableShort(), scheme.operation)
                ShadowTagType.INT -> BinaryAttributeValueS(value.toStableInt(), scheme.operation)
                ShadowTagType.LONG -> BinaryAttributeValueS(value.toStableLong(), scheme.operation)
                ShadowTagType.FLOAT -> BinaryAttributeValueS(value.toStableFloat(), scheme.operation)
                ShadowTagType.DOUBLE -> BinaryAttributeValueS(value.toStableDouble(), scheme.operation)
                else -> throw IllegalArgumentException()
            }
            return@SchemeBaker ret
        }

        // register shadow tag encoder
        AttributeFacadeRegistry.shadowTagEncoder[key] = ShadowTagEncoder {
            val ret: CompoundShadowTag = compoundShadowTag {
                // put value
                when (type) {
                    ShadowTagType.BYTE -> putByte(AttributeTagNames.VALUE, (it as BinaryAttributeValueS<Byte>).value)
                    ShadowTagType.SHORT -> putShort(AttributeTagNames.VALUE, (it as BinaryAttributeValueS<Short>).value)
                    ShadowTagType.INT -> putInt(AttributeTagNames.VALUE, (it as BinaryAttributeValueS<Int>).value)
                    ShadowTagType.LONG -> putLong(AttributeTagNames.VALUE, (it as BinaryAttributeValueS<Long>).value)
                    ShadowTagType.FLOAT -> putFloat(AttributeTagNames.VALUE, (it as BinaryAttributeValueS<Float>).value)
                    ShadowTagType.DOUBLE -> putDouble(AttributeTagNames.VALUE, (it as BinaryAttributeValueS<Double>).value)
                    else -> throw IllegalArgumentException()
                }

                // put operation
                putByte(AttributeTagNames.OPERATION, (it as BinaryAttributeValueS<*>).operation.binary)
            }
            return@ShadowTagEncoder ret
        }

        // register shadow tag decoder
        AttributeFacadeRegistry.shadowTagDecoder[key] = ShadowTagDecoder {
            it as CompoundShadowTag
            val ret: BinaryAttributeValueS<out Number> = when (type) {
                ShadowTagType.BYTE -> BinaryAttributeValueS(it.getByte(AttributeTagNames.VALUE), AttributeModifier.Operation.byId(it.getInt(AttributeTagNames.OPERATION)))
                ShadowTagType.SHORT -> BinaryAttributeValueS(it.getShort(AttributeTagNames.VALUE), AttributeModifier.Operation.byId(it.getInt(AttributeTagNames.OPERATION)))
                ShadowTagType.INT -> BinaryAttributeValueS(it.getInt(AttributeTagNames.VALUE), AttributeModifier.Operation.byId(it.getInt(AttributeTagNames.OPERATION)))
                ShadowTagType.LONG -> BinaryAttributeValueS(it.getLong(AttributeTagNames.VALUE), AttributeModifier.Operation.byId(it.getInt(AttributeTagNames.OPERATION)))
                ShadowTagType.FLOAT -> BinaryAttributeValueS(it.getFloat(AttributeTagNames.VALUE), AttributeModifier.Operation.byId(it.getInt(AttributeTagNames.OPERATION)))
                ShadowTagType.DOUBLE -> BinaryAttributeValueS(it.getDouble(AttributeTagNames.VALUE), AttributeModifier.Operation.byId(it.getInt(AttributeTagNames.OPERATION)))
                else -> throw IllegalArgumentException()
            }
            return@ShadowTagDecoder ret
        }

        // register attribute factory
        AttributeFacadeRegistry.attributeFactoryRegistry[key] = AttributeFactory<BinaryAttributeValueS<out Number>> { uuid, value ->
            ImmutableMap.of(component, AttributeModifier(uuid, value.value.toStableDouble(), value.operation))
        }
    }
}

@OptIn(InternalApi::class)
class RangedSelection(
    val key: Key,
    val type: ShadowTagType,
) {
    fun element(): RangedElementAttributeBinder {
        return RangedElementAttributeBinder(key, type)
    }

    fun bind(
        component1: Attribute,
        component2: Attribute,
    ) {
        TODO()
    }
}

@OptIn(InternalApi::class)
class SingleElementAttributeBinder(
    val key: Key,
    val type: ShadowTagType,
) {
    fun bind(
        component: (Element) -> ElementAttribute,
    ) {
        TODO()
    }
}

@OptIn(InternalApi::class)
class RangedElementAttributeBinder(
    val key: Key,
    val type: ShadowTagType,
) {
    fun bind(
        component1: (Element) -> ElementAttribute,
        component2: (Element) -> ElementAttribute,
    ) {
        TODO()
    }
}

object FacadeMaker : KoinComponent, Initializable {

    inline fun <reified T> createSchemeBuilder(): SchemeBuilder {
        TODO()
    }

    inline fun <reified T> createSchemeMaker(): SchemeBaker {
        TODO()
    }

    inline fun <reified T> createShadowTagEncoder(): ShadowTagEncoder {
        TODO()
    }

    inline fun <reified T> createShadowTagDecoder(): ShadowTagDecoder {
        TODO()
    }

    inline fun <reified T> createAttributeModifierProvider(): AttributeModifierProvider {
        TODO()
    }
}