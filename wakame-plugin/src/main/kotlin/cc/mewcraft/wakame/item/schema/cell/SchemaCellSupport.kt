package cc.mewcraft.wakame.item.schema.cell

import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.item.schema.cell.core.SchemaCore
import cc.mewcraft.wakame.item.schema.cell.curse.SchemaCurse
import cc.mewcraft.wakame.random.Group

/**
 * 空的蓝图词条栏。
 */
data object EmptySchemaCell : SchemaCell {
    override val id: String = "empty"
    override val canReforge: Boolean = false
    override val canOverride: Boolean = false
    override val keepEmpty: Boolean = false
    override val coreSelector: Group<SchemaCore, SchemaGenerationContext> = Group.empty()
    override val curseSelector: Group<SchemaCurse, SchemaGenerationContext> = Group.empty()
}

/**
 * 不可变的蓝图词条栏。
 */
data class ImmutableSchemaCell(
    override val id: String,
    override val keepEmpty: Boolean,
    override val canReforge: Boolean,
    override val canOverride: Boolean,
    override val coreSelector: SchemaCoreGroup,
    override val curseSelector: SchemaCurseGroup,
) : SchemaCell