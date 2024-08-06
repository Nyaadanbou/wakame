package cc.mewcraft.wakame.reforge.rerolling

import org.koin.core.component.KoinComponent
import java.io.File

internal object RerollingTableSerializer : KoinComponent {
    const val REFORGE_DIR_NAME = "reforge"
    const val REROLLING_DIR_NAME = "rerolling"

    /**
     * 从配置文件中加载所有的重造台.
     */
    fun loadAll(): Map<String, RerollingTable> {
        // TODO
        return emptyMap()
    }

    /**
     * 读取指定的配置文件夹, 从中构建一个 [RerollingTable].
     *
     * 文件结构必须如下:
     * ```
     * tableDir/
     * ├─ config.yml
     * ├─ items/
     * │  ├─ namespace_1/
     * │  │  ├─ path_1.yml
     * │  │  ├─ path_2.yml
     * │  ├─ namespace_2/
     * │  │  ├─ path_1.yml
     * │  │  ├─ path_2.yml
     * ```
     *
     * @param tableDir 重造台的配置文件夹
     * @return 读取的 [RerollingTable]
     * @throws IllegalArgumentException 如果 [tableDir] 不符合要求
     */
    fun load(tableDir: File): RerollingTable {
        // TODO
        return RerollingTableWtf
    }
}