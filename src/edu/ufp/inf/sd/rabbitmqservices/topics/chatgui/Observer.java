package edu.ufp.inf.sd.rabbitmqservices.topics.chatgui;

import com.rabbitmq.client.*;
import edu.ufp.inf.sd.rabbitmqservices.topics.producer.EmitLogTopic;
import edu.ufp.inf.sd.rabbitmqservices.util.RabbitUtils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static edu.ufp.inf.sd.rabbitmqservices.topics.producer.EmitLogTopic.EXCHANGE_NAME;

public class Observer {

    private final ObserverGuiClient gui;
    private final Channel channelToRabbitMq;
    private final String exchangeName=EXCHANGE_NAME;
    private final BuiltinExchangeType exchangeType=BuiltinExchangeType.TOPIC;
    private final String exchangeRoutingKey;
    private final String messageFormat="UTF-8";
    private String receivedMessage;


    public Observer(ObserverGuiClient gui, String host, String user, String pass, String bindingKey, String routingKey) throws IOException, TimeoutException {
        this.gui = gui;
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "going to attach observer to host" + host);
        Connection connection= RabbitUtils.newConnection2Server(host, user, pass);
        this.channelToRabbitMq = RabbitUtils.createChannel2Server(connection);
        bindExchangeToChannelRabbitMQ(this.channelToRabbitMq, exchangeName, exchangeType, bindingKey);
        this.exchangeRoutingKey = routingKey;
        attachConsumerToChannelExchangeWithKey(this.channelToRabbitMq, exchangeName, routingKey);
    }

    private void attachConsumerToChannelExchangeWithKey(Channel channel, String exchangeName, String exchangeBindingKey) throws IOException{
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, exchangeName, exchangeBindingKey);

        DeliverCallback deliverCallback=(consumerTag, delivery) -> {
            String message=new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Consumer Tag ["+consumerTag+"] - Received '" + message + "'");
            this.receivedMessage = message;
            gui.updateTextArea();
        };
        CancelCallback cancelCalback=(consumerTag) -> {
            System.out.println(" [x] Consumer Tag ["+consumerTag+"] - Cancel Callback invoked!");
        };
        channel.basicConsume(queueName, true, deliverCallback, cancelCalback);
    }

    private void bindExchangeToChannelRabbitMQ(Channel channel, String exchangeName, BuiltinExchangeType exchangeType, String bindingKey) throws IOException{
        channel.queueBind(channel.queueDeclare().getQueue(), exchangeName, bindingKey);
        channel.exchangeDeclare(exchangeName, exchangeType);

    }

    public void sendMessage(String msgToSend, String routingKey, BasicProperties prop) throws IOException {
        channelToRabbitMq.basicPublish(EXCHANGE_NAME, routingKey, null, msgToSend.getBytes("UTF-8"));
    }


    public String getReceivedMessage() {
        return receivedMessage;
    }

    public void setReceivedMessage(String receivedMessage) {
        this.receivedMessage = receivedMessage;
    }
}
