package cc.mewcraft.extracontexts.common.messaging

import cc.mewcraft.messaging2.MessagingConfiguration

/**
 * Initialize ExtraContexts messaging system (Velocity version).
 *
 * This class should be called when Velocity plugin is enabled to start cross-server cache invalidation messaging.
 */
object MessagingInitializer {

    /**
     * Initialize messaging system.
     *
     * @param config Messaging system configuration object
     */
    fun initialize(config: MessagingConfiguration) {
        MessagingManager.init(config)
        MessagingManager.start()
    }

    /**
     * Shutdown messaging system.
     */
    fun shutdown() {
        MessagingManager.shutdown()
    }
}