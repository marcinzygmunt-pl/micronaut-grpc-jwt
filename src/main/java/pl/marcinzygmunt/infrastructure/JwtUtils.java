package pl.marcinzygmunt.infrastructure;

import com.nimbusds.jwt.SignedJWT;
import io.micronaut.security.authentication.Authentication;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class JwtUtils {

    public static Optional<Authentication> toAuthentication(SignedJWT signedJWT) {
        try {
            String subject = signedJWT.getJWTClaimsSet().getSubject();
            if (subject == null) return Optional.empty();

            // Pobieranie ról z claim "roles"
            List<String> roles = Collections.emptyList();
            Object rolesClaim = signedJWT.getJWTClaimsSet().getClaim("roles");
            if (rolesClaim instanceof List<?>) {
                roles = (List<String>) rolesClaim;
            }

            return Optional.of(Authentication.build(subject, roles));
        } catch (ParseException e) {
            return Optional.empty();
        }
    }
}
