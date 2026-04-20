package com.aisleon.shared_baskets.api;

import com.aisleon.basket.ClothingProfile;
import com.aisleon.basket.TasteProfile;
import com.aisleon.basket.api.BasketDto;
import com.aisleon.basket.infrastructure.BasketJpaEntity;
import com.aisleon.catalogue.Retailer;
import com.aisleon.preferences.application.TasteProfileService;
import com.aisleon.preferences.interfaces.TasteProfileResponse;
import com.aisleon.shared_baskets.application.SharedBasketService;
import com.aisleon.shared_baskets.infrastructure.SharedBasketJpaEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/baskets")
@Tag(name = "Shared baskets")
public class SharedBasketController {

    private final SharedBasketService sharedBasketService;
    private final TasteProfileService tasteProfileService;
    private final String shareBaseUrl;

    public SharedBasketController(
            SharedBasketService sharedBasketService,
            TasteProfileService tasteProfileService,
            @Value("${aisleon.share.base-url:http://localhost:8080/baskets/shared}")
                    String shareBaseUrl) {
        this.sharedBasketService = sharedBasketService;
        this.tasteProfileService = tasteProfileService;
        this.shareBaseUrl = stripTrailingSlash(shareBaseUrl);
    }

    @Operation(
            summary = "Share an APPROVED basket and return its public share id + URL",
            description =
                    "Idempotent — sharing a basket that already has a share returns the"
                            + " existing shareId. 404 if the basket does not exist.")
    @PostMapping("/{id}/share")
    public ResponseEntity<ShareBasketResponse> share(@PathVariable("id") UUID basketId) {
        SharedBasketJpaEntity shared = sharedBasketService.share(basketId);
        return ResponseEntity.ok(ShareBasketResponse.builder()
                .shareId(shared.getShareId())
                .shareUrl(shareBaseUrl + "/" + shared.getShareId())
                .build());
    }

    @Operation(
            summary = "Public read-only view of a shared basket — no authentication",
            description =
                    "Returns the full basket including item dietary tags. 404 if the share"
                            + " id is unknown.")
    @GetMapping("/shared/{shareId}")
    public ResponseEntity<BasketDto> getShared(@PathVariable("shareId") String shareId) {
        BasketJpaEntity basket = sharedBasketService.loadBasketByShareId(shareId);
        return ResponseEntity.ok(BasketDto.fromEntity(basket));
    }

    @Operation(
            summary = "Fork a shared basket — creates a new basket for the calling user",
            description =
                    "Re-runs the full basket generation flow against the caller's own taste"
                            + " profile using the original intent text. Returns the new"
                            + " basket id. 404 if the share id is unknown.")
    @PostMapping("/shared/{shareId}/fork")
    public ResponseEntity<ForkBasketResponse> fork(
            Authentication auth, @PathVariable("shareId") String shareId) {
        UUID forkerUserId = UUID.fromString(auth.getPrincipal().toString());
        TasteProfile taste = loadTasteProfile(forkerUserId);
        ClothingProfile clothing = loadClothingProfile(forkerUserId);
        BasketJpaEntity newBasket =
                sharedBasketService.fork(shareId, forkerUserId, taste, clothing);
        return ResponseEntity.ok(
                ForkBasketResponse.builder().basketId(newBasket.getId()).build());
    }

    private TasteProfile loadTasteProfile(UUID userId) {
        TasteProfileResponse profile = tasteProfileService.getOrCreate(userId);
        return new TasteProfile(
                Boolean.TRUE.equals(profile.getHalalOnly()),
                Boolean.TRUE.equals(profile.getVeganOnly()),
                Boolean.TRUE.equals(profile.getVegetarianOnly()),
                parseRetailers(profile.getRetailerAllowList()),
                parseRetailers(profile.getRetailerDenyList()),
                profile.getPreferredBrands() == null ? List.of() : profile.getPreferredBrands());
    }

    private ClothingProfile loadClothingProfile(UUID userId) {
        TasteProfileResponse profile = tasteProfileService.getOrCreate(userId);
        BigDecimal shoeSize = profile.getShoeSizeUk();
        return new ClothingProfile(
                profile.getTopSize(),
                profile.getBottomSize(),
                shoeSize == null ? null : shoeSize.toPlainString(),
                profile.getDressSize(),
                ClothingProfile.SizePreference.parse(profile.getSizePreference()));
    }

    private static List<Retailer> parseRetailers(List<String> names) {
        if (names == null) return List.of();
        List<Retailer> out = new ArrayList<>();
        for (String name : names) {
            try {
                out.add(Retailer.valueOf(name));
            } catch (IllegalArgumentException ignored) {
                // unknown retailer name in stored profile — drop silently
            }
        }
        return out;
    }

    private static String stripTrailingSlash(String url) {
        if (url == null || url.isEmpty()) return url;
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
