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

# 目标

给定一个 lore line 的 key，能够返回一个 int - 越小则表示该 key 越靠前
复杂度必须为 O(1)

# key 的所有种类

- 技能
    - skill:技能名称
- 属性
    - attribute:属性
    - attribute:属性:operation
    - attribute:属性:operation:element
- 元数据
    - meta:元数据名称
- 固定内容
    - 固定内容本身
    - 空行（只要一个数字来表示）
```
val finalMap

for each line X in config: {
  
  val class = 根据X创建class
  val keyList = get list from class
  
  for index, newKey in keyList with index{
    // 有加失败(例如不该重复的内容出现重复的了)的就throw
    finalmap[newKey] = class.index /* 行内注释 inline comment */
        + index // 从0开始
  }
}

 
  interface class {
    val key: String
    val index: Int
    
    /**
     * 生成根据配置文件内key的真实key, 不会返回空集合
     */
    fun getKeyList(): List<String>
  }
    
  attribute class impl class {
    构造函数：
    override key
    override index
    
    成员变量；
    规则： List
    
    fun getKeyList(){
    创建属于这个属性的下面三种所有参数：
      - attribute:属性
      - attribute:属性:operation
      - attribute:属性:operation:element
    }
  }
  
  fixed line class impl class {
    构造函数：
    override key
    override index
  
    fun getKeyList {
      如果是空行:
        就是返回 this.index
      如果是固定内容：
        返回去掉特殊符号的 key
    }
  }
  
  meta class impl class {
    构造函数：
    override key
    override index
    
    fun getKeyList(){
      判断meta内有没有这个名字的,有就返回single List，没有抛异常
    }
  }
  
  skill class impl class {
    构造函数：
    override key
    override index
    
    fun getKeyList(){
      判断技能内内有没有这个名字的,有就返回single List，没有抛异常
    }
  }
```