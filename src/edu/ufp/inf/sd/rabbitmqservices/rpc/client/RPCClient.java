package edu.ufp.inf.sd.rabbitmqservices.rpc.client;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.tools.jsonrpc.JsonRpcClient;
import com.rabbitmq.tools.jsonrpc.JsonRpcMapper;
import edu.ufp.inf.sd.rabbitmqservices.util.RabbitUtils;

/**
 * Client that makes RPC call to server fibonacci function.
 *
 *
 *
 * ==========================================
 * Challenge:
 * Implement a calculator for basic operations: add, sub, mult, div.
 *
 * Requests must be coded in json (doenload jar from https://github.com/stleary/JSON-java)
 *
 * e.g.
 * Request: {"operation":"add", "values":[10.0, 8.0]}
 * Reply: {"result":18.0}
 *
 */
public class RPCClient implements AutoCloseable {

    private Connection connection;
    private Channel channel;
    private String requestQueueName;

    public RPCClient(String[] args) throws IOException, TimeoutException {
        RabbitUtils.printArgs(args);

        //Read args passed via shell command
        String host=args[0];
        int port=Integer.parseInt(args[1]);
        this.requestQueueName=args[2];

        connection=RabbitUtils.newConnection2Server(host, port, "guest", "guest");
        channel=RabbitUtils.createChannel2Server(connection);
    }

    public static void main(String[] args) {
        //try-with-resources...
        /*try (RPCClient fibonacciRpc=new RPCClient(args)) {
            //RPC call for calculating finonacci of numbers between [0..31]
            for (int i=0; i < 32; i++) {
                String i_str=Integer.toString(i);
                System.out.println(" [x] Requesting fib(" + i_str + ")");

                //Execute remote call and print...
                String response=fibonacciRpc.call(i_str);
                System.out.println(" [.] Got '" + response + "'");
            }
        } catch (IOException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
        }*/
        try (RPCClient calculatorRPC=new RPCClient(args)) {
                String calc="{\"operation\":\"add\", \"values\":[10.0, 8.0]}";
                System.out.println(" [x] Requesting add(\"[10.0, 8.0]\")");
                String response=calculatorRPC.call(calc);
                System.out.println(" [.] Got '" + response + "'");

                calc="{\"operation\":\"sub\", \"values\":[10.0, 8.0]}";
                System.out.println(" [x] Requesting add(\"[10.0, 8.0]\")");
                response=calculatorRPC.call(calc);
                System.out.println(" [.] Got '" + response + "'");

                calc="{\"operation\":\"mul\", \"values\":[10.0, 8.0]}";
                System.out.println(" [x] Requesting add(\"[10.0, 8.0]\")");
                response=calculatorRPC.call(calc);
                System.out.println(" [.] Got '" + response + "'");

                calc="{\"operation\":\"div\", \"values\":[10.0, 8.0]}";
                System.out.println(" [x] Requesting add(\"[10.0, 8.0]\")");
                response=calculatorRPC.call(calc);
                System.out.println(" [.] Got '" + response + "'");
        } catch (TimeoutException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String call(String message) throws IOException, InterruptedException {
        //Generate a correlation ID (between call and reply)
        final String correlationID=UUID.randomUUID().toString();

        //Get automatic queue for receiving reply
        String replyQueueName=channel.queueDeclare().getQueue();

        //Build properties with correlationID and replyQueueName
        AMQP.BasicProperties props=new AMQP.BasicProperties
                .Builder()
                .correlationId(correlationID)
                .replyTo(replyQueueName)
                .build();

        //Publish call to requestQueue (empty exchange) with given properties
        channel.basicPublish("", this.requestQueueName, props, message.getBytes(StandardCharsets.UTF_8));

        //Create array blocking queue for handler to store reply
        final BlockingQueue<String> responseArrayBlockQueue=new ArrayBlockingQueue<>(1);

        //Create callback that will receive reply message
        DeliverCallback deliverCallback=(consumerTag, delivery) -> {
            if (delivery.getProperties().getCorrelationId().equals(correlationID)) {
                responseArrayBlockQueue.offer(new String(delivery.getBody(), StandardCharsets.UTF_8));
            }
        };
        CancelCallback cancelCallback=(consumerTag) -> {
            System.out.println(" [x] Cancel callback activated: " + consumerTag);
        };

        //Associate callback for consuming RPC reply
        String ctag=channel.basicConsume(replyQueueName, true, deliverCallback, cancelCallback);

        //Get reply from array blocking queue
        String result=responseArrayBlockQueue.take();

        channel.basicCancel(ctag);
        return result;
    }

    /**
     * Close connection to rabbitmq.
     *
     * @throws IOException
     */
    public void close() throws IOException {
        connection.close();
    }
}