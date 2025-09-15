package pl.marcinzygmunt;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.micronaut.runtime.Micronaut;
import lombok.extern.slf4j.Slf4j;
import pl.marcinzygmunt.infrastructure.JwtClientInterceptor;

@Slf4j
public class Application {

    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
        demoCall();
    }

    private static void demoCall(){
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        AuthServiceGrpc.AuthServiceBlockingStub stub = AuthServiceGrpc.newBlockingStub(channel);

        // Login -> pobieramy JWT
        LoginReply loginReply = stub.login(LoginRequest.newBuilder()
                .setUsername("admin")
                .setPassword("secret")
                .build());

        String jwt = loginReply.getToken();
        log.info("Got JWT: {} ", jwt);

        // Wywołanie metody secureHello z JWT
        AuthServiceGrpc.AuthServiceBlockingStub secureStub =
                stub.withInterceptors(new JwtClientInterceptor(jwt));

        HelloReply reply = secureStub.secureHello(
                HelloRequest.newBuilder().setName("Micronaut").build()
        );

        log.info("Server replied: {}", reply.getMessage());

        channel.shutdown();
    }

}