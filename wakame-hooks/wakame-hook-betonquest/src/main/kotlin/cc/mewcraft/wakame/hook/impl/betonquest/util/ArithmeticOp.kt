package cc.mewcraft.wakame.hook.impl.betonquest.util

enum class ArithmeticOp(override val symbol: String): FriendlyEnum {

    ADD("+"),
    SUBTRACT("-"),
    MULTIPLY("*"),
    DIVIDE("/"),
    MODULO("%");
}