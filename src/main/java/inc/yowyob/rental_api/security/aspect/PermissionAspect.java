package inc.yowyob.rental_api.security.aspect;

import inc.yowyob.rental_api.security.annotations.*;
import inc.yowyob.rental_api.security.service.PermissionEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.UUID;

/**
 * Aspect pour l'évaluation automatique des permissions via les annotations
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    private final PermissionEvaluationService permissionEvaluationService;

    @Around("@annotation(requirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        log.debug("Checking permission: {} for method: {}",
            requirePermission.value(), joinPoint.getSignature().getName());

        boolean hasPermission;

        if (requirePermission.checkOrganization() || requirePermission.checkAgency()) {
            // Extraire les IDs d'organisation et d'agence des paramètres
            Object[] args = joinPoint.getArgs();
            Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            Parameter[] parameters = method.getParameters();

            UUID organizationId = extractParameterValue(args, parameters, "organizationId", UUID.class);
            UUID agencyId = requirePermission.checkAgency() ?
                extractParameterValue(args, parameters, "agencyId", UUID.class) : null;

            hasPermission = permissionEvaluationService.hasPermissionInContext(
                requirePermission.value(), organizationId, agencyId);
        } else {
            hasPermission = permissionEvaluationService.hasPermission(requirePermission.value());
        }

        if (!hasPermission) {
            log.warn("Access denied - Missing permission: {} for method: {}",
                requirePermission.value(), joinPoint.getSignature().getName());
            throw new AccessDeniedException(requirePermission.message());
        }

        return joinPoint.proceed();
    }

    @Around("@annotation(requireAnyPermission)")
    public Object checkAnyPermission(ProceedingJoinPoint joinPoint, RequireAnyPermission requireAnyPermission) throws Throwable {
        log.debug("Checking any permission: {} for method: {}",
            String.join(", ", requireAnyPermission.value()), joinPoint.getSignature().getName());

        boolean hasAnyPermission = permissionEvaluationService.hasAnyPermission(requireAnyPermission.value());

        if (!hasAnyPermission) {
            log.warn("Access denied - Missing any required permission for method: {}",
                joinPoint.getSignature().getName());
            throw new AccessDeniedException(requireAnyPermission.message());
        }

        return joinPoint.proceed();
    }

    @Around("@annotation(requireAllPermissions)")
    public Object checkAllPermissions(ProceedingJoinPoint joinPoint, RequireAllPermissions requireAllPermissions) throws Throwable {
        log.debug("Checking all permissions: {} for method: {}",
            String.join(", ", requireAllPermissions.value()), joinPoint.getSignature().getName());

        boolean hasAllPermissions = permissionEvaluationService.hasAllPermissions(requireAllPermissions.value());

        if (!hasAllPermissions) {
            log.warn("Access denied - Missing required permissions for method: {}",
                joinPoint.getSignature().getName());
            throw new AccessDeniedException(requireAllPermissions.message());
        }

        return joinPoint.proceed();
    }

    @Around("@annotation(requireRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        log.debug("Checking role: {} for method: {}",
            requireRole.value(), joinPoint.getSignature().getName());

        boolean hasRole = permissionEvaluationService.hasRole(requireRole.value());

        if (!hasRole) {
            log.warn("Access denied - Missing required role: {} for method: {}",
                requireRole.value(), joinPoint.getSignature().getName());
            throw new AccessDeniedException(requireRole.message());
        }

        return joinPoint.proceed();
    }

    @Around("@annotation(requireOrganizationAccess)")
    public Object checkOrganizationAccess(ProceedingJoinPoint joinPoint, RequireOrganizationAccess requireOrganizationAccess) throws Throwable {
        log.debug("Checking organization access for method: {}", joinPoint.getSignature().getName());

        // Extraire l'ID d'organisation des paramètres
        Object[] args = joinPoint.getArgs();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Parameter[] parameters = method.getParameters();

        UUID organizationId = extractParameterValue(args, parameters,
            requireOrganizationAccess.organizationIdParam(), UUID.class);

        if (organizationId == null) {
            log.warn("Organization ID parameter not found: {}", requireOrganizationAccess.organizationIdParam());
            throw new AccessDeniedException("Organization ID required");
        }

        // Vérifier l'accès (super admin contourne si autorisé)
        boolean hasAccess = (requireOrganizationAccess.allowSuperAdmin() && permissionEvaluationService.isSuperAdmin()) ||
            permissionEvaluationService.hasOrganizationAccess(organizationId);

        if (!hasAccess) {
            log.warn("Access denied - No access to organization: {} for method: {}",
                organizationId, joinPoint.getSignature().getName());
            throw new AccessDeniedException(requireOrganizationAccess.message());
        }

        return joinPoint.proceed();
    }

    @Around("@annotation(requireAgencyAccess)")
    public Object checkAgencyAccess(ProceedingJoinPoint joinPoint, RequireAgencyAccess requireAgencyAccess) throws Throwable {
        log.debug("Checking agency access for method: {}", joinPoint.getSignature().getName());

        // Extraire l'ID d'agence des paramètres
        Object[] args = joinPoint.getArgs();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Parameter[] parameters = method.getParameters();

        UUID agencyId = extractParameterValue(args, parameters,
            requireAgencyAccess.agencyIdParam(), UUID.class);

        if (agencyId == null) {
            log.warn("Agency ID parameter not found: {}", requireAgencyAccess.agencyIdParam());
            throw new AccessDeniedException("Agency ID required");
        }

        // Vérifier l'accès
        boolean hasAccess = (requireAgencyAccess.allowOrganizationManager() && permissionEvaluationService.isOrganizationOwner()) ||
            permissionEvaluationService.hasAgencyAccess(agencyId);

        if (!hasAccess) {
            log.warn("Access denied - No access to agency: {} for method: {}",
                agencyId, joinPoint.getSignature().getName());
            throw new AccessDeniedException(requireAgencyAccess.message());
        }

        return joinPoint.proceed();
    }

    /**
     * Extrait la valeur d'un paramètre par nom
     */
    @SuppressWarnings("unchecked")
    private <T> T extractParameterValue(Object[] args, Parameter[] parameters, String parameterName, Class<T> expectedType) {
        for (int i = 0; i < parameters.length && i < args.length; i++) {
            if (parameterName.equals(parameters[i].getName()) && args[i] != null) {
                if (expectedType.isAssignableFrom(args[i].getClass())) {
                    return (T) args[i];
                }
            }
        }
        return null;
    }
}
