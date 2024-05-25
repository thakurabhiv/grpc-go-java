package com.curlinfinity.grpcserver.service;

import com.curlinfinity.grpcserver.proto.GreetServiceGrpc;
import com.curlinfinity.grpcserver.proto.HelloRequest;
import com.curlinfinity.grpcserver.proto.HelloResponse;
import com.curlinfinity.grpcserver.proto.MessageList;
import com.curlinfinity.grpcserver.proto.NoParam;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class GreetServiceImpl extends GreetServiceGrpc.GreetServiceImplBase {
    @Override
    public void sayHello(NoParam request, StreamObserver<HelloResponse> responseObserver) {
        System.out.println("Inside sayHello");

        HelloResponse response = HelloResponse
                .newBuilder()
                .setMessage("Hello, buddy!")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
        System.out.println("Hello response sent");
    }

    @Override
    public StreamObserver<HelloRequest> sayHelloClientStreaming(StreamObserver<MessageList> responseObserver) {
        return new StreamObserver<HelloRequest>() {

            final MessageList.Builder messageBuilder = MessageList.newBuilder();

            @Override
            public void onNext(HelloRequest value) {
                System.out.println("Received name: " + value.getName());
                String message = String.format("Hello, %s", value.getName());

                messageBuilder.addMessages(message);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    System.err.println("Thread interrupted: " + ex);
                }
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Error in streaming: " + t);
            }

            @Override
            public void onCompleted() {
                System.out.println("Sending message list");
                responseObserver.onNext(messageBuilder.build());
                responseObserver.onCompleted();
            }
        };
    }
}
