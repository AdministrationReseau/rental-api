package inc.yowyob.rental_api.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation pour vérifier qu'un utilisateur a accès à une organisation spécifique
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireOrganizationAccess {

    /**
     * Nom du paramètre contenant l'ID de l'organisation
     */
    String organizationIdParam() default "organizationId";

    /**
     * Message d'erreur personnalisé
     */
    String message() default "Accès à l'organisation non autorisé";

    /**
     * Indique si les super admins contournent cette vérification
     */
    boolean allowSuperAdmin() default true;
}
