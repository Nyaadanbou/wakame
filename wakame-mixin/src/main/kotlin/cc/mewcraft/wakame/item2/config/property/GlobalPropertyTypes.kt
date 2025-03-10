package cc.mewcraft.wakame.item2.config.property

import cc.mewcraft.wakame.item2.config.property.impl.Arrow
import cc.mewcraft.wakame.util.typeTokenOf

object GlobalPropertyTypes {

    @JvmField
    val ARROW: GlobalPropertyType<Arrow> = TODO()

    /**
     * @param id 将作为 Registry 中的 id
     * @param block 用于配置 [GlobalPropertyType]
     */
    private inline fun <reified T> register(id: String, block: GlobalPropertyType.Builder<T>.() -> Unit = {}): GlobalPropertyType<T> {
        // FIXME 在 KoishRegistry 中注册以支持 type dispatching
        return GlobalPropertyType.builder(typeTokenOf<T>()).apply(block).build()
    }
}