package com.alves_dev.impostor.model;

/**
 * Define o estado atual da sala de jogo.
 */
public enum GameState {
    /**
     * Sala criada, aguardando jogadores entrarem
     */
    WAITING_PLAYERS,

    /**
     * Rodada iniciada, jogadores visualizando palavra/dica
     */
    ROUND_STARTED,

    /**
     * Fase de votação ativa
     */
    VOTING,

    /**
     * Rodada finalizada, exibindo resultados
     */
    ROUND_FINISHED,

    /**
     * Jogo completamente encerrado
     */
    GAME_FINISHED
}