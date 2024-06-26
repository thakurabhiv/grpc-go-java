package com.curlinfinity.grpcserver.service;

import com.curlinfinity.grpcserver.proto.GreetServiceGrpc;
import com.curlinfinity.grpcserver.proto.HelloRequest;
import com.curlinfinity.grpcserver.proto.HelloResponse;
import com.curlinfinity.grpcserver.proto.MessageList;
import com.curlinfinity.grpcserver.proto.NameList;
import com.curlinfinity.grpcserver.proto.NoParam;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;

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

    @Override
    public void sayHelloServerStreaming(NameList request, StreamObserver<HelloResponse> responseObserver) {
        List<String> names = request.getNamesList();
        for (String name: names) {
            HelloResponse response = HelloResponse.newBuilder()
                    .setMessage(String.format("Hello, %s", name))
                    .build();

            System.out.printf("Sending message for name: %s\n", name);
            responseObserver.onNext(response);
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                System.err.println("Thread interrupted");
                responseObserver.onError(ex);
            }
        }

        System.out.println("Streaming completed");
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<HelloRequest> sayHelloBidirectionalStreaming(StreamObserver<HelloResponse> responseObserver) {
        return new StreamObserver<HelloRequest>() {
            @Override
            public void onNext(HelloRequest value) {
                String name = value.getName();
                System.out.printf("Got request for name: %s\n", name);
                HelloResponse response = HelloResponse.newBuilder()
                        .setMessage(String.format("Hello, %s", name))
                        .build();

                responseObserver.onNext(response);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    System.err.println("Thread interrupted: " + ex);
                }
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                System.out.println("Streaming completed");
                responseObserver.onCompleted();
            }
        };
    }
}
