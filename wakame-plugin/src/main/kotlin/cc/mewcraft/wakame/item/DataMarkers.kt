package cc.mewcraft.wakame.item

/**
 * 标记一个类为「实例」数据，也就是游戏世界的映射。
 *
 * 如果光从类的名字就能区分是否为实例数据，则不必加此标注。
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class BinaryData

/**
 * 标记一个类为「模板」数据，也就是存在于游戏世界之外。
 *
 * 如果光从类的名字就能区分是否为模板数据，则不必加此标注。
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class SchemeData
