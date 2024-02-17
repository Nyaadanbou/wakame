```kotlin
// Key - 根据某种规则生成的唯一标识
// Value - 生成出来的 Component
val lore = mapOf<String, Component>()

// Key - 在配置文件内的键（需要通过加载生成些额外的）
// Value - 该键对应的优先级
val primary = mutableMapOf<String, Int>()

val sortedLore = lore.values.sortWith { o1, o2 ->
    val i1 = primary[o1] // FIXME check nullability
    val i2 = primary[o2]

    if (i1 > i2) {
        1
    } else if (i1 < i2) {
        -1
    } else {
        0
    }
}

return sortedLore
```