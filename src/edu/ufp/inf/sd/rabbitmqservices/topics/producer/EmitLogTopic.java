package edu.ufp.inf.sd.rabbitmqservices.topics.producer;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import edu.ufp.inf.sd.rabbitmqservices.util.RabbitUtils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Build a logging system based on severity and also the source which emitted the log
 * (like syslog unix tool - routes logs based on both severity (info/warn/critical) and facility (auth/cron/kernel).
 * <p>
 * Topic exchange:
 * Messages sent to a topic exchange cannot have an arbitrary routing_key, it must be a list of words,
 * delimited by dots. The words can be anything, but usually they specify some features related to the message.
 * e.g. "stock.usd.nyse", "nyse.vmw", "quick.orange.rabbit".
 * <p>
 * The routing key may contain up to 255 bytes.
 * The binding key and routing key must have the same form/pattern.
 * <p>
 * The logic behind the topic exchange is similar to a direct exchange (cf. a message sent with a particular
 * routing key will be delivered to all queues that are bound with a matching binding key).
 * However there are two important special cases for binding keys:
 * - The * (star) can substitute exactly 1 word.
 * - The # (hash) can substitute 0+ words.
 * <p>
 * Suppose we need to send messages describing animals, using a routing key with 3 words separated by 2 dots:
 * "<speed>.<colour>.<species>"
 * <p>
 * Suppose we create 3 bindings on 2 queues (Q1 and Q2) to receive/consume messages:
 * - Q1 with bindingKey = "*.orange.*" (interested in all orange animals)
 * - Q2 with bindingKeys = "*.*.rabbit" and "lazy.#" (interested in everything about rabbits, and lazy animals)
 * <p>
 * Suppose we use the following routingKeys to send/publish messages (CANNOT USE WILDCHARS):
 * - If routingKey = "quick.orange.rabbit" or "lazy.orange.elephant" => delivered to both Q1 and Q2.
 * - If routingKey = "quick.orange.fox" => delivered to Q1, only.
 * - If routingKey = "lazy.brown.fox" => delivered to Q2, only.
 * - If routingKey = "lazy.pink.rabbit" => delivered to Q2 (only once time, even though it matches two bindings).
 * - If routingKey = "quick.brown.fox" => doesn't match any binding so it will be discarded.
 * - If routingKey = "lazy.orange.male.rabbit" => match last binding, hence delivered to Q2.
 * <p>
 * Any message with 1 or 4 words, like "orange" or "quick.orange.male.rabbit", will not match any bindings (discarded).
 *
 * <p>
 * Topic exchange is powerful and can behave like other exchanges:
 * - When a queue is bound with "#" (hash) binding key, it will receive all messages,
 * regardless of routing key, like FANOUT exchange.
 * - When special characters "*" (star) and "#" (hash) are not used in bindings,
 * the topic exchange will behave just like a DIRECT exchange.
 *
 * @author rui
 */
public class EmitLogTopic {

    public static final String EXCHANGE_NAME="exchange_topic_logs";

    /**
     * This producer sends logging messages organised by topics.
     * <p>
     * Let us assume that routing keys have 2 words: <facility>.<severity>.
     * <p>
     * For example,  to emit a log we could use:
     * $ runproducer "kern.critical" "some kern critical message" //to send "some critical kernel message"
     * $ runproducer "cron.info" "some cron info message" //to send "some info cron message"
     * <p>
     * (NB: CANNOT use wildchars, such as '*' or '#', when publishing messages)
     *
     * <p>
     * Another example, used on a chat service parameterized by <chat_room>.<msg_type>.
     * <p>
     * For example, to emit messages for a chat room we could use
     * $ runproducer room1.info "room1 info message" //for sending "some info message" to room1.info topic
     * $ runproducer room2.laugh "room1 laugh message" //for sending "some laugh message" to room2.laugh topic
     *
     * @param argv
     */
    public static void main(String[] argv) {
        if (argv.length < 4) {
            System.err.println("Usage: ReceiveLogsTopic [HOST] [PORT] [EXCHANGE] [RoutingKey] [Message]...");
            System.exit(1);
        }

        //try-with-resources
        try (Connection connection=RabbitUtils.newConnection2Server("localhost", "guest", "guest");
             Channel channel=RabbitUtils.createChannel2Server(connection)) {

            //Bind exchange to channel
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);

            //Routing keys will have two words: <facility.severity> e.g. "kern.critical"
            String routingKey=RabbitUtils.getRouting(argv, 3);
            String message=RabbitUtils.getMessage(argv, 4);
            //Messages are not persisted (will be lost if no queue is bound to exchange yet)
            BasicProperties props=null;//=MessageProperties.PERSISTENT_TEXT_PLAIN

            //Publish message on exchange with routing keys
            channel.basicPublish(EXCHANGE_NAME, routingKey, props, message.getBytes("UTF-8"));
            System.out.println(" [x] Sent '" + routingKey + "':'" + message + "'");

        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}
