@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.initializer2

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.NEKO
import cc.mewcraft.wakame.attribute.AttributeMapPatchListener
import cc.mewcraft.wakame.command.CommandManager
import cc.mewcraft.wakame.damage.DamageListener
import cc.mewcraft.wakame.damage.DamagePostListener
import cc.mewcraft.wakame.ecs.EcsListener
import cc.mewcraft.wakame.element.ElementRegistryConfigStorage
import cc.mewcraft.wakame.gui.GuiManager
import cc.mewcraft.wakame.item.ItemBehaviorListener
import cc.mewcraft.wakame.item.ItemChangeListener
import cc.mewcraft.wakame.item.ItemMiscellaneousListener
import cc.mewcraft.wakame.item.component.ItemComponentRegistry
import cc.mewcraft.wakame.item.logic.ItemSlotChangeManager
import cc.mewcraft.wakame.pack.ResourcePackLifecycleListener
import cc.mewcraft.wakame.pack.ResourcePackPlayerListener
import cc.mewcraft.wakame.packet.DamageDisplay
import cc.mewcraft.wakame.player.equipment.ArmorChangeEventSupport
import cc.mewcraft.wakame.registry.ABILITY_PROTO_CONFIG_DIR
import cc.mewcraft.wakame.registry.ATTRIBUTE_GLOBAL_CONFIG_FILE
import cc.mewcraft.wakame.registry.CRATE_PROTO_CONFIG_DIR
import cc.mewcraft.wakame.registry.ENTITY_GLOBAL_CONFIG_FILE
import cc.mewcraft.wakame.registry.ITEM_PROTO_CONFIG_DIR
import cc.mewcraft.wakame.registry.KizamiRegistry.KIZAMI_DIR_NAME
import cc.mewcraft.wakame.registry.LANG_PROTO_CONFIG_DIR
import cc.mewcraft.wakame.registry.LEVEL_GLOBAL_CONFIG_FILE
import cc.mewcraft.wakame.registry.RARITY_GLOBAL_CONFIG_FILE
import cc.mewcraft.wakame.resource.ResourceSynchronizer
import cc.mewcraft.wakame.user.PaperUserManager
import cc.mewcraft.wakame.user.PlayerLevelListener
import cc.mewcraft.wakame.world.entity.BetterArmorStandListener
import cc.mewcraft.wakame.world.player.death.PlayerDeathProtect
import org.bukkit.event.Listener
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import xyz.xenondevs.invui.InvUI
import xyz.xenondevs.invui.window.WindowManager

/**
 * @see Initializable
 */
@InternalInit(
    stage = InternalInitStage.PRE_WORLD
)
object KoishBootstrap : KoinComponent {

    /**
     * Should be called before the world is loaded.
     */
    @InitFun
    fun start() {
        warmupStaticInitializer()
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
        saveResourceRecursively(CRATE_PROTO_CONFIG_DIR)
        saveResourceRecursively(ITEM_PROTO_CONFIG_DIR)
        saveResourceRecursively(KIZAMI_DIR_NAME)
        saveResourceRecursively(LANG_PROTO_CONFIG_DIR)
        saveResourceRecursively("reforge")
        saveResourceRecursively(ABILITY_PROTO_CONFIG_DIR)
        saveResource(ATTRIBUTE_GLOBAL_CONFIG_FILE)
        // saveResource(CATEGORY_GLOBAL_CONFIG_FILE) // 完成该模块后再去掉注释
        saveResource(ElementRegistryConfigStorage.FILE_PATH)
        saveResource(ENTITY_GLOBAL_CONFIG_FILE)
        saveResource(ItemComponentRegistry.CONFIG_FILE_NAME)
        saveResource(LEVEL_GLOBAL_CONFIG_FILE)
        // saveResource(PROJECTILE_GLOBAL_CONFIG_FILE) // 完成该模块后再去掉注释
        saveResource(RARITY_GLOBAL_CONFIG_FILE)
        saveResourceRecursively("renderers")
        saveResourceRecursively("station")
        saveResourceRecursively("damage")
        // saveResource(SKIN_GLOBAL_CONFIG_FILE) // 完成该模块后再去掉注释
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
        registerListener<BetterArmorStandListener>()
        registerListener<PlayerDeathProtect>()

        // compatibility
        registerListener<PlayerLevelListener>("AdventureLevel")

        // uncategorized
    }

    private inline fun <reified T : Listener> registerListener(requiredPlugin: String? = null) {
        if (requiredPlugin != null) {
            if (NEKO.isPluginPresent(requiredPlugin)) {
                NEKO.registerListener(get<T>())
            } else {
                LOGGER.info("Plugin $requiredPlugin is not present. Skipping listener ${T::class.simpleName}")
            }
        } else {
            NEKO.registerListener(get<T>())
        }
    }

    private fun registerCommands() {
        CommandManager(NEKO).init()
    }

    private fun warmupStaticInitializer() {
        InvUI.getInstance()
        WindowManager.getInstance()
    }
}