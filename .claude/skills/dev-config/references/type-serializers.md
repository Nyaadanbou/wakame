# 自定义 TypeSerializer

当 `@ConfigSerializable` 不够用（如需要多态反序列化、自定义格式解析）时，编写自定义序列化器。

## SimpleSerializer (只需反序列化)

```kotlin
import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import org.spongepowered.configurate.ConfigurationNode

// 大多数场景只需要反序列化，不需要序列化回 YAML
val MySerializer = SimpleSerializer<MyType> { type, node ->
    val name = node.node("name").string ?: return@SimpleSerializer null
    val value = node.node("value").getInt(0)
    MyType(name, value)
}
```

## DispatchingSerializer (多态类型)

```kotlin
import cc.mewcraft.lazyconfig.configurate.serializer.DispatchingSerializer

// 根据 "type" 字段分发到不同的子类型
val MyPolymorphicSerializer = DispatchingSerializer.createPartial<String, BaseType>(
    mapOf(
        "type_a" to TypeA::class,
        "type_b" to TypeB::class,
    )
)
```

对应 YAML:

```yaml
my_entry:
  type: type_a
  # TypeA 的其他字段...
```

## 注册序列化器

**方式一: 通过 ConfigAccess 注册（对整个命名空间生效）:**

```kotlin
import cc.mewcraft.lazyconfig.access.ConfigAccess
import cc.mewcraft.lazyconfig.access.registerSerializer

ConfigAccess.registerSerializer("koish", MySerializer)
```

**方式二: 在 yamlLoader 中注册（仅对该 loader 生效）:**

```kotlin
val loader = yamlLoader {
    withDefaults()
    serializers {
        register(MySerializer)
        register<BaseType>(MyPolymorphicSerializer)
    }
}
```

**方式三: 在 `ItemPropTypes` / `ItemBehaviorTypes` 中注册:**

```kotlin
@JvmField
val MY_PROP: ItemPropType<MyData> = typeOf("my_prop") {
    serializers {
        register(MyCustomSerializer())
    }
}
```
