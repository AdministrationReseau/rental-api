package inc.yowyob.rental_api.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation pour vérifier qu'un utilisateur a accès à une agence spécifique
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAgencyAccess {

    /**
     * Nom du paramètre contenant l'ID de l'agence
     */
    String agencyIdParam() default "agencyId";

    /**
     * Message d'erreur personnalisé
     */
    String message() default "Accès à l'agence non autorisé";

    /**
     * Indique si les gestionnaires d'organisation contournent cette vérification
     */
    boolean allowOrganizationManager() default true;
}
