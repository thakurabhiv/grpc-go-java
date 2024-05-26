package main

import (
	"context"
	"io"
	"log"
	"sync"
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

func callSayHelloServerStreaming(client pb.GreetServiceClient, names *pb.NameList) {
	stream, err := client.SayHelloServerStreaming(context.Background(), names)
	if err != nil {
		log.Fatalf("Error wile getting server stream: %v", err)
	}

	for {
		res, err := stream.Recv()
		if err == io.EOF {
			log.Println("Server stream closed")
			break
		}
		if err != nil {
			log.Fatalf("Error receiving value: %v", err)
		}

		log.Printf("Message from server: %v", res.Message)
	}
}

func callSayHelloBidirectionalStreaming(client pb.GreetServiceClient, names *pb.NameList) {
	stream, err := client.SayHelloBidirectionalStreaming(context.Background())
	if err != nil {
		log.Fatalf("Error opening bidirectional stream: %v", err)
	}

	var wg sync.WaitGroup

	// start new goroutine
	// for receving messages from server stream
	go func() {
		for {
			res, err := stream.Recv()
			if err == io.EOF {
				break
			}
			if err != nil {
				log.Fatalf("Error while receiving message: %v", err)
			}

			wg.Done()
			log.Printf("Message from server: %v", res.Message)
		}
	}()

	// send request one by one
	for _, name := range names.Names {
		req := &pb.HelloRequest{
			Name: name,
		}

		if err = stream.Send(req); err != nil {
			log.Fatalf("Error sending name: %v", err)
		}
		wg.Add(1)
		time.Sleep(500 * time.Millisecond)
	}

	wg.Wait()
	log.Printf("Done")
}
