package id.ac.ui.cs.advprog.jsonbackend.order.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderRatingRequest {
    @NotNull(message = "Jastiper rating is required")
    @Min(1) @Max(5)
    private Integer jastiperRating;

    @NotNull(message = "Product rating is required")
    @Min(1) @Max(5)
    private Integer productRating;

    private String reviewNotes;
}