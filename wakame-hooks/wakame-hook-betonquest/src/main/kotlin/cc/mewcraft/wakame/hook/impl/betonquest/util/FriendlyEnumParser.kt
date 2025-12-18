package cc.mewcraft.wakame.hook.impl.betonquest.util

import org.betonquest.betonquest.api.instruction.argument.Argument
import org.betonquest.betonquest.api.quest.QuestException

inline fun <reified E> FriendlyEnumParser(): FriendlyEnumParser<E> where E : Enum<E>, E : FriendlyEnum {
    return FriendlyEnumParser(E::class.java)
}

class FriendlyEnumParser<E>(
    private val enumClass: Class<E>,
) : Argument<E> where E : Enum<E>, E : FriendlyEnum {

    override fun apply(string: String): E {
        val input = string.trim()
        if (input.isEmpty()) {
            throw QuestException("Value for ${enumClass.simpleName} cannot be empty")
        }

        val constants = enumClass.enumConstants ?: emptyArray()

        // 1) Try enum constant name (case-insensitive)
        val byName = constants.firstOrNull { it.name.equals(input, ignoreCase = true) }
        if (byName != null) {
            return byName
        }

        // 2) Try FriendlyEnum.symbol (exact match)
        val bySymbol = constants.firstOrNull { it.symbol == input }
        if (bySymbol != null) {
            return bySymbol
        }

        val names = constants.joinToString(", ") { it.name }
        val symbols = constants.joinToString(", ") { it.symbol }
        throw QuestException(
            "Unknown ${enumClass.simpleName} value '$input'. Allowed names: [$names]. Allowed symbols: [$symbols]"
        )
    }
}