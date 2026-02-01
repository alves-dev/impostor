package com.alves_dev.impostor.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuração do WebSocket para comunicação em tempo real.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configura o message broker.
     * - /topic: para mensagens broadcast (público)
     * - /app: prefixo para mensagens enviadas ao servidor
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilita um broker simples em memória para enviar mensagens aos clientes
        config.enableSimpleBroker("/topic");

        // Define o prefixo para mensagens destinadas aos métodos @MessageMapping
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Registra o endpoint WebSocket.
     * Clientes se conectarão em /ws
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .withSockJS(); // Fallback para navegadores que não suportam WebSocket
    }
}