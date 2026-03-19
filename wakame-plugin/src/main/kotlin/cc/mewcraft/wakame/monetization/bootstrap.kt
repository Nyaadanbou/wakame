package cc.mewcraft.wakame.monetization

import cc.mewcraft.messaging2.ServerInfoProvider
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.database.DatabaseManager
import cc.mewcraft.wakame.lifecycle.initializer.DisableFun
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.monetization.zpay.ZPayCallbackServer
import cc.mewcraft.wakame.monetization.zpay.ZPayCallbackServerImpl
import cc.mewcraft.wakame.monetization.zpay.ZPayClientImpl
import cc.mewcraft.wakame.monetization.zpay.ZPaySignatureImpl

/**
 * 支付模块的生命周期管理.
 *
 * 负责按序初始化和销毁所有内部组件,
 * 并将 [MonetizationImpl] 注册到 [Monetization] 供外部模块访问.
 */
@Init(InitStage.POST_WORLD)
internal object MonetizationBootstrap {

    private var callbackServer: ZPayCallbackServer? = null

    @InitFun
    fun init() {
        if (!(MonetizationConfig.enabled && MonetizationConfig.enabledServer == ServerInfoProvider.serverKey)) {
            LOGGER.info("[Monetization] Module is disabled by config.")
            return
        }

        LOGGER.info("[Monetization] Initializing payment system...")

        // 1. 根据配置选择存储实现
        val storage = MonetizationConfig.storage
        val repository: OrderRepository = when (storage) {
            StorageType.IN_MEMORY -> {
                LOGGER.info("[Monetization] Using in-memory storage (data will be lost on restart).")
                InMemoryOrderRepository()
            }

            StorageType.DATABASE -> {
                LOGGER.info("[Monetization] Using database storage (global connection).")
                ExposedOrderRepository(DatabaseManager.database()).apply {
                    createSchemaIfNeeded()
                }
            }
        }

        // 2. 创建支付服务
        val signature = ZPaySignatureImpl(MonetizationConfig.zPayApi.pkey)
        val client = ZPayClientImpl(MonetizationConfig.zPayApi, signature)
        val service = PaymentServiceImpl(client, repository)

        // 3. 启动回调服务器
        val server = ZPayCallbackServerImpl(MonetizationConfig.callbackServer, signature, service)
        server.start()
        callbackServer = server

        Monetization.setImplementation(MonetizationImpl(service, repository))

        LOGGER.info("[Monetization] Payment system initialized. (storage=$storage)")
    }

    @DisableFun
    fun disable() {
        if (!(MonetizationConfig.enabled && MonetizationConfig.enabledServer == ServerInfoProvider.serverKey)) {
            return
        }

        LOGGER.info("[Monetization] Shutting down payment system...")

        callbackServer?.stop()
        callbackServer = null

        Monetization.clearImplementation()

        LOGGER.info("[Monetization] Payment system shut down.")
    }
}
