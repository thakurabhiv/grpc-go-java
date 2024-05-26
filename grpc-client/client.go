package main

import (
	"fmt"
	"log"

	pb "github.com/thakurabhiv/grpc-go-java/grpc-client/proto"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
)

const (
	port = ":9000"
)

// command to generate protobuf go files
// protoc --go_out=. --go-grpc_out=. --proto_path=../proto ..\proto\greet.proto
func main() {
	target := fmt.Sprintf("localhost%s", port)
	conn, err := grpc.NewClient(target, grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		log.Fatalf("Error connecting client: %v", err)
	}

	client := pb.NewGreetServiceClient(conn)

	names := &pb.NameList{
		Names: []string{"Abhishek", "Aditya", "Sunny", "Pankaj", "John", "Jane"},
	}

	// callSayHello(client)
	// callSayHelloClientStreaming(client, names)
	// callSayHelloServerStreaming(client, names)
	callSayHelloBidirectionalStreaming(client, names)
}
