package cc.mewcraft.wakame.annotation

/**
 * This annotation is used to provide information about the configuration path.
 *
 * This may be used in **any** classes that make use of this annotation. This
 * annotation does not provide any functions on its own, which means that you
 * should follow the development guide of the code you are working on.
 *
 * @property path the path to the configuration node
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class ConfigPath(val path: String)