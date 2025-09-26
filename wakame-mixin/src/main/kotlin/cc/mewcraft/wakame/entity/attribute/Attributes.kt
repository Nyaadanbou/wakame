package cc.mewcraft.wakame.entity.attribute

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.registry.entry.RegistryEntry
import com.google.common.collect.MultimapBuilder
import com.google.common.collect.SetMultimap
import org.jetbrains.annotations.ApiStatus
import java.util.concurrent.ConcurrentHashMap

/**
 * The container that holds all [Attribute] instances.
 *
 * The attribute instances in this singleton object are primarily served as
 * "lookup index" for other code in this project. The numeric values passing to the
 * attribute constructors are just fallback values when the config provides nothing.
 */
object Attributes : AttributeProvider {
    //<editor-fold desc="原版属性">

    // 这些属性需要原版属性作为后端才能在游戏中生效.

    @JvmField
    val ATTACK_KNOCKBACK = RangedAttribute("attack_knockback", .0, .0, 5.0, true).register()
    @JvmField
    val BLOCK_INTERACTION_RANGE = RangedAttribute("block_interaction_range", 4.5, 1.0, 64.0, true).register()
    @JvmField
    val ENTITY_INTERACTION_RANGE = RangedAttribute("entity_interaction_range", 3.0, 1.0, 64.0, true).register()
    @JvmField
    val KNOCKBACK_RESISTANCE = RangedAttribute("knockback_resistance", .0, .0, 1.0, true).register()
    @JvmField
    val MAX_ABSORPTION = RangedAttribute("max_absorption", .0, .0, 2048.0, true).register()
    @JvmField
    val MAX_HEALTH = RangedAttribute("max_health", 20.0, 1.0, 16384.0, true).register()
    @JvmField
    val MINING_EFFICIENCY = RangedAttribute("mining_efficiency", .0, .0, 1024.0, true).register()
    @JvmField
    val MOVEMENT_SPEED = RangedAttribute("movement_speed", .0, -1.0, 4.0, true).register()
    @JvmField
    val SAFE_FALL_DISTANCE = RangedAttribute("safe_fall_distance", 3.0, -1024.0, 1024.0, true).register()
    @JvmField
    val SCALE = RangedAttribute("scale", 1.0, 0.0625, 16.0, true).register()
    @JvmField
    val STEP_HEIGHT = RangedAttribute("step_height", 0.6, .0, 10.0, true).register()
    @JvmField
    val SWEEPING_DAMAGE_RATIO = RangedAttribute("sweeping_damage_ratio", 0.1, .0, 1.0, true).register()
    @JvmField
    val WATER_MOVEMENT_EFFICIENCY = RangedAttribute("water_movement_efficiency", .0, .0, 1.0, true).register()
    //</editor-fold>

    //<editor-fold desc="萌芽属性">

    // 这些属性需要我们自己实现才能在游戏中生效. 所谓“自己实现”,
    // 就是说, 我们需要通过自定义监听器或调度器等方式来实现它们.

    @JvmField
    val ATTACK_EFFECT_CHANCE = RangedAttribute("attack_effect_chance", 0.01, .0, 1.0).register()
    @JvmField
    val CRITICAL_STRIKE_CHANCE = RangedAttribute("critical_strike_chance", .0, -1.0, 1.0).register()
    @JvmField
    val CRITICAL_STRIKE_POWER = RangedAttribute("critical_strike_power", 1.0, .0, 16384.0).register()
    @JvmField
    val DAMAGE_RATE_BY_UNTARGETED = RangedAttribute("damage_rate_by_untargeted", 1.0, .0, 16384.0).register()
    @JvmField
    val HAMMER_DAMAGE_RANGE = RangedAttribute("hammer_damage_range", .0, .0, 64.0).register()
    @JvmField
    val HAMMER_DAMAGE_RATIO = RangedAttribute("hammer_damage_ratio", .0, .0, 1.0).register()
    @JvmField
    val HEALTH_REGENERATION = RangedAttribute("health_regeneration", 1.0, .0, 16384.0).register()
    @JvmField
    val LIFESTEAL = RangedAttribute("lifesteal", .0, .0, 16384.0).register()
    @JvmField
    val MANASTEAL = RangedAttribute("manasteal", .0, .0, 16384.0).register()
    @JvmField
    val MANA_CONSUMPTION_RATE = RangedAttribute("mana_consumption_rate", 1.0, .0, 5.0).register()
    @JvmField
    val MANA_REGENERATION = RangedAttribute("mana_regeneration", 1.0, .0, 16384.0).register()
    @JvmField
    val MAX_MANA = RangedAttribute("max_mana", 100.0, .0, 16384.0).register()
    @JvmField
    val NEGATIVE_CRITICAL_STRIKE_POWER = RangedAttribute("negative_critical_strike_power", 1.0, .0, 16384.0).register()
    @JvmField
    val NONE_CRITICAL_STRIKE_POWER = RangedAttribute("none_critical_strike_power", 1.0, .0, 16384.0).register()
    @JvmField
    val UNIVERSAL_DEFENSE = RangedAttribute("universal_defense", .0, -16384.0, 16384.0).register()
    @JvmField
    val UNIVERSAL_DEFENSE_PENETRATION = RangedAttribute("universal_defense_penetration", .0, -16384.0, 16384.0).register()
    @JvmField
    val UNIVERSAL_DEFENSE_PENETRATION_RATE = RangedAttribute("universal_defense_penetration_rate", .0, .0, 1.0).register()
    @JvmField
    val UNIVERSAL_MAX_ATTACK_DAMAGE = RangedAttribute("universal_max_attack_damage", "universal_attack_damage", .0, .0, 16384.0).register()
    @JvmField
    val UNIVERSAL_MIN_ATTACK_DAMAGE = RangedAttribute("universal_min_attack_damage", "universal_attack_damage", .0, .0, 16384.0).register()
    //</editor-fold>

    //<editor-fold desc="萌芽属性 (元素)">

    // 跟上面的萌芽属性一样, 只不过不是 Attribute 实例, 而是一个“中间对象”.
    // 客户端需要再指定一次元素才可以获取到最终的 (Element)Attribute 实例.

    @JvmField
    val DEFENSE = registerLazy { element -> ElementAttribute("defense", .0, -16384.0, 16384.0, element) }
    @JvmField
    val DEFENSE_PENETRATION = registerLazy { element -> ElementAttribute("defense_penetration", .0, -16384.0, 16384.0, element) }
    @JvmField
    val DEFENSE_PENETRATION_RATE = registerLazy { element -> ElementAttribute("defense_penetration_rate", .0, .0, 1.0, element) }
    @JvmField
    val MAX_ATTACK_DAMAGE = registerLazy { element -> ElementAttribute("max_attack_damage", "attack_damage", .0, .0, 16384.0, element) }
    @JvmField
    val MIN_ATTACK_DAMAGE = registerLazy { element -> ElementAttribute("min_attack_damage", "attack_damage", .0, .0, 16384.0, element) }
    @JvmField
    val ATTACK_DAMAGE_RATE = registerLazy { element -> ElementAttribute("attack_damage_rate", 1.0, .0, 16384.0, element) }
    @JvmField
    val BLOCKING_DAMAGE_REDUCTION = registerLazy { element -> ElementAttribute("blocking_damage_reduction", .0, .0, 16384.0, element) }
    @JvmField
    val INCOMING_DAMAGE_RATE = registerLazy { element -> ElementAttribute("incoming_damage_rate", 1.0, .0, 16384.0, element) }
    //</editor-fold>

    /**
     * Gets all [Attribute.id] of known attributes.
     */
    val simpleIds: Set<String>
        get() = AttributeProviderInternals.simpleIds + AttributeGetterImpl.simpleIds

    /**
     * Gets all [Attribute.bundleId] of known vanilla-backed attributes.
     */
    val vanillaAttributeNames: Collection<String>
        get() = AttributeNames.vanillaAttributeNames

    /**
     * Gets all [Attribute.bundleId] of known element attributes.
     *
     * 返回的集合中包含两种名字: 一种是不带元素的名字, 一种是带元素的名字.
     * 例如对于 `defense` 这个属性, 会有两种名字包含在返回的集合中:
     * - `defense` (不带元素的名字)
     * - `defense/fire` (带了元素的名字)
     */
    val elementAttributeNames: Collection<String>
        get() = AttributeNames.elementAttributeNames

    /**
     * 初始化 [Attributes].
     */
    @ApiStatus.Internal
    fun init() {
        AttributeGetterImpl.init()
    }

    override fun get(id: String): Attribute? {
        return AttributeProviderInternals.get(id) ?: AttributeGetterImpl.get(id)
    }

    override fun getList(id: String): Collection<Attribute> {
        return AttributeProviderInternals.getList(id)
    }

    override fun isElementalById(id: String): Boolean {
        return id in AttributeGetterImpl.simpleIds
    }

    override fun isElementalByBundleId(bundleId: String): Boolean {
        return bundleId in AttributeGetterImpl.bundleIds
    }

    //////

    private fun <T : Attribute> T.register(): T {
        return AttributeProviderInternals.register(this)
    }

    // "lazy" 意为不立马创建 ElementAttribute, 而仅仅是规定好如何创建 ElementAttribute.
    private fun registerLazy(creator: (RegistryEntry<Element>) -> ElementAttribute): AttributeGetter {
        return AttributeGetterImpl(creator)
    }
}

/**
 * 代表一个用于获取 [ElementAttribute] 实例的“中间对象”.
 *
 * ### 用法
 * 从 [Attributes] 中获取到本类型的实例后, 有一个 [AttributeGetter.of] 函数可用.
 * 使用这个函数指定一个元素, 那么就可以获取到这个元素所对应的 [ElementAttribute] 实例.
 *
 * ### 设计哲学
 * 首先要了解 [ElementAttribute] 跟 [RangedAttribute] 的区别.
 * [RangedAttribute] 的字段都是原始类型, 而 [ElementAttribute] 还带有一个 [Element] 的字段.
 * 也就是说, 想要创建 [ElementAttribute] 必须得先有一个 [Element] 实例, 这种依赖关系使得代码变得复杂.
 * 这就直接导致了, 比如说, 客户端代码想要获取一个 [ElementAttribute], 那么它必须得先有一个 [Element] 实例.
 *
 * 而在很多时候, 客户端代码并不关心 [Element] 实例是什么, 它只是想要指定一个*任意元素*的 [ElementAttribute] 实例.
 * 为了解决这个问题, 我们引入了 [AttributeGetter] 这个“中间对象”.
 * 这个中间对象相当于是一个 `[Element] -> [ElementAttribute]` 的函数, 而不是一个直接的 [ElementAttribute] 实例.
 *
 * 事实证明这种设计极大了降低了代码的重复度, 我们不需要在 [Attributes] 的字段里为每一种可能的元素都声明一个 [ElementAttribute] 实例.
 * 而是只需要创建一个 [AttributeGetter] 实例, 然后通过这个实例来获取 [ElementAttribute] 实例即可.
 * 相当于把创建 [ElementAttribute] 实例的时机给推后了.
 */
interface AttributeGetter {
    /**
     * 获取 [ElementAttribute] 实例.
     * 如果传入的 [id] 无法找到对应的 [Element] 实例, 则返回 `null`.
     * 对于同一个 [id] 的字符串值, 该函数始终会返回同一个 [ElementAttribute] 实例.
     *
     * @param id 元素类型的唯一标识
     */
    fun of(id: String): ElementAttribute?

    /**
     * 获取 [ElementAttribute] 实例.
     * 该函数始终会返回一个 [ElementAttribute] 实例.
     * 对于同一个 [Element] 实例, 该函数始终会返回同一个 [ElementAttribute] 实例.
     *
     * @param element 元素类型
     */
    fun of(element: Element): ElementAttribute

    /**
     * 获取 [ElementAttribute] 实例.
     * 该函数始终会返回一个 [ElementAttribute] 实例.
     * 对于同一个 [Element] 实例, 该函数始终会返回同一个 [ElementAttribute] 实例.
     *
     * @param element 元素类型
     */
    fun of(element: RegistryEntry<Element>): ElementAttribute
}


/* Internals */


private class AttributeGetterImpl(
    private val creator: (RegistryEntry<Element>) -> ElementAttribute,
) : AttributeGetter {

    init {
        registerGetter(this) // 注册到全局对象池, 方便之后遍历
    }

    // element -> element attribute
    private val mappings: ConcurrentHashMap<RegistryEntry<Element>, ElementAttribute> = ConcurrentHashMap()

    override fun of(id: String): ElementAttribute? {
        val elem = BuiltInRegistries.ELEMENT.getEntry(id)
        if (elem == null) {
            return null
        }
        return of(elem)
    }

    override fun of(element: Element): ElementAttribute {
        return of(BuiltInRegistries.ELEMENT.wrapAsEntry(element))
    }

    override fun of(element: RegistryEntry<Element>): ElementAttribute {
        return mappings.computeIfAbsent(element) { k ->
            registerAttribute(creator(k))
        }
    }

    /**
     * 本伴生对象主要充当 [AttributeGetter] 的对象池, 以及存放需要在所有对象之间共享的数据.
     */
    companion object {
        private val INSTANCES: HashSet<AttributeGetter> = HashSet()

        // simple id -> element attribute
        private val SIMPLE_ID_TO_ATTRIBUTE: HashMap<String, ElementAttribute> = HashMap()

        // bundle id -> set <element attribute>
        private val BUNDLE_ID_TO_ATTRIBUTE_SET: HashMap<String, HashSet<ElementAttribute>> = HashMap()

        // 所有已知的 ElementAttribute 的 simpleId
        val simpleIds: Set<String>
            get() = SIMPLE_ID_TO_ATTRIBUTE.keys

        // 所有已知的 ElementAttribute 的 bundleId
        val bundleIds: Set<String>
            get() = BUNDLE_ID_TO_ATTRIBUTE_SET.keys

        @Synchronized
        private fun registerGetter(getter: AttributeGetter): AttributeGetter {
            INSTANCES.add(getter)
            return getter
        }

        @Synchronized
        private fun registerAttribute(attribute: ElementAttribute): ElementAttribute {
            val simpleId = attribute.id
            val bundleId = attribute.bundleId

            SIMPLE_ID_TO_ATTRIBUTE[simpleId] = attribute
            BUNDLE_ID_TO_ATTRIBUTE_SET.computeIfAbsent(bundleId) { _ -> HashSet() }.add(attribute)

            AttributeNames.register(attribute)

            return attribute
        }

        @Synchronized
        fun init() {
            // 初始化每一个 AttributeGetter 的每一种 Element
            for (getter: AttributeGetter in INSTANCES) {
                for (element in BuiltInRegistries.ELEMENT.entrySequence) {
                    getter.of(element)
                }
            }
        }

        fun get(id: String): ElementAttribute? {
            return SIMPLE_ID_TO_ATTRIBUTE[id]
        }

        fun getList(id: String): Collection<ElementAttribute> {
            return BUNDLE_ID_TO_ATTRIBUTE_SET[id] ?: throw IllegalArgumentException("Unknown bundle id: '$id'")
        }
    }
}

// 封装了一些内部状态, 以提供更简洁的接口
private object AttributeProviderInternals {
    // simple id -> attribute
    private val SIMPLE_ID_TO_ATTRIBUTE: HashMap<String, Attribute> = HashMap()

    // bundle id -> attribute set
    private val BUNDLE_ID_TO_ATTRIBUTE_SET: SetMultimap<String, Attribute> = MultimapBuilder.hashKeys().linkedHashSetValues().build()

    // 所有已知的 attribute 的 simple id
    val simpleIds: Set<String>
        get() = SIMPLE_ID_TO_ATTRIBUTE.keys

    @Synchronized
    fun <T : Attribute> register(attribute: T): T {
        SIMPLE_ID_TO_ATTRIBUTE[attribute.id] = attribute
        BUNDLE_ID_TO_ATTRIBUTE_SET.put(attribute.bundleId, attribute)
        AttributeNames.register(attribute)
        return attribute
    }

    fun get(id: String): Attribute? {
        return SIMPLE_ID_TO_ATTRIBUTE[id]
    }

    fun getList(id: String): Collection<Attribute> {
        val bundleIds = BUNDLE_ID_TO_ATTRIBUTE_SET.get(id)
        if (bundleIds.isNotEmpty()) {
            return bundleIds
        }
        return AttributeGetterImpl.getList(id)
    }
}

// 封装了一些内部状态, 以提供更简洁的接口
private object AttributeNames {
    private val vanillaAttributeNameSet: HashSet<String> = HashSet()
    private val elementAttributeNameSet: HashSet<String> = HashSet()

    val vanillaAttributeNames: Collection<String>
        get() = vanillaAttributeNameSet

    val elementAttributeNames: Collection<String>
        get() = elementAttributeNameSet

    @Synchronized
    fun register(attribute: Attribute): Attribute {
        tryRegisterVanillaAttribute(attribute)
        tryRegisterElementAttribute(attribute)
        return attribute
    }

    private fun tryRegisterVanillaAttribute(attribute: Attribute): Attribute {
        if (attribute.vanilla) {
            vanillaAttributeNameSet.add(attribute.bundleId)
        }
        return attribute
    }

    private fun tryRegisterElementAttribute(attribute: Attribute): Attribute {
        if (attribute is ElementAttribute) {
            // 注册两个名字, 一个是不带元素的名字, 一个是带元素的名字.
            // 例如对于 `defense` 这个属性 (元素属性) 会注册两类名字:
            // - defense (不带元素的名字)
            // - defense/fire (带了元素的名字)
            elementAttributeNameSet.add(attribute.bundleId.substringBefore(ElementAttribute.ELEMENT_SEPARATOR))
            elementAttributeNameSet.add(attribute.bundleId)
        }
        return attribute
    }
}