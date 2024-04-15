# 词条栏 (cell)

词条栏可以看作一个容器，里面存放着具体的东西，比如属性和技能。

词条栏这一概念贯穿于多个系统之间，包含但不仅限于物品的NBT结构、物品的生成、配置文件的结构。

从顶层设计来看，我们把词条栏分为两大类：`SchemaCell` 与 `BinaryCell`。这两都继承顶层接口 `Cell`。

## 主要机制

#### 机制 #1

先来说说 NBT 结构。一个词条栏在 NBT 中的结构如下：

- `Compound("<id>")`
  - `Compound("core")`
    - `String("id"): "some_namespace:some_path"`
    - `<... 与该核心相匹配的参数>`
  - `Compound("curse")`
    - `String("id"): "some_namespace:some_path"`
    - `<... 与该诅咒相匹配的参数>`
  - `Compound("reforge")`
    - `<... 与重铸相关的参数>`

其中，

- `Compound("core")` 记录了核心的相关信息
- `Compound("curse")` 记录了诅咒的相关信息
- `Compound("reforge")` 记录了重铸的相关信息

这里不讨论每个部分的具体内容。

但需要注意的是，这里的 `Compound("reforge")` 仅储存重铸的 **历史数据**，而非重铸的 **设置参数**。

例如会储存“重铸成功的次数”，但不会储存“是否允许重铸的参数”。

那么，重铸的设置参数应该在哪呢？——结论是，它不应该储存在词条栏的 NBT 里，而是在词条栏的配置文件里。

这也意味着，

- 策划上：需要保证 NBT 里的词条栏 `id` 与配置文件里的 `id` 是尽可能一致的、可匹配上的。 
- 程序上：需要保证当两者 `id` 匹配不上时，有一个默认的行为可以执行。例如匹配不上直接当成不可重铸/移除NBT里的词条栏等等。

#### 机制 #2

允许在词条栏选择器中指定一个特殊的 **空核心**，与其他普通的核心为并列关系。

拥有 **空核心** 的词条栏叫做 **空词条栏**。

空词条栏是一个实际存在于物品上的词条栏，只不过没有核心，也就不提供任何效果。

空词条栏也意味着玩家可以安装新的核心到词条栏中（这也是重铸系统的一部分，将在重铸系统中详细说明）。

下面是一个词条栏核心的随机池，包含两个核心，其中一个就是 **空核心**，其 `key` 为固定的 `minecraft:empty`.

```yaml
  some_pool:
    - key: attribute:attack_damage # 这是一个普通的属性核心，记录了属性的种类
      element: neutral
      lower: 10
      upper: 15
      weight: 1
    - key: minecraft:empty # 这是一个空核心，代表什么都没有
      weight: 2
```

根据上面的配置文件，也就是说有 `1/3` 的概率抽到 `attack_damage` 这个 **属性核心
**，也有 `2/3` 的概率抽到 `minecraft:empty` 这个 **空核心**。

在物品生成时，如果 **空核心** 被抽中，那么该词条栏里就会有一个空核心，整个词条栏将被视作一个空词条栏。

如果什么都没抽中（也就是既不是空核心，也不是普通核心），那么该词条栏将完全不会出现在物品里。

## 配置文件

词条栏系统的设计可以先从配置文件所支持的格式说起。

下面是一个词条栏的完整规范（基于 YAML 格式）：

```yaml
  <root>:
    create_options:
      core: "core_a"
      curse: "curse_a"
    modify_options:
      reforgeable: true
      accepted_cores:
        - "attribute:critical_strike_chance"
        - "attribute:defense/fire"
        - "attribute:lifesteal"
        - "skill:*"
        - "*:*"
```

可以看到一个词条栏的配置文件被分为了两个部分：`create_options` 和 `modify_options`

下面将逐一解释每一个节点的含义。

#### 节点: `<root>`

* path: `<root>`（代表这个 `key` 不是固定的，而是自行指定的值）
* type: `map`

代表一个 **词条栏** 的节点。

该节点的 `key` 将作为该词条栏的 **唯一标识**，并且在该物品的所有其他词条栏中必须唯一。
该节点的 `key` 最好写英文与数字的组合。中文也支持，但不推荐。

注意！词条栏节点的 `key` 除了唯一性外，还与 **重铸系统** 有密切的交互。
因此 `key` 的取值应该有规律可循，并且需要长期规划。随意修改 `key`

#### 节点: `create_options`

* path: `create_options`
* type: `map`

这里包含词条栏在**生成时**会用到的参数。这里的“生成时”指的是物品刚生成时。

---

* path: `create_options.core`
* type: `string`

该词条栏所使用的核心选择器的唯一标识，必须在根路径下的 `core_groups` 中有定义。

---

* path: `create_options.curse`
* type: `string`

该词条栏所使用的核心选择器的唯一标识，必须在根路径下的 `curse_groups` 中有定义。

#### 节点: `modify_options`

* path: `modify_options`
* type: `map`

这里包含当词条栏被**修改时**会用到的参数。这里的“修改时”一般是指重铸系统（和其子系统，如镶嵌系统）。

---

* path: `modify_options.reforgeable`
* type: `boolean`

如果为 `true`，那么该词条栏将被标记为“可被重铸”；反之亦然。

目前，该选项仅作为标记作用（flag），实际用途等到之后再确定。

---

注意！经讨论，该选项已经移至 **重铸系统** 的配置文件，而不是在物品配置文件里。

* path: `modify_options.accepted_cores`
* type: `list(string)`

该词条栏接受的核心类型。

字符串可以是完整的核心唯一标识，也可以是通配符。例如：

- `attribute:attack_damage` - 表示只接受 `attribute:attack_damage`
- `attribute:*` - 表示接受任何 `attribute` 命名空间下的核心
- `*:*` - 表示接受任何核心

如果一个核心类型被该词条栏所接受，就意味着这个核心可以被安装在该词条栏里。反之，不被接受的核心将无法被安装在该词条栏里。

## 程序架构

从两方面讨论：词条栏的生成 & 词条栏的重铸。

### 关于词条栏的生成

回顾配置文件，我们已经把一个词条栏的配置分为了两大类：`create_options` 和 `modify_options`。

词条栏生成的配置文件不应该关注重铸相关的设置，这部分应该交给专门的“重铸工作台”相关设置负责。

而词条栏重铸的配置文件，将在重铸系统内部安置好，而不是写在物品词条栏里面。详细的重铸架构见专门的部分。

### 关于词条栏的重铸

按照和西木讨论的架构：

物品（的词条栏配置文件）这边，只有一个 `modify_options.reforgeable` 可用，用来直接控制一个词条栏是否可以重铸。

而重铸（的整体架构）那边，将设计成“不同重铸台，都有各自配置文件”的架构。

这是因为我们想推出多个重铸工作台，每个重铸工作台都有不一样的要求和效果。

在这样的架构下，我们可以做出例如让 `sword:demo` 这个物品只能在 `iron` 重铸台下进行“初阶重铸”；但在 `gold` 重铸台下就可以进行“中阶重铸”。

当然，如果策划不想对重铸台做差异化，那就只写一个重铸台的配置文件就行。

总的来说，我们可以创建多个不同的重铸台；每个重铸台可重铸的物品、每个物品在不同重铸台的重铸效果，都可以作差异化。类似泰拉瑞亚里的合成台。

#### 架构1

下面是一个“重铸工作台”的配置文件草案。

- 概述：每个重铸台对应一个配置文件，里面包含了重铸台本身的参数以及所有物品的重铸参数。
- 特点：所有内容都集中放在一个文件里。内容较少时比较好管理，但多起来阅读和定位就比较困难。

```yaml
# 首先是应用于整个工作台的参数
station_option_1: true
station_option_2: 3
station_option_3: "Iron Station"

# 然后是应用于特定物品、特定词条栏的参数
items:
  some_item_namespace:some_item_path:
    some_cell_identifier_1:
      accepted_runes:
        - "iron:*"
        - "gold:sword_plus"
      accepted_cores:
        - "attribute:critical_strike_chance"
        - "attribute:defense/fire"
        - "attribute:lifesteal"
    some_cell_identifier_2:
      accepted_runes:
        - "gold:sword_plus"
      accepted_cores:
        - "skill:*"
    some_cell_identifier_3:
      accepted_runes:
        - "*:*"
      accepted_cores:
        - "*:*"
```

#### 架构2

下面是另一个“重铸工作台”的配置文件草案。

- 概述：每个重铸台对应一个文件夹，文件夹下面有不同的子文件负责提供不同模块的参数。
- 特点：极致的分门别类。内容较少时会显得有点啰嗦，但长期来看可以轻松应付不断膨胀的内容。

一个文件夹下有这些文件：

```
`config.yml`
`items`
├── `<some_item_namespace_1>`
│   ├── `<some_item_path_1>.yml`
│   └── `<some_item_path_2>.yml`
└── `<some_item_namespace_2>`
    ├── `<some_item_path_1>.yml`
    └── `<some_item_path_2>.yml`
```

对每个文件/文件夹的说明：

- `config.yml` 文件，始终命名为 `config.yml`，包含整个工作台的参数。
- `items` 文件夹，始终命名为 `items`，其子文件夹都是均以物品的命名空间命名。
- `<some_item_namespace>` 文件夹，以物品的命名空间命名；包含特定命名空间下，物品的重铸参数。
- `<some_item_path>.yml` 文件，以物品的路径命名；对应一个物品，包含该物品下特定词条栏的重铸参数。

