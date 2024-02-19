package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.Reloadable
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.attribute.base.Attribute
import cc.mewcraft.wakame.attribute.base.Attributes
import cc.mewcraft.wakame.attribute.base.ElementAttribute
import cc.mewcraft.wakame.attribute.facade.AttributeModifierFactory
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.initializer.DependencyConfig
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.item.SchemeBaker
import cc.mewcraft.wakame.item.SchemeBuilder
import cc.mewcraft.wakame.item.ShadowTagDecoder
import cc.mewcraft.wakame.item.ShadowTagEncoder
import cc.mewcraft.wakame.registry.AttributeStructMeta.Format
import me.lucko.helper.nbt.ShadowTagType
import net.kyori.adventure.key.Key

/**
 * This singleton holds various implementations for **each** attribute in
 * the server.
 *
 * Currently, the types of implementations are the following:
 * - [SchemeBuilder]
 * - [SchemeBaker]
 * - [ShadowTagEncoder]
 * - [ShadowTagDecoder]
 * - [AttributeModifierFactory]
 *
 * Check their kdoc for what they do.
 */
@DependencyConfig(
    preWorldBefore = [ElementRegistry::class]
)
object AttributeRegistry : Initializable, Reloadable {

    @InternalApi
    val schemeBuilderRegistry: MutableMap<Key, SchemeBuilder> = hashMapOf()

    @InternalApi
    val schemeBakerRegistry: MutableMap<Key, SchemeBaker> = hashMapOf()

    @InternalApi
    val shadowTagEncoder: MutableMap<Key, ShadowTagEncoder> = hashMapOf()

    @InternalApi
    val shadowTagDecoder: MutableMap<Key, ShadowTagDecoder> = hashMapOf()

    @InternalApi
    val attributeFactoryRegistry: MutableMap<Key, AttributeModifierFactory> = hashMapOf()

    /**
     * Registers an attribute facade.
     *
     * ## 参数: [key]
     * 词条在 NBT/模板 中的唯一标识，用来定位词条的序列化实现。
     *
     * 注意，这仅仅是词条在 NBT/模板 中的唯一标识。底层由多个对象组成的词条标识就与这里的 [key] 不同。
     *
     * 例如攻击力这个属性词条，底层实际上是由两个属性组成的，分别是 `MIN_ATTACK_DAMAGE` 和
     * `MAX_ATTACK_DAMAGE`，但攻击力属性词条在 NBT/模板中的标识是一个经过“合并”得到的
     * `attribute:attack_damage`.
     *
     * ## 参数: [type]
     * 词条在 NBT 中的数据类型。
     */
    private fun build(key: String, type: ShadowTagType): FormatSelection {
        return (@OptIn(InternalApi::class) FormatSelectionImpl(Key.key(NekoNamespaces.ATTRIBUTE, key), type))
    }

    private fun register() {
        build("attack_damage", ShadowTagType.SHORT).ranged().element().bind(Attributes.byElement { MIN_ATTACK_DAMAGE }, Attributes.byElement { MAX_ATTACK_DAMAGE })
        build("attack_effect_chance", ShadowTagType.DOUBLE).single().bind(Attributes.ATTACK_EFFECT_CHANCE)
        build("attack_speed_level", ShadowTagType.BYTE).single().bind(Attributes.ATTACK_SPEED_LEVEL)
        build("critical_strike_chance", ShadowTagType.DOUBLE).single().bind(Attributes.CRITICAL_STRIKE_CHANCE)
        build("critical_strike_power", ShadowTagType.DOUBLE).single().bind(Attributes.CRITICAL_STRIKE_POWER)
        build("damage_reduction_rate", ShadowTagType.DOUBLE).single().bind(Attributes.DAMAGE_REDUCTION_RATE)
        build("defense", ShadowTagType.SHORT).single().element().bind(Attributes.byElement { DEFENSE })
        build("defense_penetration", ShadowTagType.SHORT).single().element().bind(Attributes.byElement { DEFENSE_PENETRATION })
        build("defense_penetration_rate", ShadowTagType.DOUBLE).single().element().bind(Attributes.byElement { DEFENSE_PENETRATION_RATE })
        build("health_regeneration", ShadowTagType.SHORT).single().bind(Attributes.HEALTH_REGENERATION)
        build("lifesteal", ShadowTagType.SHORT).single().bind(Attributes.LIFESTEAL)
        build("lifesteal_rate", ShadowTagType.DOUBLE).single().bind(Attributes.LIFESTEAL_RATE)
        build("mana_consumption_rate", ShadowTagType.DOUBLE).single().bind(Attributes.MANA_CONSUMPTION_RATE)
        build("mana_regeneration", ShadowTagType.SHORT).single().bind(Attributes.MANA_REGENERATION)
        build("manasteal", ShadowTagType.SHORT).single().bind(Attributes.MANASTEAL)
        build("manasteal_rate", ShadowTagType.DOUBLE).single().bind(Attributes.MANASTEAL_RATE)
        build("max_absorption", ShadowTagType.SHORT).single().bind(Attributes.MAX_ABSORPTION)
        build("max_health", ShadowTagType.SHORT).single().bind(Attributes.MAX_HEALTH)
        build("max_mana", ShadowTagType.SHORT).single().bind(Attributes.MAX_MANA)
        build("movement_speed_rate", ShadowTagType.DOUBLE).single().bind(Attributes.MOVEMENT_SPEED_RATE)
    }

    fun getMeta(key: Key): AttributeStructMeta {
        TODO("implement attribute meta")
    }

    override fun onPreWorld() {
        register()
    }

    override fun onReload() {
        // TODO("Not yet implemented") // what to reload?
    }
}

//<editor-fold desc="Struct">
/**
 * 属性结构体的元数据。
 */
data class AttributeStructMeta(
    /**
     * 数值的格式。
     */
    val format: Format,
    /**
     * 是否为元素属性。
     */
    val element: Boolean,
) {
    enum class Format { SINGLE, RANGED }
}

/**
 * 属性结构体的所有类型。
 */
enum class AttributeStructType(
    val meta: AttributeStructMeta,
) {
    SINGLE(AttributeStructMeta(Format.SINGLE, false)),
    RANGED(AttributeStructMeta(Format.RANGED, false)),
    SINGLE_ELEMENT(AttributeStructMeta(Format.SINGLE, true)),
    RANGED_ELEMENT(AttributeStructMeta(Format.RANGED, true));
}
//</editor-fold>

//<editor-fold desc="Builder">
interface FormatSelection {
    fun single(): SingleSelection
    fun ranged(): RangedSelection
}

interface SingleSelection : SingleAttributeBinder {
    fun element(): SingleElementAttributeBinder
}

interface RangedSelection : RangedAttributeBinder {
    fun element(): RangedElementAttributeBinder
}

interface SingleAttributeBinder {
    fun bind(component: Attribute)
}

interface RangedAttributeBinder {
    fun bind(component1: Attribute, component2: Attribute)
}

interface SingleElementAttributeBinder {
    fun bind(component: (Element) -> ElementAttribute)
}

interface RangedElementAttributeBinder {
    fun bind(component1: (Element) -> ElementAttribute, component2: (Element) -> ElementAttribute)
}
//</editor-fold>
