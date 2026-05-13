package id.ac.ui.cs.advprog.jsonbackend.order.event;

import java.math.BigDecimal;
import java.util.UUID;

public final class OrderEventPayloadFactory {

    private OrderEventPayloadFactory() {
    }

    public static String orderCreatedPayload(UUID orderId, UUID productId, int quantity, UUID titipersId, BigDecimal totalPrice) {
        return String.format("{\"orderId\":\"%s\",\"productId\":\"%s\",\"quantity\":%d,\"titipersId\":\"%s\",\"totalPrice\":%s}",
                orderId, productId, quantity, titipersId, totalPrice);
    }

    public static String orderCancelledPayload(UUID orderId, UUID productId, int quantity, UUID titipersId, BigDecimal totalPrice) {
        return String.format("{\"orderId\":\"%s\",\"productId\":\"%s\",\"quantity\":%d,\"titipersId\":\"%s\",\"totalPrice\":%s}",
                orderId, productId, quantity, titipersId, totalPrice);
    }

    public static String orderRatedPayload(UUID orderId, UUID jastiperId, Integer jastiperRating, Integer productRating) {
        return String.format("{\"orderId\":\"%s\",\"jastiperId\":\"%s\",\"jastiperRating\":%d,\"productRating\":%d}",
                orderId, jastiperId, jastiperRating, productRating);
    }
}