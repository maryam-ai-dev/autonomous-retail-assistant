package com.aisleon.basket;

import com.aisleon.catalogue.Retailer;
import java.util.List;

public record TasteProfile(
        boolean halalOnly,
        boolean veganOnly,
        boolean vegetarianOnly,
        List<Retailer> retailerAllowList,
        List<Retailer> retailerDenyList,
        List<String> preferredBrands) {
    public TasteProfile {
        if (retailerAllowList == null) retailerAllowList = List.of();
        if (retailerDenyList == null) retailerDenyList = List.of();
        if (preferredBrands == null) preferredBrands = List.of();
    }

    public static TasteProfile empty() {
        return new TasteProfile(false, false, false, List.of(), List.of(), List.of());
    }
}
