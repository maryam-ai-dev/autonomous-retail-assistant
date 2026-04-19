package com.aisleon.basket.infrastructure;

import com.aisleon.catalogue.Retailer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "basket_items")
public class BasketItemJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "basket_id", nullable = false)
    @ToString.Exclude
    private BasketJpaEntity basket;

    @Column(name = "external_product_id", nullable = false)
    private String externalProductId;

    @Enumerated(EnumType.STRING)
    @Column(name = "retailer", nullable = false)
    private Retailer retailer;

    @Column(name = "canonical_name", nullable = false)
    private String canonicalName;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "brand")
    private String brand;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "product_url")
    private String productUrl;

    @Column(name = "reasoning", columnDefinition = "TEXT")
    private String reasoning;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "dietary_tags")
    @Builder.Default
    private List<String> dietaryTags = new ArrayList<>();

    @Column(name = "substitution_flag_type")
    private String substitutionFlagType;

    @Column(name = "substitution_flag_reason", columnDefinition = "TEXT")
    private String substitutionFlagReason;

    @Column(name = "substitution_flag_resolved", nullable = false)
    @Builder.Default
    private Boolean substitutionFlagResolved = false;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
