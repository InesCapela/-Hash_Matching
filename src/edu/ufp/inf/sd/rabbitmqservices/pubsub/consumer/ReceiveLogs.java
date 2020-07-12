/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ufp.inf.sd.rabbitmqservices.pubsub.consumer;

import com.rabbitmq.client.*;
import edu.ufp.inf.sd.rabbitmqservices.pubsub.producer.EmitLog;
import edu.ufp.inf.sd.rabbitmqservices.util.RabbitUtils;

/**
 * Deliver message to multiple consumers (pattern known as "publish/subscribe").
 * <p>
 * Build a simple logging system with two programs:
 * - the first emits log messages and the second receives and prints them.
 * - every running copy of the receiver will get the messages (log messages
 * are going to be broadcasted to all receivers). One receiver directs logs to disk,
 * while another prints logs on the screen.
 * <p>
 * Exchanges:
 * A server never sends any messages directly to a queue, instead can only
 * send messages to an *exchange*.
 * The exchange receives messages to producers and pushes them to queues,
 * according the exchange type (cf. direct, topic, headers and fanout).
 * <p>
 * Listing exchanges:
 * To list the exchanges on the server you can run rabbitmqctl:
 * sudo rabbitmqctl list_exchanges
 * There will be amq.* exchanges and the default (unnamed or nameless) exchange
 * <p>
 * Temporary queues:
 * Previously we used queues with name (e.g. _01_hello and task_queue) to point
 * producers and workers to the same queue.
 * Now we want to hear about all log messages and the current flowing messages
 * not in the old ones. To solve that we need two things:
 * i) create an empty queue when connecting to rabbitmq
 * ii) once disconnect client, the queue should be automatically deleted
 * <p>
 * Binding:
 * tell the exchange to send messages to a queue (i.e. queue is interested in
 * messages MAIL_FROM_ADDR the exchange).
 * <p>
 * Listing bindings:
 * rabbitmqctl list_bindings
 * <p>
 * Running Receivers:
 * java -cp $CP ReceiveLogs > logs.log
 * java -cp $CP ReceiveLogs
 *
 * @author rui
 */
public class ReceiveLogs {

    public static void main(String[] argv) throws Exception {
        Connection connection = RabbitUtils.newConnection2Server("localhost","guest", "guest");
        Channel channel=RabbitUtils.createChannel2Server(connection);

        /* Use the Exchange FANOUT type: broadcasts all messages to all queues */
        channel.exchangeDeclare(EmitLog.EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
    
        /* Create a non-durable, exclusive, autodelete queue with a generated name.
           The string queueName will contains a random queue name (e.g. amq.gen-JzTY20BRgKO-HjmUJj0wLg) */
        String queueName=channel.queueDeclare().getQueue();
    
        /* Create binding: tell exchange to send messages to a queue;
           The fanout exchange ignores last parameter (routing/binding key) */
        String routingKey="";
        channel.queueBind(queueName, EmitLog.EXCHANGE_NAME, routingKey);

        System.out.println(" [*] Waiting for messages... to exit press CTRL+C");

        //Use a DeliverCallback instead of a DefaultConsumer for the Receiver
        DeliverCallback deliverCallback=(consumerTag, delivery) -> {
            String message=new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Consumer Tag ["+consumerTag+"] - Received '" + message + "'");
        };
        CancelCallback cancelCalback=(consumerTag) -> {
            System.out.println(" [x] Consumer Tag ["+consumerTag+"] - Cancel Callback invoked!");
        };
        channel.basicConsume(queueName, true, deliverCallback, cancelCalback);

        //DO NOT close connection neither channel otherwise it will terminate consumer
    }
}
