package cc.mewcraft.wakame

import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.config.node
import cc.mewcraft.wakame.config.optionalEntry
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.ConfigurationNode
import xyz.xenondevs.commons.provider.Provider
import xyz.xenondevs.commons.provider.orElseLazily
import kotlin.reflect.KProperty0

fun a(): Unit {
}

private val CONFIG: Provider<CommentedConfigurationNode> = Configs["example"]

//class Data(source: Provider<ConfigurationNode>, fallback: Provider<ConfigurationNode>? = null) {
//    val a: String by source.optionalEntry<String>("a").orElse(fallback?.entry("a")).requireNotNull()
//    val b: String by source.optionalEntry<String>("b").orElse(fallback?.entry("b")).requireNotNull()
//    val c: String by source.optionalEntry<String>("c").orElse(fallback?.entry("c")).requireNotNull()
//
//    private fun <T> KProperty0<T>.unsafeDelegate(): T = getDelegate() as T
//}
//
//class Outer {
//    val defaultNode = CONFIG.node("default")
//    val data1Node = CONFIG.node("data1")
//    val data2Node = CONFIG.node("data2")
//    val data3Node = CONFIG.node("data3")
//
//    val default: Data = Data(defaultNode)
//    val data1: Data = Data(data1Node, defaultNode)
//    val data2: Data = Data(data2Node, data1Node)
//    val data3: Data = Data(data3Node, data2Node)
//
//    fun doSomething() {
//        val aInData1: String = data1.a
//        val bInData1: String = data1.b
//        val cInData1: String = data1.c
//
//        val aInData2: String = data2.a
//        val bInData2: String = data2.b
//        val cInData2: String = data2.c
//
//        val aInData3: String = data3.a
//        val bInData3: String = data3.b
//        val cInData3: String = data3.c
//    }
//}

class Data(source: Provider<ConfigurationNode>, fallback: Data? = null) {

    val a: String by source.optionalEntry<String>("a").orElseLazily(fallback.requireDelegate { it::a })
    val b: String by source.optionalEntry<String>("b").orElseLazily(fallback.requireDelegate { it::b })
    val c: String by source.optionalEntry<String>("c").orElseLazily(fallback.requireDelegate { it::c })

    private fun <T> Data?.requireDelegate(getter: (Data) -> KProperty0<T>): () -> T {
        return { getter(requireNotNull(this)).getDelegate() as T }
    }

}

class Outer {

    fun doSomething() {
        val dataDefault: Data = Data(CONFIG.node("default"))
        val data1: Data = Data(CONFIG.node("data1"), dataDefault)
        val data2: Data = Data(CONFIG.node("data2"), data1)
        val data3: Data = Data(CONFIG.node("data3"), data2)

        val aInData1: String = data1.a
        val bInData1: String = data1.b
        val cInData1: String = data1.c

        val aInData2: String = data2.a
        val bInData2: String = data2.b
        val cInData2: String = data2.c

        val aInData3: String = data3.a
        val bInData3: String = data3.b
        val cInData3: String = data3.c
    }

}