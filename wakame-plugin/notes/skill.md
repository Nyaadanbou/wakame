# 技能 (Skill)

## 概念解释

---

### Skill
代表一个技能的类型，包括技能的唯一标识符 Key。用于区分不同的技能以及在配置文件中查找对应的配置。

### ConfiguredSkill
代表一个已经有配置的一个技能，包括技能类型，拥有的条件 SkillConditions 以及每个技能独有的各种配置。
包含一个 Key (下面称之为 ConfiguredKey) 来查找对应的 ConfiguredSkill。

**注意这里的 ConfiguredKey 是由技能配置文件的位置指定的，而不是 Skill 的 Key**，
这也就代表了一个 Skill 可以有多个 ConfiguredSkill。
ConfiguredSkill 会存储在 SkillRegistry 中，通过 ConfiguredKey 来查找对应的 ConfiguredSkill。

### SkillConditions
技能的触发条件，拥有条件和代价两部分。在 ConfiguredSkill 创建之初就会被初始化。
在触发技能时，会检查是否满足条件，如果满足则执行技能，同时执行里面所有的代价，否则不执行

### SkillRegistry
用于存储所有的 ConfiguredSkill，通过 ConfiguredKey 来查找对应的 ConfiguredSkill。

### SkillTrigger
技能触发器，用于在特定的条件下触发技能。触发条件一般存储在需要触发技能的配置中，
比如物品技能配置，铭刻添加技能的配置等。

### ConfiguredSkillWithTrigger
包含一个 ConfiguredSkill 和一个 SkillTrigger。表示技能在可触发技能的配置下的一个实例。

示例配置:
```yaml
  # 核心组：core_f
  core_f:
    filters: [ ]
    selects:
      skill:
        entries:
          - key: skill:blink
            trigger: xyz # 触发条件
            weight: 1
          - key: skill:dash
            trigger: xyz
            weight: 1
```

### SkillMap
每个 User 都有一个 SkillMap，用于存储用户拥有的能够触发的技能。
内部有一个 HashMap， Key 是技能的 Trigger，Value 是 ConfiguredSkill。
这样就可以通过对应的 Trigger 仅用 O(1) 的复杂度来查找对应的 ConfiguredSkill。

---

## 整体思路

---

### 技能的配置生成

1. 代码内指定所有技能的类型，唯一标识符Key，以及每个技能的需要哪些配置来构建。
2. 通过配置文件，创建出一个 ConfiguredSkill 对象。
3. 将 ConfiguredSkill 存储在 SkillRegistry 中，之后程序其他地方可通过 ConfiguredKey 来查找对应的 ConfiguredSkill。

### 技能的触发

1. 当玩家切换手持物品、更改背包内容时，其他地方（物品、铭刻）会将 ConfiguredSkillWithTrigger 存储在 SkillMap 中。
2. 当玩家做出一些满足 Trigger 的操作来试图触发技能时，
通过触发的 Trigger 访问 SkillMap 来找到对应的 ConfiguredSkill，然后尝试执行对应的技能。
3. 如果有触发条件不满足，那么就不会执行技能。
4. 如果有触发条件满足，那么就会执行技能，同时执行里面所有的代价。

#### 这样设计的好处是:
1. 技能的触发只需要根据玩家的行为直接读取 SkillMap，而不需要遍历所有的技能，这样可以减少很多不必要的计算。
2. 技能的配置和触发条件分离，可以更加灵活的配置技能的触发条件。
3. 最关键的是，技能只需访问 SkillMap 来观察有多少技能可以被触发，无需关注技能的增减，这样可以更好的解耦。