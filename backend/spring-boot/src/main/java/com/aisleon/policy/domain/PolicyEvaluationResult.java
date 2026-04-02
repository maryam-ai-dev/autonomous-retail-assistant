package com.aisleon.policy.domain;

import java.util.List;

/**
 * Value object representing the outcome of a policy evaluation against a cart.
 */
public class PolicyEvaluationResult {

    private final boolean allowed;
    private final boolean requiresApproval;
    private final List<String> blockedReasons;
    private final List<String> warnings;

    public PolicyEvaluationResult(boolean allowed,
                                  boolean requiresApproval,
                                  List<String> blockedReasons,
                                  List<String> warnings) {
        this.allowed = allowed;
        this.requiresApproval = requiresApproval;
        this.blockedReasons = blockedReasons;
        this.warnings = warnings;
    }

    public boolean isAllowed() { return allowed; }
    public boolean isRequiresApproval() { return requiresApproval; }
    public List<String> getBlockedReasons() { return blockedReasons; }
    public List<String> getWarnings() { return warnings; }
}
