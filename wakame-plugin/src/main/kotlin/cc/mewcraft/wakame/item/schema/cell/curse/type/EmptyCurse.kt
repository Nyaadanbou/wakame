package cc.mewcraft.wakame.item.schema.cell.curse.type

import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.item.binary.cell.curse.BinaryCurse
import cc.mewcraft.wakame.item.binary.cell.curse.type.BinaryEmptyCurse
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.item.schema.cell.curse.SchemaCurse
import net.kyori.adventure.key.Key
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

fun SchemaEmptyCurse(): SchemaEmptyCurse {
    return SchemaEmptyCurse
}

/**
 * 代表一个空的蓝图诅咒。
 */
data object SchemaEmptyCurse : SchemaCurse {
    override val key: Key = GenericKeys.EMPTY
    override fun reify(context: SchemaGenerationContext): BinaryCurse = BinaryEmptyCurse()
    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(ExaminableProperty.of("key", key))
    }
}