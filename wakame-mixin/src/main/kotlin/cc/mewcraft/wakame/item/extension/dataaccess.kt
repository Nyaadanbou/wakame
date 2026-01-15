package cc.mewcraft.wakame.item.extension

import cc.mewcraft.lazyconfig.MAIN_CONFIG
import cc.mewcraft.lazyconfig.access.optionalEntry
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.data.ItemDataTypes
import cc.mewcraft.wakame.item.data.impl.Core
import cc.mewcraft.wakame.item.data.impl.CoreContainer
import cc.mewcraft.wakame.item.data.impl.ItemLevel
import cc.mewcraft.wakame.item.getData
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.registry.entry.RegistryEntry
import cc.mewcraft.wakame.util.MojangStack
import cc.mewcraft.wakame.util.item.toNMS
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.commons.provider.orElse

val ITEM_LEVEL_PROVIDER: DataSource by MAIN_CONFIG.optionalEntry<DataSource>("item_level_provider").orElse(DataSource.DATA)
val ITEM_RARITY_PROVIDER: DataSource by MAIN_CONFIG.optionalEntry<DataSource>("item_rarity_provider").orElse(DataSource.DATA)
val ITEM_ELEMENT_PROVIDER: DataSource by MAIN_CONFIG.optionalEntry<DataSource>("item_element_provider").orElse(DataSource.TYPE)
val ITEM_KIZAMI_PROVIDER: DataSource by MAIN_CONFIG.optionalEntry<DataSource>("item_kizami_provider").orElse(DataSource.DATA)
val ITEM_CORE_PROVIDER: DataSource by MAIN_CONFIG.optionalEntry<DataSource>("item_core_provider").orElse(DataSource.DATA)
val ITEM_CORE_CONTAINER_PROVIDER: DataSource by MAIN_CONFIG.optionalEntry<DataSource>("item_core_container_provider").orElse(DataSource.DATA)

enum class DataSource {
    /**
     * 从物品堆叠获取数据.
     */
    DATA,
    /**
     * 从物品类型获取数据.
     */
    TYPE
}

val ItemStack.level: ItemLevel?
    get() = toNMS().level

val ItemStack.rarity2: RegistryEntry<Rarity>?
    get() = toNMS().rarity2

val ItemStack.elements: Set<RegistryEntry<Element>>
    get() = toNMS().elements

val ItemStack.kizamiz: Set<RegistryEntry<Kizami>>
    get() = toNMS().kizamiz

val ItemStack.core: Core?
    get() = toNMS().core

val ItemStack.coreContainer: CoreContainer?
    get() = toNMS().coreContainer

val MojangStack.level: ItemLevel?
    get() = when (ITEM_LEVEL_PROVIDER) {
        DataSource.DATA -> getData(ItemDataTypes.LEVEL)
        DataSource.TYPE -> getProp(ItemPropTypes.LEVEL)
    }

val MojangStack.rarity2: RegistryEntry<Rarity>?
    get() = when (ITEM_RARITY_PROVIDER) {
        DataSource.DATA -> getData(ItemDataTypes.RARITY)
        DataSource.TYPE -> getProp(ItemPropTypes.RARITY)
    }

val MojangStack.elements: Set<RegistryEntry<Element>>
    get() = when (ITEM_ELEMENT_PROVIDER) {
        DataSource.DATA -> getData(ItemDataTypes.ELEMENT) ?: emptySet()
        DataSource.TYPE -> getProp(ItemPropTypes.ELEMENT) ?: emptySet()
    }

val MojangStack.kizamiz: Set<RegistryEntry<Kizami>>
    get() = when (ITEM_KIZAMI_PROVIDER) {
        DataSource.DATA -> getData(ItemDataTypes.KIZAMI) ?: emptySet()
        DataSource.TYPE -> getProp(ItemPropTypes.KIZAMI) ?: emptySet()
    }

val MojangStack.core: Core?
    get() = when (ITEM_CORE_PROVIDER) {
        DataSource.DATA -> getData(ItemDataTypes.CORE)
        DataSource.TYPE -> getProp(ItemPropTypes.CORE)
    }

val MojangStack.coreContainer: CoreContainer?
    get() = when (ITEM_CORE_CONTAINER_PROVIDER) {
        DataSource.DATA -> getData(ItemDataTypes.CORE_CONTAINER)
        DataSource.TYPE -> getProp(ItemPropTypes.CORE_CONTAINER)
    }
