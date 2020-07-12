package edu.ufp.inf.sd.rabbitmqservices.rpc.server;

import com.rabbitmq.client.*;
import com.rabbitmq.tools.json.JSONUtil;

import edu.ufp.inf.sd.rabbitmqservices.util.RabbitUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Map;



/**
 * Server that accepts RPC calls for executing fibonacci function and returning result.
 */
public class RPCServer {

    public static String requestQueueName;


    private static int fib(int n) {
        if (n == 0) return 0;
        if (n == 1) return 1;
        return fib(n - 1) + fib(n - 2);
    }

    private static float add(float a, float b) {
        return a+b;
    }
    private static float sub(float a, float b) {
        return a-b;
    }
    private static float mul(float a, float b) {
        return a*b;
    }
    private static float div(float a, float b) {
        return a / b;
    }

    public static void main(String[] args) throws Exception {

        //Get args passed via shell command
        String host=args[0];
        int port=Integer.parseInt(args[1]);
        requestQueueName=args[2];

        /* try-with-resources will close resources automatically in reverse order... avoids finally */
        try (Connection connection=RabbitUtils.newConnection2Server(host, port, "guest", "guest");
             Channel channel=RabbitUtils.createChannel2Server(connection)) {

            //Create channel to wait for RPC calls
            boolean durable=false, exclusive=false, autoDelete=false;
            Map<String, Object> arguments=null;
            channel.queueDeclare(requestQueueName, durable, exclusive, autoDelete, arguments);
            channel.queuePurge(requestQueueName);

            //Set prefetchCount (may be used to spread the load equally over multiple servers)
            int prefetchCount=1;
            channel.basicQos(prefetchCount);

            System.out.println(" [x] Awaiting RPC requests");

            //Create an object as a monitor for syncing threads consumer/request (main) and producer/reply (delivery).
            Object monitor=new Object();

            //Consumer: receive call from client
            DeliverCallback deliverCallback=(consumerTag, delivery) -> {
                //Reply properties
                AMQP.BasicProperties replyProps=
                        new AMQP.BasicProperties
                                .Builder()
                                .correlationId(delivery.getProperties().getCorrelationId())
                                .build();
                //Init reply string
                double response = 0;
                String message= "";
                try {
                    message=new String(delivery.getBody(), StandardCharsets.UTF_8);
                    JSONObject jsonObject = new JSONObject(message);
                    JSONArray values = (JSONArray) jsonObject.get("values");
                    System.out.println("A operação é :" + jsonObject.getString("operation"));
                    response = switch (jsonObject.getString("operation")) {
                        case "add" -> values.getDouble(0) + values.getDouble(1);
                        case "sub" -> values.getDouble(0) - values.getDouble(1);
                        case "mul" -> values.getDouble(0) * values.getDouble(1);
                        case "div" -> values.getDouble(0) / values.getDouble(1);
                        default -> throw new IllegalStateException("Unexpected value: " + jsonObject.getString("operation"));
                    };
                    message = String.valueOf(response);
                    System.out.println(" [.] calc(" + message + ")");
                } catch (RuntimeException e) {
                    System.out.println(" [.] " + e.toString());
                } finally {
                    //Producer: send reply back to client
                    //Use nameless exchange when sending directly to queue
                    String exchangeName="";
                    channel.basicPublish(exchangeName,
                            delivery.getProperties().getReplyTo(),
                            replyProps,
                            message.getBytes(StandardCharsets.UTF_8));
                    boolean multiple=false;
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), multiple);

                    // RabbitMq client worker thread notifies the RPC server owner thread
                    synchronized (monitor) {
                        monitor.notify();
                    }
                }
            };

            CancelCallback cancelCallback=(consumerTag) -> {
                System.out.println(" [x] Cancel callback activated: " + consumerTag);
            };

            //Register Consumer to wait for reply (deliveryCallback)
            boolean autoAck=false;
            channel.basicConsume(requestQueueName, autoAck, deliverCallback, cancelCallback);

            //Block main thread... waits for deliveryCallback to consume the message from RPC client.
            while (true) {
                synchronized (monitor) {
                    try {
                        monitor.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}