package cc.mewcraft.wakame.enchantment

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.item.ItemSlotRegistry
import cc.mewcraft.wakame.item.VanillaItemSlot

internal object EnchantmentInitializer : Initializable {
    override fun onPostWorld() {
        // 魔咒唯一直接依赖的实例就是 Element 实例,
        // 并且 Element 是在 pre-world 就初始化了,
        // 所以这里在 post-world 遍历所有魔咒就很安全.
        //
        // 但话又说回来, 之所以要关注这个顺序还是因为
        // 不同系统对实例有直接依赖, 而不是用类似
        // “Holder” 的方式去解耦. 等未来把 Registry
        // 这块重构, 依赖顺序问题就不会存在了.

        // 初始化自定义魔咒. 这里必须保证在此之前:
        // 1. NMS 魔咒已全部初始化
        // 2. Element 已全部初始化
        CustomEnchantmentRegistry.initialize()

        // 注册魔咒所用到的所有 ItemSlot
        CustomEnchantmentRegistry.all()
            .flatMap { it.handle.activeSlotGroups }
            .flatMap { VanillaItemSlot.fromEquipmentSlotGroup(it) }
            .distinct()
            .forEach { ItemSlotRegistry.register(it) }
    }
}