package cc.mewcraft.wakame.reforge.merging

import java.io.File

object MergingTableSerializer {
    /**
     * 从配置文件夹中加载所有的合并台.
     */
    fun loadAll(): Map<String, MergingTable> {
        // TODO
        return emptyMap()
    }

    /**
     * 读取指定的配置文件夹, 从中构建一个 [MergingTable].
     */
    fun load(tableDir: File): MergingTable {
        // TODO
        return WtfMergingTable
    }
}