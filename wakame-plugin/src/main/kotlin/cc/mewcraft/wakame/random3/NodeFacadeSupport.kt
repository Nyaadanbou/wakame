package cc.mewcraft.wakame.random3

import cc.mewcraft.wakame.LOGGER

object NodeFacadeSupport {
    fun reload(facade: NodeFacade<*>) {
        LOGGER.info("Loading global random selectors: ${facade.dataDir}")
        facade.populate()
    }
}