package cc.mewcraft.wakame.initializer2

/**
 * An exception specifically made to be thrown during initialization of an [Initializable].
 * When such an exception is thrown there, only the message but not the stack trace is printed.
 */
class InitializationException(message: String) : Exception(message)