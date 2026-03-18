package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.KoishCommandFactory
import cc.mewcraft.wakame.command.koishHandler
import cc.mewcraft.wakame.monetization.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.paper.util.sender.PlayerSource
import org.incendo.cloud.paper.util.sender.Source
import org.incendo.cloud.parser.standard.EnumParser
import org.incendo.cloud.parser.standard.StringParser
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal object MonetizationCommand : KoishCommandFactory<Source> {

    private val TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault())

    override fun KoishCommandFactory.Builder<Source>.createCommands() {
        val commonBuilder = build {
            permission(CommandPermissions.MONETIZATION)
            literal("pay")
        }

        // <root> pay create <product_name> <amount> <payment_type> <command>
        buildAndAdd(commonBuilder) {
            senderType<PlayerSource>()
            literal("create")
            required("product_name", StringParser.quotedStringParser())
            required("amount", StringParser.quotedStringParser())
            required("payment_type", EnumParser.enumParser(PaymentType::class.java))
            required("command", StringParser.greedyStringParser())
            koishHandler(handler = ::handleCreate)
        }

        // <root> pay query <out_trade_no>
        buildAndAdd(commonBuilder) {
            literal("query")
            required("out_trade_no", StringParser.stringParser())
            koishHandler(handler = ::handleQuery)
        }

        // <root> pay cancel <out_trade_no>
        buildAndAdd(commonBuilder) {
            literal("cancel")
            required("out_trade_no", StringParser.stringParser())
            koishHandler(handler = ::handleCancel)
        }

        // <root> pay expire
        buildAndAdd(commonBuilder) {
            senderType<PlayerSource>()
            literal("expire")
            koishHandler(handler = ::handleExpire)
        }

        // <root> pay list
        buildAndAdd(commonBuilder) {
            senderType<PlayerSource>()
            literal("list")
            koishHandler(handler = ::handleList)
        }
    }

    // ================================================================
    //  /pay create <product_name> <amount> <payment_type> <command>
    // ================================================================

    private suspend fun handleCreate(context: CommandContext<Source>) {
        val player = (context.sender() as PlayerSource).source()
        val productName = context.get<String>("product_name")
        val amount = context.get<String>("amount")
        val paymentType = context.get<PaymentType>("payment_type")
        val command = context.get<String>("command")

        player.sendMessage(
            Component.text("Creating payment order...", NamedTextColor.YELLOW)
        )

        val order: PaymentOrder
        try {
            order = Monetization.createPayment(
                playerId = player.uniqueId,
                playerName = player.name,
                productName = productName,
                amount = amount,
                paymentType = paymentType,
                command = command,
            )
        } catch (e: PaymentException) {
            player.sendMessage(
                Component.text("Failed to create order: ${e.message}", NamedTextColor.RED)
            )
            return
        }

        // Build success message with clickable QR code link
        val header = Component.text("--- Payment Order Created ---", NamedTextColor.GREEN, TextDecoration.BOLD)
        val orderNoLine = Component.text()
            .append(Component.text("Order No: ", NamedTextColor.GRAY))
            .append(
                Component.text(order.outTradeNo, NamedTextColor.WHITE)
                    .clickEvent(ClickEvent.copyToClipboard(order.outTradeNo))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to copy", NamedTextColor.GRAY)))
            )
            .build()
        val productLine = Component.text()
            .append(Component.text("Product: ", NamedTextColor.GRAY))
            .append(
                Component.text(order.productName, NamedTextColor.WHITE)
                    .clickEvent(ClickEvent.copyToClipboard(order.productName))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to copy", NamedTextColor.GRAY)))
            )
            .build()
        val amountLine = Component.text()
            .append(Component.text("Amount: ", NamedTextColor.GRAY))
            .append(
                Component.text("¥${order.amount}", NamedTextColor.GOLD)
                    .clickEvent(ClickEvent.copyToClipboard(order.amount))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to copy", NamedTextColor.GRAY)))
            )
            .build()
        val typeLine = Component.text()
            .append(Component.text("Payment: ", NamedTextColor.GRAY))
            .append(
                Component.text(order.paymentType.name, NamedTextColor.WHITE)
                    .clickEvent(ClickEvent.copyToClipboard(order.paymentType.name))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to copy", NamedTextColor.GRAY)))
            )
            .build()

        val lines = mutableListOf(header, orderNoLine, productLine, amountLine, typeLine)

        // QR code image link (clickable)
        val qrcodeImgUrl = order.qrcodeImgUrl
        if (qrcodeImgUrl != null) {
            lines += Component.text()
                .append(Component.text("QR Code: ", NamedTextColor.GRAY))
                .append(
                    Component.text("[Click to open QR image]", NamedTextColor.AQUA, TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.openUrl(qrcodeImgUrl))
                        .hoverEvent(HoverEvent.showText(Component.text(qrcodeImgUrl, NamedTextColor.WHITE)))
                )
                .build()
        }

        // QR code raw link (clickable)
        val qrcodeUrl = order.qrcodeUrl
        if (qrcodeUrl != null) {
            lines += Component.text()
                .append(Component.text("QR Link: ", NamedTextColor.GRAY))
                .append(
                    Component.text("[Click to open payment link]", NamedTextColor.AQUA, TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.openUrl(qrcodeUrl))
                        .hoverEvent(HoverEvent.showText(Component.text(qrcodeUrl, NamedTextColor.WHITE)))
                )
                .build()
        }

        // Pay URL (clickable)
        val payUrl = order.payUrl
        if (payUrl != null) {
            lines += Component.text()
                .append(Component.text("Pay URL: ", NamedTextColor.GRAY))
                .append(
                    Component.text("[Click to open payment page]", NamedTextColor.AQUA, TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.openUrl(payUrl))
                        .hoverEvent(HoverEvent.showText(Component.text(payUrl, NamedTextColor.WHITE)))
                )
                .build()
        }

        val commandLine = Component.text()
            .append(Component.text("Command: ", NamedTextColor.GRAY))
            .append(
                Component.text(order.command, NamedTextColor.DARK_GRAY)
                    .clickEvent(ClickEvent.copyToClipboard(order.command))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to copy", NamedTextColor.GRAY)))
            )
            .build()
        lines += commandLine

        val footer = Component.text("Scan the QR code to complete payment.", NamedTextColor.YELLOW)
        lines += footer

        player.sendMessage(Component.join(JoinConfiguration.newlines(), lines))
    }

    // ================================================================
    //  /pay query <out_trade_no>
    // ================================================================

    private suspend fun handleQuery(context: CommandContext<Source>) {
        val sender = context.sender().source()
        val outTradeNo = context.get<String>("out_trade_no")

        sender.sendMessage(
            Component.text("Querying order $outTradeNo...", NamedTextColor.YELLOW)
        )

        val order = Monetization.queryPayment(outTradeNo)
        if (order == null) {
            sender.sendMessage(
                Component.text("Order not found: $outTradeNo", NamedTextColor.RED)
            )
            return
        }

        sender.sendMessage(formatOrderDetails(order))
    }

    // ================================================================
    //  /pay cancel <out_trade_no>
    // ================================================================

    private suspend fun handleCancel(context: CommandContext<Source>) {
        val sender = context.sender().source()
        val outTradeNo = context.get<String>("out_trade_no")

        val success = Monetization.cancelPayment(outTradeNo)
        if (success) {
            sender.sendMessage(
                Component.text("Order cancelled: $outTradeNo", NamedTextColor.GREEN)
            )
        } else {
            sender.sendMessage(
                Component.text("Failed to cancel order: $outTradeNo (not found or not pending)", NamedTextColor.RED)
            )
        }
    }

    // ================================================================
    //  /pay expire
    // ================================================================

    private suspend fun handleExpire(context: CommandContext<Source>) {
        val player = (context.sender() as PlayerSource).source()

        val count = Monetization.expireTimeoutOrders(player.uniqueId)
        if (count > 0) {
            player.sendMessage(
                Component.text("Expired $count timeout order(s).", NamedTextColor.GREEN)
            )
        } else {
            player.sendMessage(
                Component.text("No timeout orders to expire.", NamedTextColor.GRAY)
            )
        }
    }

    // ================================================================
    //  /pay list
    // ================================================================

    private suspend fun handleList(context: CommandContext<Source>) {
        val player = (context.sender() as PlayerSource).source()

        val orders = Monetization.findOrdersByPlayer(player.uniqueId)
        if (orders.isEmpty()) {
            player.sendMessage(
                Component.text("You have no orders.", NamedTextColor.GRAY)
            )
            return
        }

        val header = Component.text("--- Your Orders (${orders.size}) ---", NamedTextColor.GREEN, TextDecoration.BOLD)
        val lines = mutableListOf<Component>(header)

        for (order in orders) {
            val statusColor = when (order.status) {
                OrderStatus.PENDING -> NamedTextColor.YELLOW
                OrderStatus.PAID -> NamedTextColor.GREEN
                OrderStatus.EXPIRED -> NamedTextColor.DARK_GRAY
                OrderStatus.FAILED -> NamedTextColor.RED
            }
            val line = Component.text()
                .append(
                    Component.text(" [${order.status}]", statusColor)
                        .clickEvent(ClickEvent.copyToClipboard(order.status.name))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to copy status", NamedTextColor.GRAY)))
                )
                .append(
                    Component.text(" ${order.productName}", NamedTextColor.WHITE)
                        .clickEvent(ClickEvent.copyToClipboard(order.productName))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to copy product", NamedTextColor.GRAY)))
                )
                .append(
                    Component.text(" ¥${order.amount}", NamedTextColor.GOLD)
                        .clickEvent(ClickEvent.copyToClipboard(order.amount))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to copy amount", NamedTextColor.GRAY)))
                )
                .append(
                    Component.text(" (${order.outTradeNo})", NamedTextColor.DARK_GRAY)
                        .clickEvent(ClickEvent.copyToClipboard(order.outTradeNo))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to copy order no", NamedTextColor.GRAY)))
                )
                .hoverEvent(HoverEvent.showText(
                    Component.text()
                        .append(Component.text("Click to query this order", NamedTextColor.AQUA))
                        .append(Component.newline())
                        .append(Component.text("Created: ${TIME_FORMATTER.format(order.createdAt)}", NamedTextColor.GRAY))
                        .build()
                ))
                .clickEvent(ClickEvent.runCommand("/wakame pay query ${order.outTradeNo}"))
                .build()
            lines += line
        }

        player.sendMessage(Component.join(JoinConfiguration.newlines(), lines))
    }

    // ================================================================
    //  Helpers
    // ================================================================

    private fun formatOrderDetails(order: PaymentOrder): Component {
        val statusColor = when (order.status) {
            OrderStatus.PENDING -> NamedTextColor.YELLOW
            OrderStatus.PAID -> NamedTextColor.GREEN
            OrderStatus.EXPIRED -> NamedTextColor.DARK_GRAY
            OrderStatus.FAILED -> NamedTextColor.RED
        }

        val lines = mutableListOf<Component>()
        lines += Component.text("--- Order Details ---", NamedTextColor.GREEN, TextDecoration.BOLD)
        lines += Component.text()
            .append(Component.text("Order No: ", NamedTextColor.GRAY))
            .append(
                Component.text(order.outTradeNo, NamedTextColor.WHITE)
                    .clickEvent(ClickEvent.copyToClipboard(order.outTradeNo))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to copy", NamedTextColor.GRAY)))
            )
            .build()
        if (order.tradeNo != null) {
            lines += Component.text()
                .append(Component.text("Trade No: ", NamedTextColor.GRAY))
                .append(
                    Component.text(order.tradeNo, NamedTextColor.WHITE)
                        .clickEvent(ClickEvent.copyToClipboard(order.tradeNo))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to copy", NamedTextColor.GRAY)))
                )
                .build()
        }
        lines += Component.text()
            .append(Component.text("Product: ", NamedTextColor.GRAY))
            .append(
                Component.text(order.productName, NamedTextColor.WHITE)
                    .clickEvent(ClickEvent.copyToClipboard(order.productName))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to copy", NamedTextColor.GRAY)))
            )
            .build()
        lines += Component.text()
            .append(Component.text("Amount: ", NamedTextColor.GRAY))
            .append(
                Component.text("¥${order.amount}", NamedTextColor.GOLD)
                    .clickEvent(ClickEvent.copyToClipboard(order.amount))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to copy", NamedTextColor.GRAY)))
            )
            .build()
        lines += Component.text()
            .append(Component.text("Payment: ", NamedTextColor.GRAY))
            .append(
                Component.text(order.paymentType.name, NamedTextColor.WHITE)
                    .clickEvent(ClickEvent.copyToClipboard(order.paymentType.name))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to copy", NamedTextColor.GRAY)))
            )
            .build()
        lines += Component.text()
            .append(Component.text("Status: ", NamedTextColor.GRAY))
            .append(
                Component.text(order.status.name, statusColor)
                    .clickEvent(ClickEvent.copyToClipboard(order.status.name))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to copy", NamedTextColor.GRAY)))
            )
            .build()
        lines += Component.text()
            .append(Component.text("Player: ", NamedTextColor.GRAY))
            .append(
                Component.text(order.playerName, NamedTextColor.WHITE)
                    .clickEvent(ClickEvent.copyToClipboard(order.playerName))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to copy", NamedTextColor.GRAY)))
            )
            .build()
        lines += Component.text()
            .append(Component.text("Command: ", NamedTextColor.GRAY))
            .append(
                Component.text(order.command, NamedTextColor.DARK_GRAY)
                    .clickEvent(ClickEvent.copyToClipboard(order.command))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to copy", NamedTextColor.GRAY)))
            )
            .build()
        lines += Component.text()
            .append(Component.text("Created: ", NamedTextColor.GRAY))
            .append(
                Component.text(TIME_FORMATTER.format(order.createdAt), NamedTextColor.WHITE)
                    .clickEvent(ClickEvent.copyToClipboard(TIME_FORMATTER.format(order.createdAt)))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to copy", NamedTextColor.GRAY)))
            )
            .build()
        if (order.paidAt != null) {
            lines += Component.text()
                .append(Component.text("Paid at: ", NamedTextColor.GRAY))
                .append(
                    Component.text(TIME_FORMATTER.format(order.paidAt), NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.copyToClipboard(TIME_FORMATTER.format(order.paidAt)))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to copy", NamedTextColor.GRAY)))
                )
                .build()
        }

        // Append clickable links if still pending
        if (order.status == OrderStatus.PENDING) {
            order.qrcodeImgUrl?.let { url ->
                lines += Component.text()
                    .append(Component.text("QR Code: ", NamedTextColor.GRAY))
                    .append(
                        Component.text("[Click to open QR image]", NamedTextColor.AQUA, TextDecoration.UNDERLINED)
                            .clickEvent(ClickEvent.openUrl(url))
                            .hoverEvent(HoverEvent.showText(Component.text(url, NamedTextColor.WHITE)))
                    )
                    .build()
            }
            order.qrcodeUrl?.let { url ->
                lines += Component.text()
                    .append(Component.text("QR Link: ", NamedTextColor.GRAY))
                    .append(
                        Component.text("[Click to open payment link]", NamedTextColor.AQUA, TextDecoration.UNDERLINED)
                            .clickEvent(ClickEvent.openUrl(url))
                            .hoverEvent(HoverEvent.showText(Component.text(url, NamedTextColor.WHITE)))
                    )
                    .build()
            }
            order.payUrl?.let { url ->
                lines += Component.text()
                    .append(Component.text("Pay URL: ", NamedTextColor.GRAY))
                    .append(
                        Component.text("[Click to open payment page]", NamedTextColor.AQUA, TextDecoration.UNDERLINED)
                            .clickEvent(ClickEvent.openUrl(url))
                            .hoverEvent(HoverEvent.showText(Component.text(url, NamedTextColor.WHITE)))
                    )
                    .build()
            }
        }

        return Component.join(JoinConfiguration.newlines(), lines)
    }
}