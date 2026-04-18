package com.aisleon.catalogue;

import java.util.Arrays;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalogue/meta")
public class CatalogueMetaController {

    @GetMapping("/retailers")
    public List<Retailer> retailers() {
        return Arrays.asList(Retailer.values());
    }

    @GetMapping("/dietary-tags")
    public List<DietaryTag> dietaryTags() {
        return Arrays.asList(DietaryTag.values());
    }

    @GetMapping("/certifications")
    public List<CertificationTag> certifications() {
        return Arrays.asList(CertificationTag.values());
    }

    @GetMapping("/offer-flags")
    public List<OfferFlag> offerFlags() {
        return Arrays.asList(OfferFlag.values());
    }

    @GetMapping("/categories")
    public List<ProductCategory> categories() {
        return Arrays.asList(ProductCategory.values());
    }

    @GetMapping("/subcategories")
    public List<ProductSubcategory> subcategories() {
        return Arrays.asList(ProductSubcategory.values());
    }
}
