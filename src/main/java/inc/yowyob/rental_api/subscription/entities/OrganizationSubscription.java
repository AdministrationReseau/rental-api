package inc.yowyob.rental_api.subscription.entities;

import inc.yowyob.rental_api.core.enums.SubscriptionStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table("organization_subscriptions")
public class OrganizationSubscription {

    @Id
    @PrimaryKey
    private UUID id;

    @NotNull(message = "Organization ID is required")
    private UUID organizationId;

    @NotNull(message = "Subscription plan ID is required")
    private UUID subscriptionPlanId;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    @NotNull(message = "Status is required")
    private SubscriptionStatus status;

    @PositiveOrZero(message = "Amount paid must be positive or zero")
    private BigDecimal amountPaid;

    private String paymentTransactionId;
    private String paymentMethod;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Auto-renewal configuration
    private Boolean autoRenewal = false;
    private UUID nextSubscriptionPlanId; // For plan changes

    // Trial specific fields
    private Boolean isTrial = false;
    private LocalDateTime trialStartDate;
    private LocalDateTime trialEndDate;

    // Constructors
    public OrganizationSubscription(UUID organizationId, UUID subscriptionPlanId,
                                    LocalDateTime startDate, LocalDateTime endDate) {
        this.id = UUID.randomUUID();
        this.organizationId = organizationId;
        this.subscriptionPlanId = subscriptionPlanId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = SubscriptionStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Business methods
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return status == SubscriptionStatus.ACTIVE &&
            now.isAfter(startDate) &&
            now.isBefore(endDate);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(endDate) ||
            status == SubscriptionStatus.EXPIRED;
    }

    public boolean isInTrial() {
        if (!Boolean.TRUE.equals(isTrial)) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return trialStartDate != null && trialEndDate != null &&
            now.isAfter(trialStartDate) && now.isBefore(trialEndDate);
    }

    public long getDaysUntilExpiry() {
        if (endDate == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), endDate);
    }

    public boolean isExpiringIn(int days) {
        return getDaysUntilExpiry() <= days && getDaysUntilExpiry() > 0;
    }

    public void extendSubscription(int additionalDays) {
        if (endDate != null) {
            this.endDate = endDate.plusDays(additionalDays);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void cancel() {
        this.status = SubscriptionStatus.CANCELLED;
        this.autoRenewal = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void suspend() {
        this.status = SubscriptionStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now();
    }

    public void reactivate() {
        if (status == SubscriptionStatus.SUSPENDED) {
            this.status = SubscriptionStatus.ACTIVE;
            this.updatedAt = LocalDateTime.now();
        }
    }
}
