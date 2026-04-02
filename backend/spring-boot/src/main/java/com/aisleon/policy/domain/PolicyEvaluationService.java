package com.aisleon.policy.domain;

import com.aisleon.cart.domain.Cart;
import com.aisleon.cart.domain.CartItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain service that evaluates a cart against a purchase policy.
 */
public class PolicyEvaluationService {

    /**
     * Evaluate the cart against the given policy and return the result.
     */
    public PolicyEvaluationResult evaluate(Cart cart, PurchasePolicy policy) {
        List<String> blockedReasons = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        boolean requiresApproval = false;

        // Rule: cart total > maxSpendWithoutApproval → requiresApproval
        if (policy.getMaxSpendWithoutApproval() != null
                && cart.getTotalAmount().compareTo(policy.getMaxSpendWithoutApproval()) > 0) {
            requiresApproval = true;
            warnings.add("Cart total exceeds maximum spend without approval ("
                    + cart.getTotalAmount() + " > " + policy.getMaxSpendWithoutApproval() + ")");
        }

        for (CartItem item : cart.getItems()) {
            // Rule: merchant not approved → blocked
            if (!isMerchantApproved(item, policy)) {
                blockedReasons.add("Item '" + item.getTitle()
                        + "' is from unapproved merchant: " + item.getMerchantName());
            }

            // Rule: substitution not allowed → blocked
            if (item.isSubstitution() && !policy.isAllowSubstitutions()) {
                blockedReasons.add("Substitution not allowed for item: " + item.getTitle());
            }

            // Rule: substitution price delta exceeds limit → requiresApproval
            if (item.isSubstitution() && policy.getMaxSubstitutionPriceDelta() != null) {
                // Use item price as the delta proxy when original price isn't tracked separately
                BigDecimal delta = item.getPrice();
                if (delta.compareTo(policy.getMaxSubstitutionPriceDelta()) > 0) {
                    requiresApproval = true;
                    warnings.add("Substitution price for '" + item.getTitle()
                            + "' exceeds maximum delta (" + policy.getMaxSubstitutionPriceDelta() + ")");
                }
            }
        }

        boolean allowed = blockedReasons.isEmpty();
        return new PolicyEvaluationResult(allowed, requiresApproval, blockedReasons, warnings);
    }

    /**
     * Check merchant approval using merchantId first, falling back to name match.
     */
    private boolean isMerchantApproved(CartItem item, PurchasePolicy policy) {
        if (item.getMerchantId() != null
                && policy.getApprovedMerchantIds().contains(item.getMerchantId())) {
            return true;
        }
        return policy.getApprovedMerchantNames().stream()
                .anyMatch(name -> name.equalsIgnoreCase(item.getMerchantName()));
    }
}
