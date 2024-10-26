package de.lenneflow.orchestrationservice.configuration;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.concurrent.Executor;

@Configuration
public class AppConfiguration {

    public static final String FUNCTIONRESULTQUEUE = "functionResultQueue";
    public static final String FUNCTIONQUEUE = "functionQueue";


    @Value("${rabbit.address}")  private String address;

    @Value("${rabbit.port}")  private int port;

    @Value("${rabbit.username}")  private String userName;

    @Value("${rabbit.password}")  private String password;

    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

    @Bean
    public Queue functionResultQueue() {
        return new Queue(FUNCTIONRESULTQUEUE, true);
    }

    @Bean
    public Queue functionQueue() {
        return new Queue(FUNCTIONQUEUE, true);
    }

//    @Bean
//    public RestTemplate restTemplate(RestTemplateBuilder builder) {
//        return builder.setReadTimeout(Duration.ofMinutes(10)).build();
//    }

    @Bean
    public RestTemplate getRestTemplate() throws NoSuchAlgorithmException, KeyManagementException {

        // Create SSL context to trust all certificates
        SSLContext sslContext = SSLContext.getInstance("TLS");

        // Define trust managers to accept all certificates
        TrustManager[] trustManagers = new TrustManager[]{new X509TrustManager() {
            // Method to check client's trust - accepting all certificates
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
            }

            // Method to check server's trust - accepting all certificates
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
            }

            // Method to get accepted issuers - returning an empty array
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }};

        // Initialize SSL context with the defined trust managers
        sslContext.init(null, trustManagers, null);

        // Disable SSL verification for RestTemplate

        // Set the default SSL socket factory to use the custom SSL context
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

        // Set the default hostname verifier to allow all hostnames
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

        // Create a RestTemplate with a custom request factory

        // Build RestTemplate with SimpleClientHttpRequestFactory
        return new RestTemplateBuilder().requestFactory(SimpleClientHttpRequestFactory.class).setReadTimeout(Duration.ofMinutes(10))
                .build();
    }


    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("lenneflow-");
        executor.initialize();
        return executor;
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setAddresses(address);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(userName);
        connectionFactory.setPassword(password);
        return connectionFactory;
    }
}
