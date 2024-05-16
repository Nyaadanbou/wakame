## 架构

- AttributeEventHandler
    - 监听事件，根据事件的种类来调用更新属性的方法
- AttributeAccessor
    - 用于获取实体的 AttributeMap
- AttributeMap
    - 根据属性的类别来存储与获取属性的 AttributeInstance
- AttributeInstance
    - 用于存储与获取**一个**属性的所有的计算操作 (即 AttributeModifier), 并且可以返回计算的最终的值
- AttributeModifier
    - 属性的修饰器，包括数值和对应的操作类型（加算，乘算，最终乘算）

## 关于配置文件的思考

我的架构中，属性的类别是硬编码，但是属性的数值是需要支持通过配置文件定义的。

如果要通过配置文件定义，那么就要保证下面几个类的成员变量要跟配置文件里的一致：

1. `Attribute`
2. `AttributeInstance`
3. `AttributeMap`
4. `AttributeSupplier`
5. `DefaultAttributes`

*包含上面几个类的类不在讨论范围内。

要实现配置文件这个系统，本身又分为两个部分：

- 类在首次实例化时
    - 需要读取配置文件里的值
- 配置文件重载之后
    - 已经存在的对象需要反映最新的配置文件，或者
    - 至少让新创建的对象反映最新的配置文件

然后我们依照上面的思路，分别讨论下各个类的配置文件方案。

#### `Attribute`

`Attribute` 作为“类型”，它的值应该由 provider 来实现配置文件的机制。

这是因为 `Attribute` 作为类型，在很多时候我们是通过引用来比较对象，而非值来比较。

这也意味着我们不能随意的创建一个新的 `Attribute` 然后拿去当类型用，这势必会在比较对象上出现问题。

因此，`Attribute` 的方案就是为所有需要支持配置文件的成员变量套上一个 `Provider`。

这样就能够在不重新实例化 `Attribute` 的基础上，让其数值能够反映最新的配置文件。

#### `AttributeInstance`

这是最为复杂的一个类。

关于 `baseValue` 直接存值还是套个 provider 的思考。

先说结论 - 直接储存值，不储存 provider。

1. 直接储存值 -
   意味着无法直接支持重载，除非特意去 `setBaseValue`，
   或直接重新构建一个新的 `AttributeInstance`
2. 使用 provider -
   意味着支持重载，并且是立马生效的，这种方案只支持
   已有的 `AttributeInstance` 的值自动更新，而无法支持
   新的 `AttributeInstance`

#### `AttributeSupplier`

其本身不需要支持重载，而是让获取到的 `AttributeSupplier` 实例能够反映最新的配置文件即可。

#### `DefaultAttributes`

这是一个单例，里面包含了一个容器，存有每种生物类型所对应的默认属性 (即每种生物类型都有它自己的一个 `AttributeSupplier` 对象)。

因此，要重载这个单例，就是要使得这个容器所返回的对象能够反映最新的配置文件。

这一点通过实现 `Initializable` 使其接入 `Initializer` 的生态即可。
