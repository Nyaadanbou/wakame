package cc.mewcraft.wakame.lifecycle

/**
 * An exception specifically made to be thrown during initialization of a **Lifecycle Task**.
 * When such an exception is thrown there, only the message but not the stack trace is printed.
 */
class LifecycleException(message: String) : Exception(message)