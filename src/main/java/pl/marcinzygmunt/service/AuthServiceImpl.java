package pl.marcinzygmunt.service;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.micronaut.grpc.annotation.GrpcService;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.token.jwt.generator.JwtTokenGenerator;
import lombok.extern.slf4j.Slf4j;
import pl.marcinzygmunt.*;
import pl.marcinzygmunt.infrastructure.GrpcSecurityContext;

import java.util.List;

@Slf4j
@GrpcService
public class AuthServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {

    private final JwtTokenGenerator jwtGenerator;

    public AuthServiceImpl(JwtTokenGenerator jwtGenerator) {
        this.jwtGenerator = jwtGenerator;
    }

    @Override
    public void login(LoginRequest request, StreamObserver<LoginReply> responseObserver) {
        if ("admin".equals(request.getUsername()) && "secret".equals(request.getPassword())) {
            Authentication auth = Authentication.build(request.getUsername(), List.of("ADMIN"));
            jwtGenerator.generateToken(auth, 300).ifPresentOrElse(token -> {
                responseObserver.onNext(LoginReply.newBuilder().setToken(token).build());
                responseObserver.onCompleted();
            }, () -> responseObserver.onError(Status.INTERNAL.withDescription("JWT generation failed").asRuntimeException()));
        } else {
            responseObserver.onError(Status.UNAUTHENTICATED.withDescription("Invalid credentials").asRuntimeException());
        }
        log.info("Login Successfully");
    }

    @Override
    public void secureHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        Authentication auth = GrpcSecurityContext.AUTH_KEY.get();
        if (auth == null) {
            responseObserver.onError(
                    Status.UNAUTHENTICATED.withDescription("No JWT found").asRuntimeException()
            );
            return;
        }
        HelloReply reply = HelloReply.newBuilder()
                .setMessage("Hello " + auth.getName() + "(" + auth.getRoles() + ")" + "! You said: " + request.getName())
                .build();

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}