package com.aisleon.basket;

import com.aisleon.catalogue.NormalizedProduct;
import java.util.Optional;

public record BasketItem(
        String id,
        NormalizedProduct product,
        int quantity,
        Optional<SubstitutionFlag> substitutionFlag) {
    public BasketItem {
        if (substitutionFlag == null) substitutionFlag = Optional.empty();
        if (quantity < 1) quantity = 1;
    }
}
