package cc.mewcraft.wakame.monetization

import cc.mewcraft.messaging2.ServerInfoProvider
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.database.DatabaseManager
import cc.mewcraft.wakame.lifecycle.initializer.DisableFun
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.monetization.zpay.ZPayCallbackServerImpl
import cc.mewcraft.wakame.monetization.zpay.ZPayClientImpl
import cc.mewcraft.wakame.monetization.zpay.ZPaySignatureImpl
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 * 支付模块的生命周期管理.
 *
 * 负责按序初始化和销毁所有内部组件,
 * 并将 [MonetizationImpl] 注册到 [Monetization] 供外部模块访问.
 *
 * ### 配置项语义
 *
 * - **`enabled`**: 控制整个模块的开关. 为 `false` 时, 模块完全不加载 (包括数据库).
 * - **`enabled_server`**: 仅控制 Z-PAY 通讯 (HTTP 回调服务器) 是否启动.
 *   订单数据库查询在所有 `enabled=true` 的服务器上均可用,
 *   但只有 `enabled_server` 匹配的服务器才会启动 Z-PAY 客户端和回调服务器.
 */
@Init(InitStage.POST_WORLD)
internal object MonetizationBootstrap {

    /** Ktor stop/close 清理线程的最大等待时间 (秒). */
    private const val SHUTDOWN_TIMEOUT_SECONDS = 5L

    private var client: ZPayClientImpl? = null
    private var callbackServer: ZPayCallbackServerImpl? = null

    @InitFun
    fun init() {
        if (!MonetizationConfig.enabled) {
            LOGGER.info("[Monetization] Module is disabled by config.")
            return
        }

        LOGGER.info("[Monetization] Initializing payment system...")

        // 1. 根据配置选择存储实现 — 所有 enabled 的服务器都会初始化
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

        // 2. 仅在指定服务器上启动 Z-PAY 通讯
        val isPaymentServer = MonetizationConfig.enabledServer == ServerInfoProvider.serverKey
        val service: PaymentService? = if (isPaymentServer) {
            LOGGER.info("[Monetization] This server is the payment server — starting Z-PAY communication.")
            val signature = ZPaySignatureImpl(MonetizationConfig.zPayApi.pkey)
            val client = ZPayClientImpl(MonetizationConfig.zPayApi, signature).also {
                client = it
            }
            val svc = PaymentServiceImpl(client, repository)

            // 启动回调服务器
            val server = ZPayCallbackServerImpl(MonetizationConfig.callbackServer, signature, svc)
            server.start()
            callbackServer = server

            svc
        } else {
            LOGGER.info("[Monetization] This server is NOT the payment server — Z-PAY communication disabled, database queries only.")
            null
        }

        Monetization.setImplementation(MonetizationImpl(service, repository))

        LOGGER.info("[Monetization] Payment system initialized. (storage=$storage, paymentServer=$isPaymentServer)")
    }

    @DisableFun
    fun disable() {
        if (!MonetizationConfig.enabled) {
            return
        }

        LOGGER.info("[Monetization] Shutting down payment system...")

        // 1. 立即切换到 NoOp 实现, 使所有并发调用方 (如 LuckPerms ContextCalculator) 不再访问已关闭的数据库
        Monetization.clearImplementation()
        MonetizationCache.shutdown()

        // 2. 在后台线程执行 Ktor 的 stop/close, 设置硬性超时以避免阻塞 Server 线程
        //    Ktor CIO 的 stop() 依赖协程调度器, 如果调度器已在关服流程中被销毁, stop() 可能永远阻塞
        val callbackServer = callbackServer
        val httpClient = client

        this.callbackServer = null
        client = null

        if (callbackServer != null || httpClient != null) {
            val cleanupThread = thread(isDaemon = true, name = "Monetization-Shutdown") {
                runCatching { callbackServer?.stop() }.onFailure { LOGGER.warn("[Monetization] Exception while stopping callback server: ${it.message}") }
                runCatching { httpClient?.close() }.onFailure { LOGGER.warn("[Monetization] Exception while closing HTTP client: ${it.message}") }
            }
            try {
                cleanupThread.join(TimeUnit.SECONDS.toMillis(SHUTDOWN_TIMEOUT_SECONDS))
                if (cleanupThread.isAlive) {
                    LOGGER.warn("[Monetization] Cleanup thread did not finish within ${SHUTDOWN_TIMEOUT_SECONDS}s, proceeding with shutdown.")
                    cleanupThread.interrupt()
                }
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }

        LOGGER.info("[Monetization] Payment system shut down.")
    }
}
