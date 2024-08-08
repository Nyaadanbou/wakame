package cc.mewcraft.wakame.convertor

import cc.mewcraft.wakame.item.NekoItem
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.realize
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.registry.ItemRegistry
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.*

/**
 * 配置文件中代表物品类型的key.
 * 这种key与配置文件中物品的写法对应.
 * 但其本身不保证能够realize成功.
 */
sealed interface ConfigItemKey {
    val namespace: String
    val value: String

    fun realize(): ItemStack
}

/**
 * 原版物品的配置文件key.
 * minecraft:heart_of_the_sea
 * minecraft:enchanted_golden_apple
 */
private class VanillaConfigItemKey(
    override val value: String
) : ConfigItemKey {
    override val namespace = "minecraft"

    /**
     * 将 [Material] 转化成 [ConfigItemKey]
     */
    constructor(material: Material) : this(material.name.lowercase(Locale.getDefault()))

    override fun realize(): ItemStack {
        val material = Material.getMaterial(value.uppercase(Locale.getDefault()))
        material ?: throw IllegalArgumentException("Unknown vanilla item: '$value'")
        return ItemStack(material)
    }

}

/**
 * 萌芽物品的配置文件key.
 * wakame:material/raw_tin
 * wakame:material/raw_bronze
 */
private class WakameConfigItemKey(
    override val value: String
) : ConfigItemKey {
    override val namespace = "wakame"

    /**
     * 将 [NekoStack] 转化成 [ConfigItemKey]
     */
    constructor(nekoStack: NekoStack) : this(nekoStack.key.namespace() + "/" + nekoStack.key.value())

    /**
     * 将 [NekoItem] 转化成 [ConfigItemKey]
     */
    constructor(nekoItem: NekoItem) : this(nekoItem.key.namespace() + "/" + nekoItem.key.value())


    val key = Key.key(value.replaceFirst('/', ':'))
    override fun realize(): ItemStack {
        val nekoItem = ItemRegistry.CUSTOM.find(key)
        val nekoStack = nekoItem?.realize()
        val itemStack = nekoStack?.itemStack
        itemStack ?: throw IllegalArgumentException("Unknown wakame item: '$key'")
        return itemStack
    }

}

/**
 * 直接从一个游戏中的 [ItemStack] 获取 [ConfigItemKey]
 */
fun ItemStack.getConfigKey(): ConfigItemKey {
    val nekoStack = this.tryNekoStack
    return if (nekoStack != null) {
        WakameConfigItemKey(nekoStack)
    } else {
        VanillaConfigItemKey(this.type)
    }
}

/**
 * 将配置文件读取的adventure [Key] 转化为 [ConfigItemKey]
 * 仅做格式上的转化
 * 不会检查是否可以正常realize
 */
fun Key.convertToConfigKey(): ConfigItemKey {
    return when (this.namespace()) {
        "minecraft" -> {
            VanillaConfigItemKey(this.value())
        }

        "wakame" -> {
            WakameConfigItemKey(this.value())
        }

        else -> {
            throw IllegalArgumentException("Unknown namespace: '${this.namespace()}'")
        }
    }
}
