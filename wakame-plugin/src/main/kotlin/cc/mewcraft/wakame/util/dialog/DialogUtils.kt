package cc.mewcraft.wakame.util.dialog

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.input.TextDialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import org.bukkit.entity.Player
import java.time.Duration

object DialogUtils {

    fun getPlayerTextInput(
        player: Player,
        noBtnLabel: Component = Component.text("取消"),
        yseBtnLabel: Component = Component.text("确认"),
        inputWidth: Int = 200,
        inputLabel: Component = Component.text("文本输入框"),
        inputLabelVisible: Boolean = true,
        inputInitial: String = "",
        inputMaxLength: Int = 32,
        inputMultilineOptions: TextDialogInput.MultilineOptions? = null,
        noAction: (audience: Audience, textInput: List<String>) -> Unit = { _, _ -> },
        yesAction: (audience: Audience, textInput: List<String>) -> Unit,
    ) {
        // 构建 Dialog
        val dialog = Dialog.create { builder ->
            builder
                .empty()
                .type(
                    DialogType.confirmation(
                        ActionButton
                            .builder(yseBtnLabel)
                            .action(
                                DialogAction.customClick(
                                    x@{ view, audience ->
                                        val text = view.getText("text") ?: return@x
                                        yesAction(audience, text.split("\n"))
                                    },
                                    ClickCallback.Options.builder()
                                        .lifetime(Duration.ofMinutes(30))
                                        .uses(1)
                                        .build()
                                )
                            )
                            .build(),
                        ActionButton.builder(noBtnLabel)
                            .action(
                                DialogAction.customClick(
                                    x@{ view, audience ->
                                        val text = view.getText("text") ?: return@x
                                        noAction(audience, text.split("\n"))
                                        audience.closeDialog()
                                    },
                                    ClickCallback.Options.builder()
                                        .lifetime(Duration.ofSeconds(1))
                                        .uses(1)
                                        .build()
                                )
                            )
                            .build()
                    )
                )
                .base(
                    DialogBase.builder(Component.text("完成以下表单"))
                        .canCloseWithEscape(false)
                        .inputs(
                            listOf(
                                DialogInput.text(
                                    "text",
                                    inputWidth,
                                    inputLabel,
                                    inputLabelVisible,
                                    inputInitial,
                                    inputMaxLength,
                                    inputMultilineOptions
                                )
                            )
                        )
                        .build()
                )

        }
        // 显示 Dialog
        player.showDialog(dialog)
    }
}