package cc.mewcraft.wakame.reforge.recycle

import cc.mewcraft.wakame.reforge.common.ReforgeLoggerPrefix
import cc.mewcraft.wakame.util.decorate
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.slf4j.Logger

internal class SimpleRecyclingSession : RecyclingSession, KoinComponent {
    val logger: Logger = get<Logger>().decorate(prefix = ReforgeLoggerPrefix.RECYCLE)

    override fun sell() {
        logger.info("Sold!")
    }
}