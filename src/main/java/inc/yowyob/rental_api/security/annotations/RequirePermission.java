package inc.yowyob.rental_api.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation pour vérifier qu'un utilisateur possède une permission spécifique
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {

    /**
     * Code de la permission requise
     */
    String value();

    /**
     * Message d'erreur personnalisé
     */
    String message() default "Permission insuffisante";

    /**
     * Indique si la vérification doit inclure l'organisation courante
     */
    boolean checkOrganization() default true;

    /**
     * Indique si la vérification doit inclure l'agence courante
     */
    boolean checkAgency() default false;
}
