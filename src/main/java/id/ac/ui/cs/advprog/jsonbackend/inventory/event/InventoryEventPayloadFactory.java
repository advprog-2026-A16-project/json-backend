package id.ac.ui.cs.advprog.jsonbackend.inventory.event;

import java.util.UUID;

public final class InventoryEventPayloadFactory {

    private InventoryEventPayloadFactory() {
    }

    public static String stockMutationPayload(UUID productId, int quantity) {
        return "{\"productId\":\"" + productId + "\",\"quantity\":" + quantity + "}";
    }
}
