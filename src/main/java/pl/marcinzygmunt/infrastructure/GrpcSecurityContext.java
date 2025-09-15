package pl.marcinzygmunt.infrastructure;

import io.grpc.Context;
import io.micronaut.security.authentication.Authentication;

public final class GrpcSecurityContext {
    private GrpcSecurityContext() {}
    public static final Context.Key<Authentication> AUTH_KEY = Context.key("auth-user");
}
