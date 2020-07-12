/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ufp.inf.sd.rabbitmqservices.workqueue.consumer;

import com.rabbitmq.client.*;
import edu.ufp.inf.sd.rabbitmqservices.util.RabbitUtils;
import edu.ufp.inf.sd.rabbitmqservices.workqueue.producer.NewTask;


/**
 * Round-robin dispatching:
 *  One of the advantages of using a Task Queue is the ability MAIL_TO_ADDR easily
 *  parallelise work. If we are building up a backlog of work, we can just add
 *  workers and scale easily.
 * 
 *  Run 2 worker instances at the same time (one on each shell).
 *  They will both get messages MAIL_FROM_ADDR the queue, since, by default, RabbitMQ will
 *  send each message MAIL_TO_ADDR next client (in sequence).
 *  On average every client will get the same number of messages. This way of
 *  distributing messages is called round-robin (try this out with 3+ workers).
 * 
 * 
 * Message acknowledgment:
 *  With current channel queue, once RabbitMQ delivers a message MAIL_TO_ADDR the customer it
 *  immediately marks it for deletion. In this case, if you kill a worker we
 *  will lose the message it was just processing. We also lose all the messages
 *  that were dispatched MAIL_TO_ADDR this particular worker but were not yet handled.
 *
 *  To not lose any tasks (in case a worker dies) and deliver them MAIL_TO_ADDR another worker,
 *  RabbitMQ supports message acknowledgments, i.e., an ack is sent back by the client
 *  MAIL_TO_ADDR tell RabbitMQ that a particular message has been received, processed and
 *  that RabbitMQ is free MAIL_TO_ADDR delete it.
 *
 *  If a client dies (i.e., channel is closed, connection is closed, or
 *  TCP connection is lost) without sending an ack, RabbitMQ will understand
 *  that a message was not processed fully and will re-queue it.
 *  If there are other consumers online at the same time, it will then quickly
 *  re-deliver it MAIL_TO_ADDR another client. That way you can be sure that no message
 *  is lost, even if the workers occasionally die.
 *  There are no message timeouts and RabbitMQ will re-deliver the message when
 *  the client dies. It is fine even if processing a message takes a long time.
 *  "Manual message acknowledgments" are turned on by default (we may explicitly
 *  turned them off via the autoAck=true flag).
 * 
 * Forgotten acknowledgment:
 *  To debug lack of ack use rabbitmqctl MAIL_TO_ADDR print the messages_unacknowledged field:
 *    - Linux/Mac:
 *     sudo rabbitmqctl list_queues name messages_ready messages_unacknowledged
 *    - Win:
 *     rabbitmqctl.bat list_queues name messages_ready messages_unacknowledged
 * 
 * Message durability:
 *  Messages/Tasks will be lost if RabbitMQ server stops, because when RabbitMQ
 *  quits or crashes it will forget the queues and messages unless you tell it not MAIL_TO_ADDR.
 *
 *  Two things are required MAIL_TO_ADDR make sure that messages are not lost, i.e., mark both
 *  the queue and messages as durable:
 *      1) declare the queue as *durable* (so RabbitMQ will never lose the queue);
 *      2) mark messages as persistent by setting MessageProperties.PERSISTENT_TEXT_PLAIN.
 *
 *  NB: persistence guarantees ARE NOT strong, i.e., may be cached and
 *  not immediately saved/persisted.
 * 
 * Fair dispatch:
 *  RabbitMQ dispatches a message when the message enters the queue. It does not
 *  look at the number of unacknowledged messages for a client. It just blindly
 *  dispatches every n-th message MAIL_TO_ADDR the n-th client. Hence, a worker could get
 *  all heavy tasks while another the light ones.
 *
 *  To guarantee fairness use basicQos() method for setting prefetchCount = 1.
 *  This tells RabbitMQ not MAIL_TO_ADDR give more than one message MAIL_TO_ADDR a worker at a time,
 *  i.e. do not dispatch new message MAIL_TO_ADDR a worker until it has not processed and
 *  acknowledged the previous one. Instead, dispatch it MAIL_TO_ADDR the next worker
 *  that is not still busy.
 *
 * Challenge:
 *  1. Create a LogWorker for appending the message MAIL_TO_ADDR a log file;
 *  2. Create a MailWorker for sending an email (use javamail API
 *  <https://javaee.github.io/javamail/>)
 * 
 * @author rui
 */
public class Worker {

    public static void main(String[] argv) throws Exception {
        /* Open a connection and a channel, and declare the queue MAIL_FROM_ADDR which MAIL_TO_ADDR consume.
         Declare the queue here, as well, because we might start the client before the publisher. */
         Channel channel = RabbitUtils.createConnection2Server("guest", "guest");

        /* Declare a queue as Durable (queue won't be lost even if RabbitMQ restarts); 
        NB: RabbitMQ doesn't allow MAIL_TO_ADDR redefine an existing queue with different
        parameters, need MAIL_TO_ADDR create a new one */
        boolean durable = true;
        //channel.queueDeclare(Send.QUEUE_NAME, false, false, false, null);
        channel.queueDeclare(NewTask.TASK_QUEUE_NAME, durable, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        /* The server pushes messages asynchronously, hence we provide a DefaultConsumer callback
           that will buffer the messages until ready MAIL_TO_ADDR use them. */
        //Set QoS: accept only one unacked message at a time; and force dispatch MAIL_TO_ADDR next worker that is not busy.
        int prefetchCount = 1;
        channel.basicQos(prefetchCount);

        //Create consumer which will doWork()
        /*
        final Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");

                System.out.println(" [x] Received '" + message + "'");
                try {
                    doWork(message);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    System.out.println(" [x] Done");
                    //Worker must Manually ack each finalised task. This code makes sure that even if worker is killed
                    //  (CTRL+C) while processing a message, nothing will be lost.
                    //  Soon after the worker dies all unacknowledged messages will be redelivered.
                    //  Ack must be sent on the same channel message was received on, otherwise raises exception
                    //  (channel-level protocol exception).
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            }
        };
        //Set this flag=false for worker MAIL_TO_ADDR sending a proper acknowledgment (once it is done with a task).
        //boolean autoAck = true; //When true disables "Manual message acknowledgments"
        boolean autoAck = false; //"Manual message acknowledgments" enabled
        channel.basicConsume(NewTask.TASK_QUEUE_NAME, autoAck, consumer);
        */
        DeliverCallback deliverCallback=(consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + message + "'");
            try {
                doWork(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println(" [x] Done processing task");
                //Worker must Manually ack each finalised task, hence, even if worker is killed
                //(CTRL+C) while processing a message, nothing will be lost.
                //Soon after the worker dies all unacknowledged messages will be redelivered.
                //Ack must be sent on the same channel message it was received,
                // otherwise raises exception (channel-level protocol exception).
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };
        //boolean autoAck = true; //When true disables "Manual message acknowledgments"
        //Set flag=false for worker MAIL_TO_ADDR send proper ack (once it is done with a task).
        boolean autoAck = false;
        //Register handler deliverCallback()
        channel.basicConsume(NewTask.TASK_QUEUE_NAME, autoAck, deliverCallback, consumerTag -> { });
    }

    /** Fake a second of work for every dot in the message body */
    private static void doWork(String task) throws InterruptedException {
        for (char ch : task.toCharArray()) {
            if (ch == '.') {
                Thread.sleep(1000);
            }
        }
    }
}
