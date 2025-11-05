# koish-hook-auraskills

为了使该钩子新增的 Stat/Trait/Ability 可以完全正常运行, 需要同时修改 AuraSkills 相关的语言文件.

### 文件 `AuraSkills/messages/messages_(lang-code).yml`

```yaml
traits:
  # ... 已有内容
  attack_damage:
    name: 攻击伤害
  
  # ... 新增内容
  koish/attribute_attack_knockback:
    name: 攻击击退
  koish/attribute_block_interaction_range:
    name: 方块交互范围
  koish/attribute_entity_interaction_range:
    name: 实体交互范围
  koish/attribute_knockback_resistance:
    name: 击退抗性
  koish/attribute_max_absorption:
    name: 最大吸收生命值
  koish/attribute_max_health:
    name: 最大生命值
  koish/attribute_mining_efficiency:
    name: 挖掘效率
  koish/attribute_movement_speed:
    name: 移动速度
  koish/attribute_safe_fall_distance:
    name: 安全跌落距离
  koish/attribute_scale:
    name: 实体大小
  koish/attribute_step_height:
    name: 台阶高度
  koish/attribute_sweeping_damage_ratio:
    name: 横扫伤害比率
  koish/attribute_water_movement_efficiency:
    name: 水中移动效率
  koish/attribute_attack_effect_chance:
    name: 攻击效果几率
  koish/attribute_critical_strike_chance:
    name: 暴击几率
  koish/attribute_critical_strike_power:
    name: 暴击伤害
  koish/attribute_damage_rate_by_untargeted:
    name: 未被锁定伤害率
  koish/attribute_hammer_damage_range:
    name: 锤击伤害范围
  koish/attribute_hammer_damage_ratio:
    name: 锤击伤害比率
  koish/attribute_health_regeneration:
    name: 生命值再生
  koish/attribute_lifesteal:
    name: 生命偷取
  koish/attribute_negative_critical_strike_power:
    name: 负暴击伤害
  koish/attribute_none_critical_strike_power:
    name: 无暴击伤害
  koish/attribute_universal_defense:
    name: 全元素防御力
  koish/attribute_universal_defense_penetration:
    name: 全元素防御穿透
  koish/attribute_universal_defense_penetration_rate:
    name: 全元素防御穿透率
  koish/attribute_universal_max_attack_damage:
    name: 全元素大攻击力
  koish/attribute_universal_min_attack_damage:
    name: 全元素最小攻击力
```

### 文件 `AuraSkills/menus/stat_info.yml`

```yaml
templates:
  trait:
    contexts:
      # ... 已有内容
      attack_damage:
        material: red_dye
      
      # ... 新增内容
      koish/attribute_attack_knockback:
        material: gray_dye
      koish/attribute_block_interaction_range:
        material: gray_dye
      koish/attribute_entity_interaction_range:
        material: gray_dye
      koish/attribute_knockback_resistance:
        material: gray_dye
      koish/attribute_max_absorption:
        material: gray_dye
      koish/attribute_max_health:
        material: gray_dye
      koish/attribute_mining_efficiency:
        material: gray_dye
      koish/attribute_movement_speed:
        material: gray_dye
      koish/attribute_safe_fall_distance:
        material: gray_dye
      koish/attribute_scale:
        material: gray_dye
      koish/attribute_step_height:
        material: gray_dye
      koish/attribute_sweeping_damage_ratio:
        material: gray_dye
      koish/attribute_water_movement_efficiency:
        material: gray_dye
      koish/attribute_attack_effect_chance:
        material: gray_dye
      koish/attribute_critical_strike_chance:
        material: gray_dye
      koish/attribute_critical_strike_power:
        material: gray_dye
      koish/attribute_damage_rate_by_untargeted:
        material: gray_dye
      koish/attribute_hammer_damage_range:
        material: gray_dye
      koish/attribute_hammer_damage_ratio:
        material: gray_dye
      koish/attribute_health_regeneration:
        material: gray_dye
      koish/attribute_lifesteal:
        material: gray_dye
      koish/attribute_negative_critical_strike_power:
        material: gray_dye
      koish/attribute_none_critical_strike_power:
        material: gray_dye
      koish/attribute_universal_defense:
        material: gray_dye
      koish/attribute_universal_defense_penetration:
        material: gray_dye
      koish/attribute_universal_defense_penetration_rate:
        material: gray_dye
      koish/attribute_universal_max_attack_damage:
        material: gray_dye
      koish/attribute_universal_min_attack_damage:
        material: gray_dye
```