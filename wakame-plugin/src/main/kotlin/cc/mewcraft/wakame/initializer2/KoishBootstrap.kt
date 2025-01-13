@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.initializer2

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.NEKO
import cc.mewcraft.wakame.attribute.AttributeMapPatchListener
import cc.mewcraft.wakame.command.CommandManager
import cc.mewcraft.wakame.damage.DamageListener
import cc.mewcraft.wakame.damage.DamagePostListener
import cc.mewcraft.wakame.ecs.EcsListener
import cc.mewcraft.wakame.element.ElementRegistryConfigStorage
import cc.mewcraft.wakame.entity.UnbreakableArmorStand
import cc.mewcraft.wakame.entity.attribute.AttributeBundleFacadeRegistryConfigStorage
import cc.mewcraft.wakame.entity.attribute.AttributeSupplierRegistryConfigStorage
import cc.mewcraft.wakame.gui.GuiManager
import cc.mewcraft.wakame.item.ItemBehaviorListener
import cc.mewcraft.wakame.item.ItemChangeListener
import cc.mewcraft.wakame.item.ItemMiscellaneousListener
import cc.mewcraft.wakame.item.ItemRegistryConfigStorage
import cc.mewcraft.wakame.item.component.ItemComponentRegistry
import cc.mewcraft.wakame.item.logic.ItemSlotChangeManager
import cc.mewcraft.wakame.kizami.KizamiRegistryConfigStorage
import cc.mewcraft.wakame.lang.GlobalTranslations
import cc.mewcraft.wakame.pack.ResourcePackLifecycleListener
import cc.mewcraft.wakame.pack.ResourcePackPlayerListener
import cc.mewcraft.wakame.packet.DamageDisplay
import cc.mewcraft.wakame.player.equipment.ArmorChangeEventSupport
import cc.mewcraft.wakame.rarity.LevelRarityMappingRegistryConfigStorage
import cc.mewcraft.wakame.rarity.RarityRegistryConfigStorage
import cc.mewcraft.wakame.registry.ABILITY_PROTO_CONFIG_DIR
import cc.mewcraft.wakame.resource.ResourceSynchronizer
import cc.mewcraft.wakame.user.PaperUserManager
import cc.mewcraft.wakame.user.PlayerLevelListener
import cc.mewcraft.wakame.world.player.death.PlayerDeathProtect
import org.bukkit.event.Listener
import xyz.xenondevs.invui.InvUI
import xyz.xenondevs.invui.window.WindowManager

/**
 * @see Initializable
 */
@InternalInit(
    stage = InternalInitStage.PRE_WORLD
)
object KoishBootstrap {

    /**
     * Should be called before the world is loaded.
     */
    @InitFun
    fun start() {
        warmupStaticBlocks()
        saveDefaultConfigs()
        registerListeners()
        registerCommands()
    }

    @DisableFun
    fun close() {
        // 关闭服务器时服务端不会触发任何事件,
        // 需要我们手动执行保存玩家资源的逻辑.
        // 如果服务器有使用 HuskSync, 我们的插件必须在 HuskSync 之前关闭,
        // 否则 PDC 无法保存到 HuskSync 的数据库, 导致玩家资源数据丢失.
        ResourceSynchronizer.saveAll()

        // 关闭所有打开的 GUI
        GuiManager.closeAll()
    }

    private fun saveDefaultConfigs() = with(NEKO) {
        saveDefaultConfig() // config.yml
        saveResourceRecursively(ItemRegistryConfigStorage.DIR_PATH)
        saveResourceRecursively(KizamiRegistryConfigStorage.DIR_PATH)
        saveResourceRecursively(GlobalTranslations.DIR_PATH)
        saveResourceRecursively("reforge")
        saveResourceRecursively(ABILITY_PROTO_CONFIG_DIR)
        saveResource(AttributeBundleFacadeRegistryConfigStorage.FILE_PATH)
        saveResource(ElementRegistryConfigStorage.FILE_PATH)
        saveResource(AttributeSupplierRegistryConfigStorage.FILE_PATH)
        saveResource(ItemComponentRegistry.CONFIG_FILE_NAME)
        saveResource(LevelRarityMappingRegistryConfigStorage.FILE_PATH)
        saveResource(RarityRegistryConfigStorage.FILE_PATH)
        saveResourceRecursively("renderers")
        saveResourceRecursively("station")
        saveResourceRecursively("damage")
    }

    private fun registerListeners() {
        // item
        registerListener<ArmorChangeEventSupport>()
        registerListener<ItemSlotChangeManager>()
        registerListener<ItemChangeListener>()
        registerListener<ItemBehaviorListener>()
        registerListener<ItemMiscellaneousListener>()

        // attribute
        registerListener<AttributeMapPatchListener>()

        // damage
        registerListener<DamageListener>()
        registerListener<DamagePostListener>()
        registerListener<DamageDisplay>()

        // ecs
        registerListener<EcsListener>()

        // rpg player
        registerListener<PaperUserManager>()

        // resourcepack
        registerListener<ResourcePackLifecycleListener>()
        registerListener<ResourcePackPlayerListener>()

        // game world
        registerListener<UnbreakableArmorStand>()
        registerListener<PlayerDeathProtect>()

        // compatibility
        registerListener<PlayerLevelListener>("AdventureLevel")

        // uncategorized
    }

    private inline fun <reified T : Listener> registerListener(requiredPlugin: String? = null) {
        if (requiredPlugin != null) {
            if (NEKO.isPluginPresent(requiredPlugin)) {
                NEKO.registerListener(Injector.get<T>())
            } else {
                LOGGER.info("Plugin $requiredPlugin is not present. Skipping listener ${T::class.simpleName}")
            }
        } else {
            NEKO.registerListener(Injector.get<T>())
        }
    }

    private fun registerCommands() {
        CommandManager(NEKO).init()
    }

    private fun warmupStaticBlocks() {
        InvUI.getInstance()
        WindowManager.getInstance()
    }
}