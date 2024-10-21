package cc.mewcraft.wakame.util

import net.kyori.examination.Examinable
import net.kyori.examination.string.StringExaminer

fun Examinable.toSimpleString(): String {
    return this.examine(StringExaminer.simpleEscaping())
}