// 文件说明:
// 提供快速读写 NekoStack 数据的扩展函数

package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.CustomModelData
import cc.mewcraft.wakame.item.components.FireResistant
import cc.mewcraft.wakame.item.components.ItemCells
import cc.mewcraft.wakame.item.components.ItemElements
import cc.mewcraft.wakame.item.components.ItemKizamiz
import cc.mewcraft.wakame.item.components.ItemLevel
import cc.mewcraft.wakame.item.components.ItemRarity
import cc.mewcraft.wakame.item.components.PortableCore
import cc.mewcraft.wakame.item.components.ReforgeHistory
import cc.mewcraft.wakame.item.components.StandaloneCell
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.registry.RarityRegistry
import net.kyori.adventure.text.Component
import kotlin.reflect.KProperty

var NekoStack.customModelData: Int? by mapped(ItemComponentTypes.CUSTOM_MODEL_DATA, ::CustomModelData, CustomModelData::data)

var NekoStack.customName: Component? by direct(ItemComponentTypes.CUSTOM_NAME)

var NekoStack.itemName: Component? by direct(ItemComponentTypes.ITEM_NAME)

var NekoStack.fireResistant: Boolean by mapped(ItemComponentTypes.FIRE_RESISTANT, { false }, { FireResistant.instance() }, { true })

var NekoStack.level: Int by mapped(ItemComponentTypes.LEVEL, ItemLevel::minimumLevel, ::ItemLevel, ItemLevel::level)

var NekoStack.rarity: Rarity by mapped(ItemComponentTypes.RARITY, RarityRegistry::DEFAULT, ::ItemRarity, ItemRarity::rarity)

var NekoStack.elements: Set<Element> by mapped(ItemComponentTypes.ELEMENTS, ::emptySet, ::ItemElements, ItemElements::elements)

var NekoStack.kizamiz: Set<Kizami> by mapped(ItemComponentTypes.KIZAMIZ, ::emptySet, ::ItemKizamiz, ItemKizamiz::kizamiz)

var NekoStack.reforgeHistory: ReforgeHistory by direct(ItemComponentTypes.REFORGE_HISTORY, ReforgeHistory.ZERO)

var NekoStack.cells: ItemCells? by direct(ItemComponentTypes.CELLS)

var NekoStack.standaloneCell: StandaloneCell? by direct(ItemComponentTypes.STANDALONE_CELL)

var NekoStack.portableCore: PortableCore? by direct(ItemComponentTypes.PORTABLE_CORE)


/* private */


private fun <T> direct(type: ItemComponentType<T>): SimpleComponentDelegate<T> {
    return SimpleComponentDelegate(type)
}

private fun <T> direct(type: ItemComponentType<T>, def: T): SimpleWithDefaultComponentDelegate<T> {
    return SimpleWithDefaultComponentDelegate(type, def)
}

private class SimpleComponentDelegate<T>(
    private val type: ItemComponentType<T>,
) {
    operator fun getValue(thisRef: NekoStack, property: KProperty<*>): T? {
        return thisRef.components.get(type)
    }

    operator fun setValue(thisRef: NekoStack, property: KProperty<*>, value: T?) {
        if (value == null) {
            thisRef.components.unset(type)
        } else {
            thisRef.components.set(type, value)
        }
    }
}

private class SimpleWithDefaultComponentDelegate<T>(
    private val type: ItemComponentType<T>,
    private val def: T,
) {
    operator fun getValue(thisRef: NekoStack, property: KProperty<*>): T {
        return thisRef.components.getOrDefault(type, def)
    }

    operator fun setValue(thisRef: NekoStack, property: KProperty<*>, value: T?) {
        if (value == null) {
            thisRef.components.unset(type)
        } else {
            thisRef.components.set(type, value)
        }
    }
}

private fun <T, R> mapped(type: ItemComponentType<T>, box: (R) -> T, unbox: (T) -> R): MappedComponentDelegate<T, R> {
    return MappedComponentDelegate(type, box, unbox)
}

private fun <T, R> mapped(type: ItemComponentType<T>, def: () -> R, box: (R) -> T, unbox: (T) -> R): MappedWithDefaultComponentDelegate<T, R> {
    return MappedWithDefaultComponentDelegate(type, def, box, unbox)
}

private class MappedComponentDelegate<T, R>(
    private val type: ItemComponentType<T>,
    private val box: (R) -> T,
    private val unbox: (T) -> R,
) {
    operator fun getValue(thisRef: NekoStack, property: KProperty<*>): R? {
        return thisRef.components.get(type)?.let(unbox)
    }

    operator fun setValue(thisRef: NekoStack, property: KProperty<*>, value: R?) {
        if (value == null) {
            thisRef.components.unset(type)
        } else {
            thisRef.components.set(type, box(value))
        }
    }
}

private class MappedWithDefaultComponentDelegate<T, R>(
    private val type: ItemComponentType<T>,
    private val def: () -> R,
    private val box: (R) -> T,
    private val unbox: (T) -> R,
) {
    operator fun getValue(thisRef: NekoStack, property: KProperty<*>): R {
        return thisRef.components.getOrDefault(type, box(def())).let(unbox)
    }

    operator fun setValue(thisRef: NekoStack, property: KProperty<*>, value: R?) {
        if (value == null) {
            thisRef.components.unset(type)
        } else {
            thisRef.components.set(type, box(value))
        }
    }
}
