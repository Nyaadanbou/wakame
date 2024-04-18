package cc.mewcraft.wakame.item.schema.cell

/**
 * 不可变的蓝图词条栏。
 */
internal data class SchemaCellImpl(
    override val createOptions: SchemaCell.CreateOptions,
    override val modifyOptions: SchemaCell.ModifyOptions,
) : SchemaCell {

    data class ImmutableCreateOptions(
        override val core: SchemaCoreGroup,
        override val curse: SchemaCurseGroup,
    ) : SchemaCell.CreateOptions

    data class ImmutableModifyOptions(
        override val isReforgeable: Boolean,
    ) : SchemaCell.ModifyOptions

}

