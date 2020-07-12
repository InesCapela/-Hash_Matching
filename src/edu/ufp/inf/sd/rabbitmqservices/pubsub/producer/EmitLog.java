package edu.ufp.inf.sd.rabbitmqservices.pubsub.producer;


import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import edu.ufp.inf.sd.rabbitmqservices.util.RabbitUtils;


public class EmitLog {

    public static final String EXCHANGE_NAME="logs_exchange";

    public static void main(String[] argv) throws Exception {
        //Try-with-resources
        try (Connection connection= RabbitUtils.newConnection2Server("localhost", "guest", "guest");
             Channel channel=RabbitUtils.createChannel2Server(connection)) {

            System.out.println(" [x] Declare exchange: '" + EXCHANGE_NAME + "' of type " + BuiltinExchangeType.FANOUT.toString());
            /* Set the Exchange type MAIL_TO_ADDR FANOUT (multicast MAIL_TO_ADDR all queues). */
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);

            String message=RabbitUtils.getMessage(argv, 3);
          
            /* Publish messages to the logs_exchange instead of the nameless one.
               Fanout exchanges will ignore routingKey (hence set routingKey="").
               Messages will be lost if no queue is bound to the exchange yet. */
            String routingKey="";
            channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes("UTF-8"));
            System.out.println(" [x] Sent: '" + message + "'");
        }
    }
}