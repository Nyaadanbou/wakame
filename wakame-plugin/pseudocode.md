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
  - ability:技能名称
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
  
  ability class impl class {
    构造函数：
    override key
    override index
    
    fun getKeyList(){
      判断技能内内有没有这个名字的,有就返回single List，没有抛异常
    }
  }
```

# 目标
根据一些事件的触发为实体 Entity 添加 Attribute，并将 Attribute 的具体效果应用到游戏内实体上

## 事件的种类
- PlayerJoinEvent （初始化玩家的属性）
- PlayerQuitEvent （仅作移除）
- PlayerArmorChangeEvent （更新玩家的属性）
- PlayerItemHeldEvent （更新玩家的属性）
- TODO: 生物的事件

## 一些类的定义
- AttributeHandler
  - 监听事件，根据事件的种类来调用更新属性的方法
- AttributeAccessor
  - 用于获取实体的 AttributeMap
  - 提供修改实体属性的方法，修改完成后触发一个事件，表示
- AttributeMap
  - 根据属性的类别来存储与获取属性的 AttributeInstance
- AttributeInstance
  - 一个属性的类别
  - 用于存储与获取**一个**属性的所有的计算操作(即 AttributeModifier), 并且可以返回计算的最终的值
- AttributeModifier
  - 一个UUID，表示提供此计算操作的来源的唯一标识 
  - 一个Double，表示提供的计算操作的值
  - 一个属性的计算操作，包括了操作的类型（加算，乘算，最终乘算）

## 伪代码
```kotlin
interface AttributeAccessor<T : Entity> {
    fun getAttributeMap(): AttributeMap
    
    fun 各种修改 AttributeMap 的方法() {
        修改完成
    
        触发修改事件
    }
}

class 修改事件(
  val uuid: 触发此事件的生物UUID,
  val type: 触发此事件的生物类型,
): Cancelable

/**
 * 将代码内的属性应用到游戏世界内的实体上
 */
abstract class AttributeApplier<T: Entity> {
    // 通用生物的属性
    fun applyAttribute(attributeMap: AttributeMap) {
        // 根据属性的类别来获取 AttributeInstance
        val instance = attributeMap[属性的类别]
        
      instance.获得计算结果()
      
      根据计算结果来修改实体的属性
        
    }
}

class PlayerAttributeApplier : AttributeApplier<Player> {
    // 重写 applyAttribute 方法
}


class AttributeApplierListener {
    val registery :可以通过生物类型获得不同的 AttributeApplier
    
    @EventHandler(ingoreCancelled = true, priority = EventPriority.LOWEST)
    fun 监听事件(e) {
        根据生物类型来调用
        registery[e.type].applyAttribute(e.attributeMap)
    }
}


```