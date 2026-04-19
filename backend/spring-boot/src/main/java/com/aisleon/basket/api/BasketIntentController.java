package com.aisleon.basket.api;

import com.aisleon.basket.ClothingProfile;
import com.aisleon.basket.TasteProfile;
import com.aisleon.basket.application.BasketOrchestrationService;
import com.aisleon.basket.infrastructure.BasketJpaEntity;
import com.aisleon.catalogue.Retailer;
import com.aisleon.preferences.application.TasteProfileService;
import com.aisleon.preferences.interfaces.TasteProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/basket-intent")
@Tag(name = "Basket intent")
public class BasketIntentController {

    private final BasketOrchestrationService orchestration;
    private final TasteProfileService tasteProfileService;

    public BasketIntentController(
            BasketOrchestrationService orchestration,
            TasteProfileService tasteProfileService) {
        this.orchestration = orchestration;
        this.tasteProfileService = tasteProfileService;
    }

    @Operation(
            summary = "Submit a free-text basket intent and receive a DRAFT basket",
            description =
                    "Returns 422 with { reason: BUDGET_TOO_LOW_FOR_INTENT } if the budget"
                            + " is too low to assemble any basket; 428 with"
                            + " { reason: CLOTHING_PROFILE_INCOMPLETE } for fashion intents"
                            + " when the user's clothing profile is incomplete.")
    @PostMapping("/submit")
    public ResponseEntity<BasketDto> submit(
            Authentication auth,
            @Valid @RequestBody SubmitBasketIntentRequest request) {
        UUID userId = UUID.fromString(auth.getPrincipal().toString());
        TasteProfile taste = loadTasteProfile(userId);
        ClothingProfile clothing = loadClothingProfile(userId);
        BasketJpaEntity basket = orchestration.submit(userId, request.getText(), taste, clothing);
        return ResponseEntity.ok(BasketDto.fromEntity(basket));
    }

    private TasteProfile loadTasteProfile(UUID userId) {
        TasteProfileResponse profile = tasteProfileService.getOrCreate(userId);
        return new TasteProfile(
                Boolean.TRUE.equals(profile.getHalalOnly()),
                Boolean.TRUE.equals(profile.getVeganOnly()),
                Boolean.TRUE.equals(profile.getVegetarianOnly()),
                parseRetailers(profile.getRetailerAllowList()),
                parseRetailers(profile.getRetailerDenyList()),
                profile.getPreferredBrands() == null
                        ? List.of()
                        : profile.getPreferredBrands());
    }

    private ClothingProfile loadClothingProfile(UUID userId) {
        TasteProfileResponse profile = tasteProfileService.getOrCreate(userId);
        BigDecimal shoeSize = profile.getShoeSizeUk();
        return new ClothingProfile(
                profile.getTopSize(),
                profile.getBottomSize(),
                shoeSize == null ? null : shoeSize.toPlainString(),
                profile.getDressSize());
    }

    private static List<Retailer> parseRetailers(List<String> names) {
        if (names == null) return List.of();
        List<Retailer> out = new ArrayList<>();
        for (String name : names) {
            try {
                out.add(Retailer.valueOf(name));
            } catch (IllegalArgumentException ignored) {
                // drop unknown retailer names rather than fail the whole flow
            }
        }
        return out;
    }
}
