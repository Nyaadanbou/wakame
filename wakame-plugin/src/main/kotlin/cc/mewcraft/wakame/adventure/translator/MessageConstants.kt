package cc.mewcraft.wakame.adventure.translator

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent

object MessageConstants {
    val MSG_OPENED_MERGING_MENU = create("msg_opened_merging_menu")
    val MSG_OPENED_MODDING_MENU = create("msg_opened_modding_menu")
    val MSG_OPENED_REROLLING_MENU = create("msg_opened_rerolling_menu")
    val MSG_ERR_FULL_RECYCLING_STASH_LIST = create("msg_err_full_recycling_stash_list")
    val MSG_ERR_UNSUPPORTED_RECYCLING_ITEM_TYPE = create("msg_err_unsupported_recycling_item_type")
    val MSG_SPENT_X_REPAIRING_ITEM = create("msg_spent_x_repairing_item")
    val MSG_ERR_NOT_ENOUGH_MONEY_TO_REPAIR_ITEM = create("msg_err_not_enough_money_to_repair_item")

    private fun create(key: String): TranslatableComponent.Builder {
        return Component.translatable().key(key)
    }
}