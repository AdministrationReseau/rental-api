package inc.yowyob.rental_api.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation pour vérifier qu'un utilisateur possède toutes les permissions spécifiées
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAllPermissions {

    /**
     * Liste des permissions (toutes requises)
     */
    String[] value();

    /**
     * Message d'erreur personnalisé
     */
    String message() default "Permissions insuffisantes";

    /**
     * Indique si la vérification doit inclure l'organisation courante
     */
    boolean checkOrganization() default true;
}
