package com.allpasoft.msapigateway;

import com.netflix.discovery.EurekaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class MsApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsApiGatewayApplication.class, args);
    }

    @Autowired
    private EurekaClient eurekaClient;

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            String appName = eurekaClient.getApplicationInfoManager().getInfo().getAppName();
            System.out.println("Eureka conectado correctamente como: " + appName);
        } catch (Exception e) {
            System.out.println("Error al conectar con Eureka: " + e.getMessage());
        }
    }
}
