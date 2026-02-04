package cc.mewcraft.wakame.hook.impl.nightcore

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.util.decorate
import org.slf4j.Logger
import su.nightexpress.nightcore.integration.currency.EconomyBridge
import su.nightexpress.nightcore.integration.item.ItemBridge

@Hook(plugins = ["nightcore"])
object NightCoreHook {

    private val logger: Logger = LOGGER.decorate(NightCoreHook::class)

    init {
        // 向 Nightcore 注册 Koish 物品适配器
        ItemBridge.register(KoishItemAdapter())
        logger.info("Currently registered item adapters: {}", ItemBridge.registry().values().joinToString { it.name })

        // 向 Nightcore 注册 Economy 货币适配器
        EconomyCurrency.currencies().forEach(EconomyBridge::register)
        logger.info("Currently registered currency adapters: {}", EconomyBridge.registry().values().joinToString { it.name })

        // TODO: 这样注册的实例会在 nightcore 重载后失效
        //  解决办法就是让 nightcore 那边暴露一个重载事件
        //  暂时先记录一个日志, 免得后面我们忘记这回事儿了
        logger.warn("NightCore integration is experimental and will not work correctly after NightCore reloads!")
    }
}