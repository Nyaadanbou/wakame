package damage

import cc.mewcraft.wakame.adventure.adventureModule
import cc.mewcraft.wakame.attribute.AttributeMap
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.core.Holder
import cc.mewcraft.wakame.core.registries.KoishRegistries
import cc.mewcraft.wakame.damage.DamageBundle
import cc.mewcraft.wakame.damage.damageBundle
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.element.ElementRegistryConfigStorage
import cc.mewcraft.wakame.registry.registryModule
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import org.slf4j.Logger
import testEnv
import kotlin.test.Test

/**
 * 测试 [DamageBundle] 的 DSL 的正确性.
 */
class DamageBundleDSLTest : KoinTest {
    companion object {
        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            // 配置依赖注入
            startKoin {
                // environment
                modules(
                    testEnv()
                )

                // this module
                modules(
                    // damageModule()
                )

                // dependencies
                modules(
                    adventureModule(),
                    registryModule(),
                )
            }

            // 按依赖顺序, 初始化注册表
            ElementRegistryConfigStorage.init()
        }

        @AfterAll
        @JvmStatic
        fun afterAll() {
            stopKoin()
        }
    }

    private val logger: Logger by inject()

    // 用于测试的 Element 实例
    private lateinit var fireElem: Holder<Element>
    private lateinit var windElem: Holder<Element>

    // 用于测试的 AttributeMap 实例
    private lateinit var attriMap: AttributeMap

    @BeforeEach
    fun beforeEach() {
        fireElem = KoishRegistries.ELEMENT.getOrThrow("fire")
        windElem = KoishRegistries.ELEMENT.getOrThrow("wind")

        // 初始化 AttributeMap 的摹刻, 用于测试
        attriMap = mockk()
        every { attriMap.getValue(match { it.descriptionId.contains("min_attack_damage") }) } returns 8.0
        every { attriMap.getValue(match { it.descriptionId.contains("max_attack_damage") }) } returns 8.0
        every { attriMap.getValue(match { it.descriptionId.contains("attack_damage_rate") }) } returns 0.1
        every { attriMap.getValue(match { it.descriptionId.contains("defense_penetration") }) } returns 1.0
        every { attriMap.getValue(match { it.descriptionId.contains("defense_penetration_rate") }) } returns 0.1
        every { attriMap.getValue(Attributes.UNIVERSAL_MIN_ATTACK_DAMAGE) } returns 2.0
        every { attriMap.getValue(Attributes.UNIVERSAL_MAX_ATTACK_DAMAGE) } returns 2.0
        every { attriMap.getValue(Attributes.UNIVERSAL_DEFENSE_PENETRATION) } returns 1.0
        every { attriMap.getValue(Attributes.UNIVERSAL_DEFENSE_PENETRATION_RATE) } returns 0.1
    }

    private fun printBundle(id: String, bundle: DamageBundle) {
        bundle.packets().forEach { logger.info("($id) $it") }
    }

    // 标准的伤害包构建方式
    @Test
    fun `use case 1`() {
        val bundle1: DamageBundle = damageBundle(attriMap) {
            // 使用 every() 为*每个*已知的元素构建伤害包
            every {
                min { value(Attributes.MIN_ATTACK_DAMAGE) + value(Attributes.UNIVERSAL_MIN_ATTACK_DAMAGE) }
                max { value(Attributes.MAX_ATTACK_DAMAGE) + value(Attributes.UNIVERSAL_MAX_ATTACK_DAMAGE) }
                rate { value(Attributes.ATTACK_DAMAGE_RATE) }
                defensePenetration { value(Attributes.DEFENSE_PENETRATION) + value(Attributes.UNIVERSAL_DEFENSE_PENETRATION) }
                defensePenetrationRate { value(Attributes.DEFENSE_PENETRATION_RATE) + value(Attributes.UNIVERSAL_DEFENSE_PENETRATION_RATE) }
            }

            // 使用 single() 为单个元素构建伤害包, 将覆盖由 every() 统一定义的伤害包
            // 如果“fire”这个字符串 id 对应的元素不存在, 会有警告, 并且该伤害包不会被添加
            single("fire") {
                min { value(Attributes.MIN_ATTACK_DAMAGE) + value(Attributes.UNIVERSAL_MIN_ATTACK_DAMAGE) }
                max { value(Attributes.MAX_ATTACK_DAMAGE) + value(Attributes.UNIVERSAL_MAX_ATTACK_DAMAGE) }
                rate { .0 }
                defensePenetration { .0 }
                defensePenetrationRate { .0 }
            }

            // 如果已经有一个 Element 的实例, 直接传进来也行
            single(windElem) {
                min { value(Attributes.MIN_ATTACK_DAMAGE) + value(Attributes.UNIVERSAL_MIN_ATTACK_DAMAGE) }
                max { value(Attributes.MAX_ATTACK_DAMAGE) + value(Attributes.UNIVERSAL_MAX_ATTACK_DAMAGE) }
                rate { 1.0 }
                defensePenetration { .0 }
                defensePenetrationRate { .0 }
            }
        }

        printBundle("1", bundle1)
    }

    // 如果伤害包的构建方式与“标准”的相同,
    // 则不需要重复书写标准的构建方式,
    // 只需要调用 standard() 即可.
    @Test
    fun `use case 2`() {
        val bundle1: DamageBundle = damageBundle(attriMap) {
            every {
                // 这里的 standard() 意思是使用“标准”计算方式
                min { standard() }
                max { standard() }
                rate { standard() }
                defensePenetration { standard() }
                defensePenetrationRate { standard() }
            }
        }
        val bundle2: DamageBundle = damageBundle(attriMap) {
            every {
                // 或者, 如果每个变量采用“标准”计算方式,
                // 那么直接调用 standard() 即可,
                // 不需要单独用 min, max ...
                standard()
            }
        }

        printBundle("1", bundle1)
        printBundle("2", bundle2)
    }

    // 如果伤害包的构建方式不依赖 AttributeMap,
    // 则 DSL 的初始构造函数可以不传入 AttributeMap.
    // 但这也要求在 DSL 中不能使用任何依赖 AttrMap 的方法, 否则会抛出异常.
    @Test
    fun `use case 3`() {
        val damageValue = 12.0
        val defensePenetration = 3.0
        val defensePenetrationRate = 0.1
        val bundle1: DamageBundle = damageBundle {
            every {
                min(damageValue) // 也可以直接传入一个值, 不一定要用 lambda
                max(damageValue)
                rate(1.0)
                defensePenetration(defensePenetration)
                defensePenetrationRate(defensePenetrationRate)
            }
        }

        printBundle("1", bundle1)
    }

    // 有些伤害包的构造依赖了 AttributeMap,
    // 但伤害包中的某些值是固定的, 没有依赖的.
    // 这种情况可以混合使用两种 DSL.
    @Test
    fun `use case 4`() {
        val bundle1: DamageBundle = damageBundle(attriMap) {
            every {
                // 最小/最大设置为 1.0
                min(1.0)
                max(1.0)
                // 其余的采用“标准”计算方式
                rate { standard() }
                defensePenetration { standard() }
                defensePenetrationRate { standard() }
            }
        }

        printBundle("1", bundle1)
    }

    // 有些伤害包的构造依赖了 AttributeMap,
    // 并且数值是基于“标准”计算方式之上的,
    // 这时可以修饰 standard() 的返回值.
    @Test
    fun `use case 5`() {
        val force = 0.8
        val bundle1: DamageBundle = damageBundle(attriMap) {
            every {
                // 先把所有数值用“标准”方式计算出来,
                // 这样可以省去写 defense 的步骤.
                standard()
                // 然后单独设置 min, max 的值,
                // 做法是修饰“标准”方式的返回值.
                min { force * standard() }
                max { force * standard() }
            }
        }

        printBundle("1", bundle1)
    }

    // 有些伤害包只需要包含默认元素, 并且也不依赖 AttrMap.
    // 这时候可以使用 default() 来直接构建默认元素的伤害包.
    // 但注意, 因为没有 AttrMap, 因此也只能使用直接传值的 DSL.
    @Test
    fun `use case 6`() {
        val damageValue = 1.0
        val bundle1: DamageBundle = damageBundle {
            default {
                min(damageValue)
                max(damageValue)
                rate(1.0)
                defensePenetration(.0)
                defensePenetrationRate(.0)
            }
        }

        printBundle("1", bundle1)
    }
}