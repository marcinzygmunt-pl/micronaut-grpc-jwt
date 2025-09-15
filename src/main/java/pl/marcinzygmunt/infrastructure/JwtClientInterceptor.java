package pl.marcinzygmunt.infrastructure;

import io.grpc.*;

public class JwtClientInterceptor implements ClientInterceptor {
    private final String jwt;

    public JwtClientInterceptor(String jwt) {
        this.jwt = jwt;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                headers.put(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER), "Bearer " + jwt);
                super.start(responseListener, headers);
            }
        };
    }
}