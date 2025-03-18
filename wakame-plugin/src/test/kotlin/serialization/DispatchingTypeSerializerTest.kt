package serialization

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.registry2.Registry
import cc.mewcraft.wakame.registry2.SimpleRegistry
import cc.mewcraft.wakame.serialization.configurate.TypeSerializers
import cc.mewcraft.wakame.serialization.configurate.typeserializer.valueByNameTypeSerializer
import cc.mewcraft.wakame.util.register
import cc.mewcraft.wakame.util.require
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.spongepowered.configurate.BasicConfigurationNode
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.kotlin.extensions.getList
import org.spongepowered.configurate.kotlin.extensions.typedSet
import org.spongepowered.configurate.kotlin.objectMapperFactory
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.lang.reflect.Type

class DispatchingTypeSerializerTest {

    private fun assertNode(node: ConfigurationNode) {
        val animals = listOf(
            Cat("black"),
            Dog(25)
        )

        // Serialize
        node.node("animals").typedSet<List<Animal>>(animals)

        // Deserialize
        val deserialized = node.node("animals").getList<Animal>(emptyList())

        // Assertions
        assertEquals(animals.size, deserialized.size)
        assertEquals(animals[0], deserialized[0])
        assertEquals(animals[1], deserialized[1])
    }

    @Test
    fun `using object mapper`() {
        val node = BasicConfigurationNode.root(ConfigurationOptions.defaults().serializers { builder ->
            builder.registerAnnotatedObjects(objectMapperFactory())
            builder.register(AnimalType.REGISTRY.valueByNameTypeSerializer())
            builder.register<Animal>(TypeSerializers.dispatching(Animal::type, AnimalType<*>::type))
        })

        assertNode(node)
    }

    @Test
    fun `using type serializer`() {
        val node = BasicConfigurationNode.root(ConfigurationOptions.defaults().serializers { builder ->
            builder.register(AnimalType.REGISTRY.valueByNameTypeSerializer())
            builder.register<Animal>(TypeSerializers.dispatching(Animal::type, AnimalType<*>::type))
            builder.register<Cat>(CatSerializer)
            builder.register<Dog>(DogSerializer)
        })

        assertNode(node)
    }
}

// 定义一个 interface: Animal
interface Animal {
    // 接口必须可以返回一个代表自己类型的实例 (AnimalType)
    val type: AnimalType<*>
}

// 定义一个 class, 用于表示 impl 的 *类型*
class AnimalType<E : Animal>(val type: TypeToken<E>) {
    companion object {
        // 创建一个注册表, 用于存放所有 Animal 的 *类型* (AnimalType)
        val REGISTRY: SimpleRegistry<AnimalType<*>> = Registry.of("animal_type")
    }
}

// 定义一个 impl: Cat
// 尽量使用 @ConfigSerializable 注解, 这样可以省掉 TypeSerializer
@ConfigSerializable
data class Cat(val color: String) : Animal {
    override val type: AnimalType<*> = AnimalTypes.CAT
}

// 定义一个 impl: Dog
// 尽量使用 @ConfigSerializable 注解, 这样可以省掉 TypeSerializer
@ConfigSerializable
data class Dog(val weight: Int) : Animal {
    override val type: AnimalType<*> = AnimalTypes.DOG
}

// 定一个单例, 用于存放所有实现的 *类型*
object AnimalTypes {
    val CAT: AnimalType<Cat> = register("cat") // 这里的字符串 id 将成为配置文件中 `type: ...` 字段的值
    val DOG: AnimalType<Dog> = register("dog")

    // 定义一个方便函数, 减少重复代码
    private inline fun <reified E : Animal> register(id: String): AnimalType<E> {
        return Registry.register(AnimalType.REGISTRY, id, AnimalType(typeTokenOf()))
    }
}

//<editor-fold desc="使用 TypeSerializer 定义每个实现的序列化逻辑">

// 总结来说, 对于序列化的实现, 从下面选择一个方案即可:
// 1) 给实现类标记上 @ConfigSerializable, 然后在 ConfigurationLoader 里注册一个 ObjectMapperFactory
// 1) 给实现类写好 TypeSerializer, 然后分别在 ConfigurationOptions 里注册

// Cat 的 TypeSerializer
object CatSerializer : TypeSerializer<Cat> {
    override fun deserialize(type: Type, node: ConfigurationNode): Cat {
        return Cat(node.node("color").require<String>())
    }

    override fun serialize(type: Type, obj: Cat?, node: ConfigurationNode) {
        node.node("color").set(obj?.color)
    }
}

// Dog 的 TypeSerializer
object DogSerializer : TypeSerializer<Dog> {
    override fun deserialize(type: Type, node: ConfigurationNode): Dog {
        return Dog(node.node("weight").require<Int>())
    }

    override fun serialize(type: Type, obj: Dog?, node: ConfigurationNode) {
        node.node("weight").set(obj?.weight)
    }
}
//</editor-fold>
