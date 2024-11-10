package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.registry.ElementRegistry
import com.google.common.collect.MultimapBuilder
import com.google.common.collect.SetMultimap
import java.util.concurrent.ConcurrentHashMap

/**
 * The container that holds all [Attribute] instances.
 *
 * The attribute instances in this singleton object are primarily served as
 * "lookup index" for other code in this project. The numeric values passing to the
 * attribute constructors are just fallback values when the config provides nothing.
 */
object Attributes : AttributeProvider {
    // 这只是一个特殊值, 供其他系统使用.
    val EMPTY = SimpleAttribute("empty", .0).register()

    //<editor-fold desc="原版属性">

    // 这些属性需要原版属性作为后端才能在游戏中生效.

    val BLOCK_INTERACTION_RANGE = RangedAttribute("block_interaction_range", 4.5, 1.0, 64.0, true).register()
    val ENTITY_INTERACTION_RANGE = RangedAttribute("entity_interaction_range", 3.0, 1.0, 64.0, true).register()
    val SWEEPING_DAMAGE_RATIO = RangedAttribute("sweeping_damage_ratio", 0.1, .0, 1.0, true).register()
    val MAX_ABSORPTION = RangedAttribute("max_absorption", .0, .0, 2048.0, true).register()
    val MAX_HEALTH = RangedAttribute("max_health", 20.0, 1.0, 16384.0, true).register()
    val MINING_EFFICIENCY = RangedAttribute("mining_efficiency", .0, .0, 1024.0, true).register()
    val MOVEMENT_SPEED = RangedAttribute("movement_speed", .0, -1.0, 4.0, true).register()
    val SAFE_FALL_DISTANCE = RangedAttribute("safe_fall_distance", 3.0, -1024.0, 1024.0, true).register()
    val SCALE = RangedAttribute("scale", 1.0, 0.0625, 16.0, true).register()
    val STEP_HEIGHT = RangedAttribute("step_height", 0.6, .0, 10.0, true).register()
    //</editor-fold>

    //<editor-fold desc="萌芽属性">

    // 这些属性需要我们自己实现才能在游戏中生效. 所谓“自己实现”,
    // 就是说, 我们需要通过自定义监听器或调度器等方式来实现它们.

    val ATTACK_EFFECT_CHANCE = RangedAttribute("attack_effect_chance", 0.01, .0, 1.0).register()
    val CRITICAL_STRIKE_CHANCE = RangedAttribute("critical_strike_chance", .0, -1.0, 1.0).register()
    val CRITICAL_STRIKE_POWER = RangedAttribute("critical_strike_power", 1.0, 1.0, 16384.0).register()
    val HAMMER_DAMAGE_RANGE = RangedAttribute("hammer_damage_range", .0, .0, 64.0).register()
    val HAMMER_DAMAGE_RATIO = RangedAttribute("hammer_damage_ratio", .0, .0, 1.0).register()
    val HEALTH_REGENERATION = RangedAttribute("health_regeneration", 1.0, .0, 16384.0).register()
    val LIFESTEAL = RangedAttribute("lifesteal", .0, .0, 16384.0).register()
    val MANASTEAL = RangedAttribute("manasteal", .0, .0, 16384.0).register()
    val MANA_CONSUMPTION_RATE = RangedAttribute("mana_consumption_rate", 1.0, .0, 5.0).register()
    val MANA_REGENERATION = RangedAttribute("mana_regeneration", 1.0, .0, 16384.0).register()
    val MAX_MANA = RangedAttribute("max_mana", 100.0, .0, 16384.0).register()
    val NEGATIVE_CRITICAL_STRIKE_POWER = RangedAttribute("negative_critical_strike_power", 1.0, .0, 1.0).register()
    val UNIVERSAL_DEFENSE = RangedAttribute("universal_defense", .0, -16384.0, 16384.0).register()
    val UNIVERSAL_DEFENSE_PENETRATION = RangedAttribute("universal_defense_penetration", .0, -16384.0, 16384.0).register()
    val UNIVERSAL_DEFENSE_PENETRATION_RATE = RangedAttribute("universal_defense_penetration_rate", .0, .0, 1.0).register()
    val UNIVERSAL_MAX_ATTACK_DAMAGE = RangedAttribute("universal_attack_damage", "universal_max_attack_damage", .0, .0, 16384.0).register()
    val UNIVERSAL_MIN_ATTACK_DAMAGE = RangedAttribute("universal_attack_damage", "universal_min_attack_damage", .0, .0, 16384.0).register()
    //</editor-fold>

    //<editor-fold desc="萌芽属性 (元素)">

    // 跟上面的萌芽属性一样, 只不过不是 Attribute 实例, 而是一个“中间对象”.
    // 客户端需要再指定一次元素才可以获取到最终的 (Element)Attribute 实例.

    val DEFENSE = createGetter { element -> ElementAttribute("defense", .0, -16384.0, 16384.0, element) }
    val DEFENSE_PENETRATION = createGetter { element -> ElementAttribute("defense_penetration", .0, -16384.0, 16384.0, element) }
    val DEFENSE_PENETRATION_RATE = createGetter { element -> ElementAttribute("defense_penetration_rate", .0, .0, 1.0, element) }
    val MAX_ATTACK_DAMAGE = createGetter { element -> ElementAttribute("attack_damage", "max_attack_damage", .0, .0, 16384.0, element) }
    val MIN_ATTACK_DAMAGE = createGetter { element -> ElementAttribute("attack_damage", "min_attack_damage", .0, .0, 16384.0, element) }
    val ATTACK_DAMAGE_RATE = createGetter { element -> ElementAttribute("attack_damage_rate", 1.0, -1.0, 16384.0, element) }
    val INCOMING_DAMAGE_RATE = createGetter { element -> ElementAttribute("incoming_damage_rate", 1.0, -1.0, 16384.0, element) }
    //</editor-fold>

    /**
     * Gets all [Attribute.descriptionId] of known attributes.
     */
    val descriptionIds: Set<String>
        get() = AttributeProviderInternals.descriptionIds + SimpleAttributeGetter.descriptionIds

    /**
     * Gets all [Attribute.compositionId] of known vanilla-backed attributes.
     */
    val vanillaAttributeNames: Collection<String>
        get() = AttributeNamesHolder.vanillaAttributeNames

    /**
     * Gets all [Attribute.compositionId] of known element attributes.
     */
    val elementAttributeNames: Collection<String>
        get() = AttributeNamesHolder.elementAttributeNames

    override fun getSingleton(descriptionId: String): Attribute? {
        return AttributeProviderInternals.getSingleton(descriptionId) ?: SimpleAttributeGetter.getSingleton(descriptionId)
    }

    override fun getComposition(compositionId: String): Collection<Attribute> {
        return AttributeProviderInternals.getComposition(compositionId)
    }

    override fun isElementalByDescriptionId(descriptionId: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun isElementalByCompositionId(compositionId: String): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * 初始化 [Attributes] 的所有数据.
     */
    fun bootstrap() {
        SimpleAttributeGetter.bootstrap()
    }

    //////

    private fun <T : Attribute> T.register(): T {
        return AttributeProviderInternals.register(this)
    }

    private fun createGetter(creator: (Element) -> ElementAttribute): AttributeGetter {
        return SimpleAttributeGetter(creator)
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
}


/* Internals */



private class SimpleAttributeGetter(
    private val creator: (Element) -> ElementAttribute,
) : AttributeGetter {

    init {
        register(this) // 注册到全局对象池, 方便之后遍历
    }

    // element -> element attribute
    private val mappings: ConcurrentHashMap<Element, ElementAttribute> = ConcurrentHashMap()

    override fun of(id: String): ElementAttribute? {
        val elem = ElementRegistry.INSTANCES.find(id)
        if (elem == null) {
            return null
        }
        return of(elem)
    }

    override fun of(element: Element): ElementAttribute {
        return mappings.computeIfAbsent(element) { x: Element ->
            register(creator(x))
        }
    }

    /**
     * 本伴生对象主要充当 [AttributeGetter] 的对象池, 以及存放一些其他需要在所有之中共享的数据.
     */
    companion object Shared {
        private val objectPool: HashSet<AttributeGetter> = HashSet()

        // description id -> element attribute
        private val descriptionId2Attribute: HashMap<String, ElementAttribute> = HashMap()

        // composition id -> set <element attribute>
        private val compositionId2AttributeSet: HashMap<String, HashSet<ElementAttribute>> = HashMap()

        // 所有已知的 ElementAttribute 的 descriptionId
        val descriptionIds: Set<String>
            get() = descriptionId2Attribute.keys

        @Synchronized
        fun bootstrap() {
            // 初始化 ElementAttribute 的每一种 Element
            objectPool.forEach { getter ->
                ElementRegistry.INSTANCES.forEach { (_, element) ->
                    getter.of(element)
                }
            }
        }

        @Synchronized
        fun register(getter: AttributeGetter): AttributeGetter {
            objectPool.add(getter)
            return getter
        }

        @Synchronized
        fun register(attribute: ElementAttribute): ElementAttribute {
            descriptionId2Attribute[attribute.descriptionId] = attribute
            compositionId2AttributeSet.computeIfAbsent(attribute.compositionId) { _ -> HashSet() }.add(attribute)
            AttributeNamesHolder.register(attribute)
            return attribute
        }

        fun getSingleton(descriptionId: String): ElementAttribute? {
            return descriptionId2Attribute[descriptionId]
        }

        fun getComposition(compositionId: String): Collection<ElementAttribute> {
            return compositionId2AttributeSet[compositionId] ?: throw IllegalArgumentException("unknown composition id: '$compositionId'")
        }
    }
}

// 封装了一些内部状态, 以提供更简洁的接口
private object AttributeProviderInternals {
    // description id -> attribute
    private val descriptionId2Attribute: HashMap<String, Attribute> = HashMap()

    // composition id -> attribute set
    private val compositionId2AttributeSet: SetMultimap<String, Attribute> = MultimapBuilder.hashKeys().linkedHashSetValues().build()

    // 所有已知的 attribute 的 description id
    val descriptionIds: Set<String>
        get() = descriptionId2Attribute.keys

    @Synchronized
    fun <T : Attribute> register(attribute: T): T {
        descriptionId2Attribute[attribute.descriptionId] = attribute
        compositionId2AttributeSet.put(attribute.compositionId, attribute)
        AttributeNamesHolder.register(attribute)
        return attribute
    }

    fun getSingleton(descriptionId: String): Attribute? {
        return descriptionId2Attribute[descriptionId]
    }

    fun getComposition(compositionId: String): Collection<Attribute> {
        val compositionIds = compositionId2AttributeSet.get(compositionId)
        if (compositionIds.isNotEmpty()) {
            return compositionIds
        }
        return SimpleAttributeGetter.getComposition(compositionId)
    }
}

// 封装了一些内部状态, 以提供更简洁的接口
private object AttributeNamesHolder {
    private val _vanillaAttributeNames: HashSet<String> = HashSet()
    private val _elementAttributeNames: HashSet<String> = HashSet()

    val vanillaAttributeNames: Collection<String>
        get() = _vanillaAttributeNames

    val elementAttributeNames: Collection<String>
        get() = _elementAttributeNames

    @Synchronized
    fun register(attribute: Attribute) {
        tryRegisterVanillaAttribute(attribute)
        tryRegisterElementAttribute(attribute)
    }

    private fun tryRegisterVanillaAttribute(attribute: Attribute) {
        if (attribute.vanilla) {
            _vanillaAttributeNames.add(attribute.compositionId)
        }
    }

    private fun tryRegisterElementAttribute(attribute: Attribute) {
        if (attribute is ElementAttribute) {
            _elementAttributeNames.add(attribute.compositionId.substringBefore('/'))
            _elementAttributeNames.add(attribute.compositionId)
        }
    }
}