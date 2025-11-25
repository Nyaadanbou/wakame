# !!! DEPRECATED !!!

## 关于技能的修改

### Ability (技能)

#### 技能配置文件的编写

技能配置文件只能调整有限的技能参数, 无法去调整具体的技能逻辑. 技能的配置文件由技能决定, 但还是有一些通用项, 下面会列举出来.

| Arguments | Description                              | Default |
|-----------|------------------------------------------|---------|
| type      | 使用技能的类型, 详见技能的详细参数                       | -       |
| displays  | 技能的显示文字, 由 `name` 与 `tooltip` 组成         | 空       |
| triggers  | 在技能的执行过程中对 trigger 的操作, 详见下方 trigger 的描述 | 空       |

`ability/melee/dash.yml`

```yaml
displays:
  name: "位移"
  tooltips:
    - "<red>位移！"

# 技能模板的 type
type: blink

#######  以下都是此技能专属配置 ######

distance: 20
teleported_messages:
  - type: sound
    name: "entity.player.teleport"
    source: "master"
    emitter: self
```

#### 给物品添加技能

| Arguments | Description           | Default |
|-----------|-----------------------|---------|
| type      | 核孔的类型, 详见关于核孔的 PR     | -       |
| trigger   | 触发此技能的触发器, 详见 Trigger | -       |
| weight    | 选择中此技能的权重             | -       |

##### 如果是直接在物品内写. `items/test.yml`

```yaml
cells:
  buckets:
    ...
  selectors:
    core_groups:
      core_a:
        filters: [ ]
        selects:
          pool_1:
            entries:
              - type: ability:melee/blink
                trigger: trigger:combo/111 # 一个多键触发器, 详见 trigger
                weight: 5
        default: [ ]
```

##### 如果是在全局的配置内写. `random/items/attack/speed_1/weapon/1_element/common.yml`

```yaml
nodes:
  - type: ability:melee/blink
    trigger: trigger:combo/111 # 一个多键触发器, 详见 abilityTrigger
    weight: 5
```

### Trigger (触发器)

用于触发技能的玩家游戏操作, 分为单键和组合键. `namespace` 是 `trigger`

#### 单键触发器

一般的**用来触发技能的**单键触发器只有两种, `LEFT_CLICK` (`generic/left_click`, `0`) 与 `RIGHT_CLICK` (`generic/right_click`, `1`).
其中 `LEFT_CLICK` 不包括对生物的左键攻击, 同理 `RIGHT_CLICK` 不包括右键对生物交互

还有一类特殊的单键触发器, 它们一般只在技能触发阶段才会奏效, 包括

- `ATTACK` (`generic/attack`) 代表玩家按下了攻击键, 具体上是指左键对生物的攻击, 不包括对空气与方块的交互.
- `JUMP` (`generic/jump`) 代表玩家按下了跳跃键.
- `MOVE` (`generic/move`) 玩家进行了移动操作, 不包括跳跃.
- `SNEAK` (`generic/sneak`) 代表玩家按下了潜行键.

#### 多键触发器

由多个单键触发器组合而来的触发器, 需要玩家在一定时间内按照指定顺序进行触发多个单键触发器才可以触发的触发器.

多键触发器的格式是由单键触发器的 `id` 来组成, 如 `combo/101` 意为"右左右", 即玩家需要按下"右左右"才可以触发这个多键触发器

#### 技能触发阶段对 Trigger 的操作

为了在技能触发的各个阶段对玩家的控制, 在技能的配置中, 我们可以设置技能在玩家到达特定执行阶段时进行的一系列对 Trigger 的操作.

| Arguments | Description             | Default |
|-----------|-------------------------|---------|
| forbidden | 在技能达到某个阶段时, 哪些玩家操作会被阻止  | 空       |
| interrupt | 在技能达到某个阶段时, 哪些玩家操作会打断技能 | 空       |

##### 具体格式

`ability/melee/dash.yml`

```yaml

# 技能模板的 type
type: blink
...

triggers:
  forbidden:
    casting: # 玩家执行技能时的状态, 详见对技能状态的介绍
      - MOVE # 意为在释放技能的时候进行移动会失效
  interrupt:
    casting:
      - JUMP # 意为在释放技能的时候进行跳跃会将技能打断
...

```

### 技能状态 （State)

技能状态意为在玩家执行技能过程中的几个阶段, 目的是为了能更好整理代码以及展示出更好的游戏视觉效果.

#### 技能状态的几个类别

1. `IDLE` 表示玩家正闲置, 这时候的玩家只是应用的技能的效果, 并未真正地触发技能.
2. `CAST_POINT` 表示玩家正在执行技能前摇.
3. `CASTING` 表示玩家正在执行技能本身.
4. `BACKSWING` 表示玩家正在执行技能后摇.