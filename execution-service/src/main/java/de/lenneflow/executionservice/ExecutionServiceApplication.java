package de.lenneflow.executionservice;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ExecutionServiceApplication {

    private static final String WORKERTASKRESULTQUEUE = "WorkerTaskResultQueue";
    private static final String SYSTEMTASKRESULTQUEUE = "SystemTaskResultQueue";

    public static void main(String[] args) {
        SpringApplication.run(ExecutionServiceApplication.class, args);
    }

    @Value("${rabbit.address}")
    private String address;

    @Value("${rabbit.username}")
    private String userName;

    @Value("${rabbit.password}")
    private String password;

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setAddresses(address);
        connectionFactory.setUsername(userName);
        connectionFactory.setPassword(password);
        return connectionFactory;
    }

    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

    @Bean
    public Queue workerTaskResultsQueue() {
        return new Queue(WORKERTASKRESULTQUEUE);
    }

    @Bean
    public Queue systemTaskResultsQueue() {
        return new Queue(SYSTEMTASKRESULTQUEUE);
    }

}
