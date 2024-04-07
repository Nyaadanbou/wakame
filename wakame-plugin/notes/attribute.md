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