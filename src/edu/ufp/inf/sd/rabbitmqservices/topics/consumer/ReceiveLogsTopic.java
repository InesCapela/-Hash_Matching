package edu.ufp.inf.sd.rabbitmqservices.topics.consumer;

import com.rabbitmq.client.*;
import edu.ufp.inf.sd.rabbitmqservices.topics.producer.EmitLogTopic;
import edu.ufp.inf.sd.rabbitmqservices.util.RabbitUtils;



public class ReceiveLogsTopic {

    /**
     * This comsumer receives logging info, organised by topics.
     *
     * Let us assume routing keys for logs with 2 words: "<facility>.<severity>".
     * - facility = {kern, cron, auth}
     * - severity = {info, warn, critical}
     * <p>
     * To receive ALL logs run client with key: "#":
     * $ runconsumer "#"
     * <p>
     * To receive ALL logs to facility: "kern":
     * $ runconsumer "kern.*"
     * <p>
     * To receive ALL "critical" logs from ALL facilities:
     * $ runconsumer "*.critical"
     * <p>
     * To receive multiple bindings (all severities from kernel, critical from all facilities):
     * $ runconsumer "kern.*" "*.critical"
     *
     */
    public static void main(String[] argv) throws Exception {
        //DO NOT USE try-with-resources HERE because closing resources (channel) will prevent receiving any messages.
        try {
            Connection connection=RabbitUtils.newConnection2Server("localhost", "guest", "guest");
            Channel channel=RabbitUtils.createChannel2Server(connection);

            channel.exchangeDeclare(EmitLogTopic.EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
            String queueName=channel.queueDeclare().getQueue();

            System.out.println("main(): argv.length=" + argv.length);
            printArgv(argv);
            if (argv.length < 4) {
                System.err.println("Usage: ReceiveLogsTopic [HOST] [PORT] [EXCHANGE] [RoutingKey 1]... [RoutingKey n]");
                System.exit(1);
            }

            //Bind to each routing key (received from args[3] upward)
            for (int i=3; i < argv.length; i++) {
                String bindingKey=argv[i];
                System.err.println("main(): add queue bind to queue = " + queueName + ", with bindingKey = " + bindingKey);
                channel.queueBind(queueName, EmitLogTopic.EXCHANGE_NAME, bindingKey);
            }

            System.out.println(" [*] Waiting for messages... to exit press CTRL+C");

            //Create callback that will receive messages from topic
            DeliverCallback deliverCallback=(consumerTag, delivery) -> {
                String message=new String(delivery.getBody(), "UTF-8");
                System.out.println(" [x] Received '" +
                        delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
            };
            CancelCallback cancelCallback=(consumerTag) -> {
                System.out.println(" [x] Cancel callback activated: " + consumerTag);
            };
            //Associate callback to queue with topic exchange
            //When true disables "Manual message acks";When false worker must explicitly send ack (once done with task)
            boolean autoAck = true;
            channel.basicConsume(queueName, autoAck, deliverCallback, cancelCallback);

            //Current Thread waits till interrupted (avoids finishing try-with-resources which closes channel)
            //Thread.currentThread().join();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printArgv(String[] argv) {
        int i=0;
        for (String s : argv) {
            System.out.println("argv[" + i++ + "] = " + s);
        }
    }
}
