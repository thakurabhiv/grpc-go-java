package main

import (
	"context"
	"log"
	"time"

	pb "github.com/thakurabhiv/grpc-go-java/grpc-client/proto"
)

func callSayHello(client pb.GreetServiceClient) {
	ctx, cancel := context.WithTimeout(context.Background(), time.Second)
	defer cancel()

	res, err := client.SayHello(ctx, &pb.NoParam{})
	if err != nil {
		log.Fatalf("Error getting hello response: %v", err)
	}

	log.Printf("Message from server: %s", res.Message)
}

func callSayHelloClientStreaming(client pb.GreetServiceClient, names *pb.NameList) {
	stream, err := client.SayHelloClientStreaming(context.Background())
	if err != nil {
		log.Fatalf("Error opening client stream: %v", err)
	}

	for _, name := range names.Names {
		req := &pb.HelloRequest{
			Name: name,
		}
		err := stream.Send(req)
		if err != nil {
			log.Fatalf("Error sending name request: %v", err)
		}
	}

	messages, err := stream.CloseAndRecv()
	if err != nil {
		log.Fatalf("Error receiving messages: %v", err)
	}

	for _, message := range messages.Messages {
		log.Printf("Received message: %s", message)
	}
}
