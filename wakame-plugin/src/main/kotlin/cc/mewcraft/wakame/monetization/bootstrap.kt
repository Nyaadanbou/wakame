package cc.mewcraft.wakame.monetization

import cc.mewcraft.wakame.LOGGER
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
        if (!MonetizationConfig.enabled) {
            LOGGER.info("[Monetization] Module is disabled by config.")
            return
        }

        LOGGER.info("[Monetization] Initializing payment system...")

        val signature = ZPaySignatureImpl(MonetizationConfig.zPayApi.pkey)
        val client = ZPayClientImpl(MonetizationConfig.zPayApi, signature)
        val repository = InMemoryOrderRepository()
        val service = PaymentServiceImpl(client, repository)

        val server = ZPayCallbackServerImpl(MonetizationConfig.callbackServer, signature, service)
        server.start()
        callbackServer = server

        Monetization.setImplementation(MonetizationImpl(service, repository))

        LOGGER.info("[Monetization] Payment system initialized.")
    }

    @DisableFun
    fun disable() {
        if (!MonetizationConfig.enabled) {
            return
        }

        LOGGER.info("[Monetization] Shutting down payment system...")

        callbackServer?.stop()
        callbackServer = null

        Monetization.clearImplementation()

        LOGGER.info("[Monetization] Payment system shut down.")
    }
}
