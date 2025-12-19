package cc.mewcraft.wakame.hook.impl.betonquest.util

enum class ComparisonOp(override val symbol: String) : FriendlyEnum {

    LESS_THAN("<"),
    LESS_THAN_OR_EQUAL("<="),
    EQUAL("="),
    GREATER_THAN_OR_EQUAL(">="),
    GREATER_THAN(">");
}