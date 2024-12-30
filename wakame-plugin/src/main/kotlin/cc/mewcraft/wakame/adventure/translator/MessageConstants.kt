package cc.mewcraft.wakame.adventure.translator

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent

object MessageConstants {
    val MSG_ERR_CANCELLED = create("msg_err_cancelled")
    val MSG_ERR_INTERNAL_ERROR = create("msg_err_internal_error")
    val MSG_OPENED_MERGING_MENU = create("msg_opened_merging_menu")
    val MSG_OPENED_MODDING_MENU = create("msg_opened_modding_menu")
    val MSG_OPENED_REROLLING_MENU = create("msg_opened_rerolling_menu")
    val MSG_OPENED_BLACKSMITH_MENU = create("msg_opened_blacksmith_menu")
    val MSG_ERR_FULL_RECYCLING_STASH_LIST = create("msg_err_full_recycling_stash_list")
    val MSG_ERR_UNSUPPORTED_RECYCLING_ITEM_TYPE = create("msg_err_unsupported_recycling_item_type")
    val MSG_SPENT_X_REPAIRING_ITEM = create("msg_spent_x_repairing_item")
    val MSG_ERR_NOT_ENOUGH_MONEY_TO_REPAIR_ITEM = create("msg_err_not_enough_money_to_repair_item")
    val MSG_SOLD_ITEMS_FOR_X_COINS = create("msg_sold_items_for_x_coins")
    val MSG_ERR_NOT_AUGMENT_CORE = create("msg_err_not_augment_core")
    val MSG_SNEAK_TO_BREAK_ARMOR_STAND = create("msg_sneak_to_break_armor_stand")
    val MSG_ERR_NOT_INSIDE_TOWN = create("msg_err_not_inside_town")
    val MSG_ERR_WORLD_WEATHER_CONTROL_NOT_READY = create("msg_err_world_weather_control_not_ready")
    val MSG_ERR_WORLD_TIME_CONTROL_NOT_READY = create("msg_err_world_time_control_not_ready")
    val MSG_WORLD_TIME_CONTROL_EXECUTED = create("msg_world_time_control_executed")
    val MSG_WORLD_WEATHER_CONTROL_EXECUTED = create("msg_world_weather_control_executed")
    val MSG_MERGING_COST_ZERO = create("msg_merging_cost_zero")
    val MSG_MERGING_COST_EMPTY = create("msg_merging_cost_empty")
    val MSG_MERGING_COST_SUCCESS = create("msg_merging_cost_success")
    val MSG_MERGING_TYPE_SUCCESS_0 = create("msg_merging_type_success_0")
    val MSG_MERGING_TYPE_SUCCESS_1 = create("msg_merging_type_success_1")
    val MSG_MERGING_TYPE_SUCCESS_2 = create("msg_merging_type_success_2")
    val MSG_MERGING_TYPE_EMPTY = create("msg_merging_type_empty")
    val MSG_MERGING_TYPE_FAILED = create("msg_merging_type_failed")
    val MSG_MERGING_RESULT_EMPTY = create("msg_merging_result_empty")
    val MSG_MERGING_RESULT_SUCCESS = create("msg_merging_result_success")
    val MSG_MERGING_RESULT_FROZEN_SESSION = create("msg_merging_result_frozen_session")
    val MSG_MERGING_RESULT_BAD_INPUT_1 = create("msg_merging_result_bad_input_1")
    val MSG_MERGING_RESULT_BAD_INPUT_2 = create("msg_merging_result_bad_input_2")
    val MSG_MERGING_RESULT_INPUTS_NOT_SIMILAR = create("msg_merging_result_inputs_not_similar")
    val MSG_MERGING_RESULT_UNACCEPTABLE_TYPE = create("msg_merging_result_unacceptable_type")
    val MSG_MERGING_RESULT_LEVEL_TOO_HIGH = create("msg_merging_result_level_too_high")
    val MSG_MERGING_RESULT_PENALTY_TOO_HIGH = create("msg_merging_result_penalty_too_high")
    val MSG_MERGING_RESULT_NON_MERGEABLE_TYPE = create("msg_merging_result_non_mergeable_type")
    val MSG_MODDING_RESULT_EMPTY = create("msg_modding_result_empty")
    val MSG_MODDING_RESULT_SUCCESS = create("msg_modding_result_success")
    val MSG_MODDING_RESULT_FAILURE_FOR_NON_LEGIT_SOURCE_ITEM = create("msg_modding_result_failure_for_non_legit_source_item")
    val MSG_MODDING_RESULT_FAILURE_FOR_ITEM_RULE_NOT_FOUND = create("msg_modding_result_failure_for_item_rule_not_found")
    val MSG_MODDING_RESULT_FAILURE_FOR_ALL_CORES_NOT_APPLICABLE = create("msg_modding_result_failure_for_all_cores_not_applicable")
    val MSG_MODDING_RESULT_FAILURE_FOR_SOME_CORE_NOT_APPLICABLE = create("msg_modding_result_failure_for_some_core_not_applicable")
    val MSG_MODDING_RESULT_FAILURE_FOR_REACHING_MOD_COUNT_LIMIT = create("msg_modding_result_failure_for_reaching_mod_count_limit")
    val MSG_MODDING_RESULT_FAILURE_FOR_NO_CELLS_FOUND_IN_SOURCE_ITEM = create("msg_modding_result_failure_for_no_cells_found_in_source_item")
    val MSG_MODDING_COST_EMPTY = create("msg_modding_cost_empty")
    val MSG_MODDING_COST_SIMPLE = create("msg_modding_cost_simple")
    val MSG_MODDING_REPLACE_RESULT_EMPTY = create("msg_modding_replace_result_empty")
    val MSG_MODDING_REPLACE_RESULT_SUCCESS = create("msg_modding_replace_result_success")
    val MSG_MODDING_REPLACE_RESULT_UNCHANGEABLE = create("msg_modding_replace_result_unchangeable")
    val MSG_MODDING_REPLACE_RESULT_NOT_PORTABLE_CORE = create("msg_modding_replace_result_not_portable_core")
    val MSG_MODDING_REPLACE_RESULT_SIMILAR_CORE_PRESENT_ON_TARGET = create("msg_modding_replace_result_similar_core_present_on_target")
    val MSG_MODDING_REPLACE_RESULT_SIMILAR_CORE_PRESENT_ON_INPUT = create("msg_modding_replace_result_similar_core_present_on_input")
    val MSG_MODDING_REPLACE_RESULT_CORE_INCOMPATIBLE_WITH_CELL = create("msg_modding_replace_result_core_incompatible_with_cell")
    val MSG_MODDING_REPLACE_RESULT_CORE_ELEMENT_INCOMPATIBLE_WITH_TARGET = create("msg_modding_replace_result_core_element_incompatible_with_target")
    val MSG_MODDING_REPLACE_RESULT_TARGET_REACH_MOD_COUNT_LIMIT = create("msg_modding_replace_result_target_reach_mod_count_limit")
    val MSG_REROLLING_RESULT_EMPTY = create("msg_rerolling_result_empty")
    val MSG_REROLLING_RESULT_SUCCESS = create("msg_rerolling_result_success")
    val MSG_REROLLING_RESULT_FAILURE_INPUT_NOT_USABLE = create("msg_rerolling_result_failure_input_not_usable")
    val MSG_REROLLING_RESULT_FAILURE_ITEM_RULE_NOT_FOUND = create("msg_rerolling_result_failure_item_rule_not_found")
    val MSG_REROLLING_RESULT_FAILURE_NOTHING_CHANGEABLE = create("msg_rerolling_result_failure_nothing_changeable")
    val MSG_REROLLING_RESULT_FAILURE_NOTHING_SELECTED = create("msg_rerolling_result_failure_nothing_selected")
    val MSG_REROLLING_RESULT_FAILURE_INPUT_WITHOUT_LEVEL = create("msg_rerolling_result_failure_input_without_level")
    val MSG_REROLLING_RESULT_FAILURE_INPUT_WITHOUT_CELLS = create("msg_rerolling_result_failure_input_without_cells")
    val MSG_REROLLING_RESULT_FAILURE_INPUT_REACH_MOD_COUNT_LIMIT = create("msg_rerolling_result_failure_input_reach_mod_count_limit")
    val MSG_REROLLING_COST_EMPTY = create("msg_rerolling_cost_empty")
    val MSG_REROLLING_COST_SIMPLE = create("msg_rerolling_cost_simple")

    private fun create(key: String): TranslatableComponent.Builder {
        return Component.translatable().key(key)
    }
}