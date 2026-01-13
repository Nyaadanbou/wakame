package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.catalog.item.CatalogItemCategoryRegistryLoader
import cc.mewcraft.wakame.catalog.item.CatalogItemLootTableRecipeRegistryLoader
import cc.mewcraft.wakame.catalog.item.CatalogItemMenuSettings
import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.KoishCommandFactory
import cc.mewcraft.wakame.command.koishHandler
import cc.mewcraft.wakame.config.ConfigAccess
import cc.mewcraft.wakame.craftingstation.CraftingStationRecipeRegistry
import cc.mewcraft.wakame.craftingstation.CraftingStationRegistry
import cc.mewcraft.wakame.damage.mapping.AttackCharacteristicDamageMappings
import cc.mewcraft.wakame.damage.mapping.DamageTypeDamageMappings
import cc.mewcraft.wakame.damage.mapping.NullCausingDamageMappings
import cc.mewcraft.wakame.damage.mapping.PlayerAdhocDamageMappings
import cc.mewcraft.wakame.element.ElementRegistryLoader
import cc.mewcraft.wakame.entity.attribute.AttributeSupplierRegistryLoader
import cc.mewcraft.wakame.entity.attribute.ImgAttributeMapRegistryLoader
import cc.mewcraft.wakame.entity.typeref.EntityRefRegistryLoader
import cc.mewcraft.wakame.gui.BasicGuiInitializer
import cc.mewcraft.wakame.init.RecipeInitializer
import cc.mewcraft.wakame.item.CustomItemRegistryLoader
import cc.mewcraft.wakame.item.ItemProxyRegistryLoader
import cc.mewcraft.wakame.item.ItemTagManager
import cc.mewcraft.wakame.item.display.implementation.crafting_station.CraftingStationItemRenderer
import cc.mewcraft.wakame.item.display.implementation.merging_table.MergingTableItemRenderer
import cc.mewcraft.wakame.item.display.implementation.repairing_table.RepairingTableItemRenderer
import cc.mewcraft.wakame.item.display.implementation.simple.SimpleItemRenderer
import cc.mewcraft.wakame.item.display.implementation.standard.StandardItemRenderer
import cc.mewcraft.wakame.kizami.KizamiRegistryLoader
import cc.mewcraft.wakame.lang.GlobalTranslations
import cc.mewcraft.wakame.loot.LootTableRegistryLoader
import cc.mewcraft.wakame.pack.ResourcePackLifecycle
import cc.mewcraft.wakame.rarity.RarityRegistryLoader
import cc.mewcraft.wakame.reforge.blacksmith.BlacksmithStationRegistry
import cc.mewcraft.wakame.reforge.merge.MergingTableRegistry
import cc.mewcraft.wakame.reforge.mod.ModdingTableRegistry
import cc.mewcraft.wakame.reforge.recycle.RecyclingStationRegistry
import cc.mewcraft.wakame.reforge.repair.RepairingTableRegistry
import cc.mewcraft.wakame.reforge.reroll.RerollingTableRegistry
import cc.mewcraft.wakame.util.coroutine.minecraft
import kotlinx.coroutines.Dispatchers
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.paper.util.sender.Source
import org.incendo.cloud.parser.standard.EnumParser
import kotlin.system.measureTimeMillis


// TODO 在 #439 后续的 PR 中慢慢完善此指令

internal object PluginCommand : KoishCommandFactory<Source> {

    override fun KoishCommandFactory.Builder<Source>.createCommands() {
        // <root> reload <type>
        // Reload specific plugin configs
        buildAndAdd {
            permission(CommandPermissions.PLUGIN)
            literal("reload")
            required("type", EnumParser.enumParser(ReloadType::class.java))
            koishHandler(context = Dispatchers.minecraft, handler = ::handleReload)
        }
    }

    private fun handleReload(context: CommandContext<Source>) {
        val sender = context.sender().source()
        val reloadType = context.get<ReloadType>("type")

        sender.sendMessage("Starting reload process, it may take a while...")

        val elapsed = measureTimeMillis {
            when (reloadType) {
                ReloadType.ALL -> {
                    ReloadProcess.all()
                }

                ReloadType.RECIPE -> {
                    ReloadProcess.recipes()
                }
            }
        }

        sender.sendMessage("Reload OK, ${elapsed}ms elapsed.")
    }

    enum class ReloadType {
        ALL, // 所有 (不包含配方)
        RECIPE, // 配方
    }
}

/**
 * 封装了各模块的重载逻辑.
 *
 * 注意这里不应该隐式的包含任何依赖关系.
 * 每个重载逻辑都只负责自己的内容 (即使这会导致最终的数据关系错乱).
 * 依赖关系的维护应该交给调用方 (也就是使用重载指令的人).
 */
private object ReloadProcess {

    fun all() {
        ConfigAccess.INSTANCE.reload()

        GlobalTranslations.reload()

        EntityRefRegistryLoader.reload()

        ElementRegistryLoader.reload()
        RarityRegistryLoader.reload()
        KizamiRegistryLoader.reload()

        AttributeSupplierRegistryLoader.reload()
        ImgAttributeMapRegistryLoader.reload()

        LootTableRegistryLoader.reload()

        CustomItemRegistryLoader.reload()
        ItemProxyRegistryLoader.reload()

        BasicGuiInitializer.reload()

        CraftingStationRecipeRegistry.reload()
        CraftingStationRegistry.reload()
        MergingTableRegistry.reload()
        ModdingTableRegistry.reload()
        RerollingTableRegistry.reload()
        RepairingTableRegistry.reload()
        RecyclingStationRegistry.reload()
        BlacksmithStationRegistry.reload()

        SimpleItemRenderer.reload()
        StandardItemRenderer.reload()
        MergingTableItemRenderer.reload()
        RepairingTableItemRenderer.reload()
        CraftingStationItemRenderer.reload()

        CatalogItemMenuSettings.reload()
        CatalogItemLootTableRecipeRegistryLoader.reload()
        CatalogItemCategoryRegistryLoader.reload()

        AttackCharacteristicDamageMappings.reload()
        DamageTypeDamageMappings.reload()
        NullCausingDamageMappings.reload()
        PlayerAdhocDamageMappings.reload()

        ResourcePackLifecycle.reload()
    }

    fun recipes() {
        ItemTagManager.reload()
        RecipeInitializer.reload()
    }
}