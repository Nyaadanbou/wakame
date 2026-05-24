# 范式 C: 手动加载配置文件夹

当需要遍历一个文件夹加载多个配置文件、每个文件对应一个注册表条目时，不适合用 Provider 响应式范式，而是直接使用底层 Configurate API。

## 使用 yamlLoader DSL

```kotlin
import cc.mewcraft.lazyconfig.configurate.register
import cc.mewcraft.lazyconfig.configurate.require
import cc.mewcraft.wakame.util.configurate.yamlLoader

val loader = yamlLoader {
    withDefaults()                          // 应用项目默认设置 (缩进、序列化器等)
    serializers {
        registerAll(MY_CUSTOM_SERIALIZERS)  // 注册自定义序列化器 (如需要)
    }
}
```

## 遍历文件夹加载

```kotlin
// 获取存放配置文件的目录
val dataDir = getFileInConfigDirectory("item/")

dataDir.walk().drop(1)                      // drop(1) 跳过根目录自身
    .filter { it.isFile && it.extension == "yml" }
    .forEach { file ->
        try {
            // 从文件内容构建节点树
            val rootNode = loader.buildAndLoadString(file.readText())

            // 从文件路径推导 ID
            val id = file.relativeTo(dataDir)
                .invariantSeparatorsPath
                .substringBeforeLast('.')

            // 反序列化各部分
            val properties = rootNode.require<ItemPropContainer>()
            val behaviors = rootNode.require<ItemBehaviorContainer>()

            // 注册到注册表
            registry.add(id, MyItem(id, properties, behaviors))
        } catch (e: Exception) {
            LOGGER.error("Failed to load config from file: {}", file.path)
        }
    }
```

## `require` 扩展函数

```kotlin
import cc.mewcraft.lazyconfig.configurate.require

// 从节点反序列化，缺失时抛 NoSuchElementException
val value: MyType = node.require<MyType>()
```

## 典型场景

- 物品注册表加载 (`CustomItemRegistryLoader`)
- 战利品表加载
- 技能配置加载
- 任何 "一个文件夹 = 一组注册表条目" 的场景
