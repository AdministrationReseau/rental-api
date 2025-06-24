package inc.yowyob.rental_api.security.annotations;

import inc.yowyob.rental_api.core.enums.RoleType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation pour vérifier qu'un utilisateur possède un rôle spécifique
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    /**
     * Type de rôle requis
     */
    RoleType value();

    /**
     * Message d'erreur personnalisé
     */
    String message() default "Rôle insuffisant";

    /**
     * Indique si la vérification doit inclure l'organisation courante
     */
    boolean checkOrganization() default true;
}
