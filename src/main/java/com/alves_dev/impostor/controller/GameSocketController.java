package com.alves_dev.impostor.controller;

import com.alves_dev.impostor.dto.*;
import com.alves_dev.impostor.model.Room;
import com.alves_dev.impostor.model.Round;
import com.alves_dev.impostor.model.WordPack;
import com.alves_dev.impostor.model.Player;
import com.alves_dev.impostor.service.RoomManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * Controller WebSocket para gerenciar ações do jogo em tempo real.
 */
@Controller
public class GameSocketController {

    @Autowired
    private RoomManager roomManager;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Inicia a primeira rodada do jogo.
     * Apenas o dono da sala pode executar.
     */
    @MessageMapping("/start-game")
    public void startGame(RoomActionRequest request) {
        try {
            Room room = roomManager.startGame(request.getRoomCode());

            // Envia o estado atualizado da sala
            broadcastRoomState(room);

            // Envia informações privadas para cada jogador
            sendPrivatePlayerInfo(room);

        } catch (Exception e) {
            sendError(request.getRoomCode(), e.getMessage());
        }
    }

    /**
     * Inicia a fase de votação.
     * Apenas o dono da sala pode executar.
     */
    @MessageMapping("/start-voting")
    public void startVoting(RoomActionRequest request) {
        try {
            Room room = roomManager.startVoting(request.getRoomCode());

            // Envia o estado atualizado da sala
            broadcastRoomState(room);

        } catch (Exception e) {
            sendError(request.getRoomCode(), e.getMessage());
        }
    }

    /**
     * Registra o voto de um jogador.
     */
    @MessageMapping("/vote")
    public void vote(VoteRequest request) {
        try {
            Room room = roomManager.registerVote(
                    request.getRoomCode(),
                    request.getPlayerId(),
                    request.getVotedPlayerId()
            );

            // Envia o estado atualizado da sala
            broadcastRoomState(room);

            // Se a rodada foi finalizada automaticamente, envia o resultado
            Round currentRound = room.getCurrentRoundObject();
            if (currentRound != null && currentRound.isFinished()) {
                sendRoundResult(room);
            }

        } catch (Exception e) {
            sendError(request.getRoomCode(), e.getMessage());
        }
    }

    /**
     * Inicia a próxima rodada.
     * Apenas o dono da sala pode executar.
     */
    @MessageMapping("/next-round")
    public void nextRound(RoomActionRequest request) {
        try {
            Room room = roomManager.startNextRound(request.getRoomCode());

            // Envia o estado atualizado da sala
            broadcastRoomState(room);

            // Envia informações privadas para cada jogador
            sendPrivatePlayerInfo(room);

        } catch (Exception e) {
            sendError(request.getRoomCode(), e.getMessage());
        }
    }

    /**
     * Finaliza o jogo manualmente.
     * Apenas o dono da sala pode executar.
     */
    @MessageMapping("/finish-game")
    public void finishGame(RoomActionRequest request) {
        try {
            Room room = roomManager.finishGame(request.getRoomCode());

            // Envia o estado atualizado da sala
            broadcastRoomState(room);

        } catch (Exception e) {
            sendError(request.getRoomCode(), e.getMessage());
        }
    }

    // Métodos auxiliares para envio de mensagens

    /**
     * Envia o estado da sala para todos os jogadores.
     */
    private void broadcastRoomState(Room room) {
        RoomStateResponse response = RoomStateResponse.fromRoom(room);
        messagingTemplate.convertAndSend(
                "/topic/room/" + room.getCode(),
                response
        );
    }

    /**
     * Envia informações privadas (palavra ou dica) para cada jogador.
     */
    private void sendPrivatePlayerInfo(Room room) {
        WordPack wordPack = room.getCurrentWordPack();
        if (wordPack == null) {
            return;
        }

        for (Player player : room.getPlayers()) {
            PrivatePlayerInfo info;

            if (player.getId().equals(room.getCurrentImpostorId())) {
                // É o impostor - recebe apenas a dica
                info = PrivatePlayerInfo.forImpostor(wordPack.getImpostorHint());
            } else {
                // Jogador normal - recebe a palavra
                info = PrivatePlayerInfo.forNormalPlayer(wordPack.getWord());
            }

            messagingTemplate.convertAndSend(
                    "/topic/room/" + room.getCode() + "/private/" + player.getId(),
                    info
            );
        }
    }

    /**
     * Envia o resultado da rodada para todos os jogadores.
     */
    private void sendRoundResult(Room room) {
        Round currentRound = room.getCurrentRoundObject();
        if (currentRound == null) {
            return;
        }

        RoundResultResponse response = RoundResultResponse.fromRound(room, currentRound);
        messagingTemplate.convertAndSend(
                "/topic/room/" + room.getCode() + "/result",
                response
        );
    }

    /**
     * Envia mensagem de erro para a sala.
     */
    private void sendError(String roomCode, String errorMessage) {
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomCode + "/error",
                errorMessage
        );
    }
}