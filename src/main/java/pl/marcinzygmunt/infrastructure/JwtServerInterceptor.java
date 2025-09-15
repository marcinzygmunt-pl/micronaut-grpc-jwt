package pl.marcinzygmunt.infrastructure;

import com.nimbusds.jwt.SignedJWT;
import io.grpc.*;
import io.micronaut.security.authentication.Authentication;

import io.micronaut.security.token.jwt.validator.ReactiveJsonWebTokenValidator;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

@Singleton
public class JwtServerInterceptor implements ServerInterceptor {

    private final ReactiveJsonWebTokenValidator jwtValidator;

    public JwtServerInterceptor(ReactiveJsonWebTokenValidator jwtValidator) {
        this.jwtValidator = jwtValidator;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String authHeader = headers.get(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER));
        if (!call.getMethodDescriptor().getFullMethodName().contains("/Login")) {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                call.close(Status.UNAUTHENTICATED.withDescription("Missing or invalid Authorization header"), headers);
                return new ServerCall.Listener<>() {
                };
            }

            String token = authHeader.substring(7);
            try {
                Publisher<SignedJWT> pubAuth = jwtValidator.validate(token, null);
                var auth = JwtUtils.toAuthentication(Mono.from(pubAuth).block());
                if (auth.isEmpty()) {
                    call.close(Status.UNAUTHENTICATED.withDescription("JWT validation returned empty"), headers);
                    return new ServerCall.Listener<>() {};
                }

                Context ctx = Context.current().withValue(GrpcSecurityContext.AUTH_KEY, auth.get());
                return Contexts.interceptCall(ctx, call, headers, next);

            } catch (Exception e) {
                call.close(Status.UNAUTHENTICATED.withDescription("Invalid JWT: " + e.getMessage()), headers);
                return new ServerCall.Listener<>() {};
            }
        }
        return Contexts.interceptCall(Context.current(), call, headers, next);
    }
}