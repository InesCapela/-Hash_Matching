package edu.ufp.inf.sd.rabbitmqservices.pubsub.chatgui;

import com.rabbitmq.client.*;
import edu.ufp.inf.sd.rabbitmqservices.util.RabbitUtils;
import edu.ufp.inf.sd.rmi.util.rmisetup.SetupContextRMI;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static edu.ufp.inf.sd.rabbitmqservices.pubsub.producer.EmitLog.EXCHANGE_NAME;

public class Observer {

    private final ObserverGuiClient gui;
    private final Channel channelToRabbitMq;
    private final String exchangeName=EXCHANGE_NAME;
    private final BuiltinExchangeType exchangeType=BuiltinExchangeType.FANOUT;
    private final String exchangeRoutingKey="";
    private final String messageFormat="UTF-8";
    private String receivedMessage;


    public Observer(ObserverGuiClient gui, String host, String user, String pass) throws IOException, TimeoutException {
        this.gui = gui;
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "going to attach observer to host" + host);
        Connection connection= RabbitUtils.newConnection2Server(host, user, pass);
        this.channelToRabbitMq = RabbitUtils.createChannel2Server(connection);
        bindExchangeToChannelRabbitMQ(this.channelToRabbitMq, exchangeName, exchangeType);
        attachConsumerToChannelExchangeWithKey(this.channelToRabbitMq, exchangeName, exchangeRoutingKey);
    }

    private void attachConsumerToChannelExchangeWithKey(Channel channel, String exchangeName, String exchangeRoutingKey) throws IOException{
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, exchangeName, exchangeRoutingKey);

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

    private void bindExchangeToChannelRabbitMQ(Channel channel, String exchangeName, BuiltinExchangeType exchangeRoutingKey) throws IOException{
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
