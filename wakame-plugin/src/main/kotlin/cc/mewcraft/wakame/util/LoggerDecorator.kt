package cc.mewcraft.wakame.util

import org.slf4j.Logger

fun Logger.decorate(prefix: String): Logger =
    LoggerDecorator(this, prefix)

fun Lazy<Logger>.decorate(prefix: String): Lazy<Logger> =
    map { value.decorate(prefix) }

/**
 * 为 [Logger] 提供装饰器功能.
 *
 * @param logger 要装饰的 [Logger]
 * @param prefix 装饰的前缀
 */
class LoggerDecorator(
    private val logger: Logger,
    private val prefix: String,
) : Logger by logger {
    override fun debug(msg: String) = logger.debug("$prefix$msg")
    override fun info(msg: String) = logger.info("$prefix$msg")
    override fun warn(msg: String) = logger.warn("$prefix$msg")
    override fun error(msg: String) = logger.error("$prefix$msg")
    override fun trace(msg: String) = logger.trace("$prefix$msg")

    override fun debug(format: String, arg: Any) = logger.debug("$prefix$format", arg)
    override fun info(format: String, arg: Any) = logger.info("$prefix$format", arg)
    override fun warn(format: String, arg: Any) = logger.warn("$prefix$format", arg)
    override fun error(format: String, arg: Any) = logger.error("$prefix$format", arg)
    override fun trace(format: String, arg: Any) = logger.trace("$prefix$format", arg)

    override fun debug(format: String, vararg arguments: Any) = logger.debug("$prefix$format", *arguments)
    override fun info(format: String, vararg arguments: Any) = logger.info("$prefix$format", *arguments)
    override fun warn(format: String, vararg arguments: Any) = logger.warn("$prefix$format", *arguments)
    override fun error(format: String, vararg arguments: Any) = logger.error("$prefix$format", *arguments)
    override fun trace(format: String, vararg arguments: Any) = logger.trace("$prefix$format", *arguments)
}