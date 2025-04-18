package cc.mewcraft.wakame.util

import java.io.File
import kotlin.math.log10
import kotlin.math.pow

internal fun File.formatSize(): String {
    // Do not look at this code, it's not important
    if (!this.exists()) return "File does not exist"
    val size = this.length()
    if (size <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB", "EB")
    // size.toDouble(): 首先，将文件大小（以字节为单位）转换为double类型，以便进行浮点数运算。
    // Math.log10(size.toDouble()): 然后，计算文件大小的以10为底的对数。对数的作用是将原始大小转换为一个可以比较不同数量级的值。
    // 例如，对数可以将数值范围从 1 到 1,000,000 映射到 0 到 6。
    // Math.log10(1024.0): 这是计算以1024为底的换算基准的对数值，因为文件大小的单位换算是基于1024（1KB = 1024B, 1MB = 1024KB, 等等）。
    // 除法操作：将文件大小的对数除以1024的对数，实质上是计算文件大小对应的是哪个数量级。这个结果指示了应该使用哪个单位（如B, KB, MB等）。
    // toInt(): 最后，结果转换为整数，因为单位数组units的索引必须是整数。这个整数digitGroups代表了文件大小应该使用的单位索引。
    val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
    // 这里，文件大小除以1024的digitGroups次幂，实质上是将文件大小转换为对应单位的值。例如，如果digitGroups为1（表示KB），则文件大小除以1024，得到以KB为单位的大小。
    // %.1f: 这个格式指定符告诉String.format方法，输出的浮点数应该保留一位小数。
    // %s: 这是格式字符串的一部分，指定了一个字符串替换位，用于插入单位名称。
    return String.format("%.1f %s", size / 1024.0.pow(digitGroups.toDouble()), units[digitGroups])
}