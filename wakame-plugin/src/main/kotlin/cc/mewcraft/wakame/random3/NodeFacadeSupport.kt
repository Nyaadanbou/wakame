package cc.mewcraft.wakame.random3

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger

object NodeFacadeSupport : KoinComponent {
    fun reload(facade: NodeFacade<*>) {
        logger.info("Loading: ${facade.dataDir}")
        facade.populate()
    }

    private val logger: Logger by inject()
}