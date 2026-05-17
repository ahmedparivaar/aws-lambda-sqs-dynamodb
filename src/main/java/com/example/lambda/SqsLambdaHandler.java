package com.example.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.HashMap;
import java.util.Map;

public class SqsLambdaHandler implements RequestHandler<SQSEvent, String> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String handleRequest(SQSEvent event, Context context) {

        DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
                .region(Region.EU_NORTH_1)
                .build();

        try {

            for (SQSEvent.SQSMessage message : event.getRecords()) {

                String body = message.getBody();

                System.out.println("Received: " + body);

                Order order = mapper.readValue(body, Order.class);

                Map<String, AttributeValue> item = new HashMap<>();

                item.put("orderId",
                        AttributeValue.builder()
                                .s(order.getOrderId())
                                .build());

                item.put("customerName",
                        AttributeValue.builder()
                                .s(order.getCustomerName())
                                .build());

                item.put("amount",
                        AttributeValue.builder()
                                .n(order.getAmount().toString())
                                .build());

                PutItemRequest request = PutItemRequest.builder()
                        .tableName("orders")
                        .item(item)
                        .build();

                dynamoDbClient.putItem(request);

                System.out.println("Saved into DynamoDB");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "SUCCESS";
    }
}
