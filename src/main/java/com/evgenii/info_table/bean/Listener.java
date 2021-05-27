package com.evgenii.info_table.bean;


import com.evgenii.info_table.presentation.ProductReceiver;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@WebListener
public class Listener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(Listener.class);

    @Inject
    ProductReceiver restReceiver;

    @SneakyThrows
    @Override
    public void contextInitialized(ServletContextEvent event) {


        logger.info("Startup");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare("infoTable", false, false, false, null);
        logger.info(" [*] Waiting for messages. ");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);

            logger.info(" [x] Received '" + message + "'");
            if (message.equals("update")) {
                restReceiver.update();
            } else {
                restReceiver.disconnect();
            }
        };
        try {
            channel.basicConsume("infoTable", true, deliverCallback, consumerTag -> {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
