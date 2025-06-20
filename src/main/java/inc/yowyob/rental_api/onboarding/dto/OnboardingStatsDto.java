package inc.yowyob.rental_api.onboarding.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour les statistiques du processus d'onboarding
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnboardingStatsDto {

    private Long totalSessions;
    private Long completedSessions;
    private Long inProgressSessions;
    private Long expiredSessions;
    private Long cancelledSessions;
    private Long failedSessions;

    private Double completionRate; // Pourcentage de completion
    private Double abandonmentRate; // Pourcentage d'abandon

    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;

    // Statistiques par étape
    private Long ownerInfoCompletions;
    private Long organizationInfoCompletions;
    private Long subscriptionCompletions;

    // Temps moyen de completion
    private Double averageCompletionTimeHours;
    private Double averageStepTimeHours;

    // Méthodes calculées
    public Double getAbandonmentRate() {
        if (totalSessions == null || totalSessions == 0) {
            return 0.0;
        }
        Long abandonedSessions = (expiredSessions != null ? expiredSessions : 0) +
            (cancelledSessions != null ? cancelledSessions : 0);
        return (double) abandonedSessions / totalSessions * 100;
    }

    public Double getInProgressRate() {
        if (totalSessions == null || totalSessions == 0) {
            return 0.0;
        }
        return (double) (inProgressSessions != null ? inProgressSessions : 0) / totalSessions * 100;
    }

    public Long getTotalFinishedSessions() {
        return (completedSessions != null ? completedSessions : 0) +
            (expiredSessions != null ? expiredSessions : 0) +
            (cancelledSessions != null ? cancelledSessions : 0) +
            (failedSessions != null ? failedSessions : 0);
    }
}
