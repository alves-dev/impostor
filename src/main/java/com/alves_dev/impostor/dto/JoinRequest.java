package com.alves_dev.impostor.dto;

import java.util.UUID;

/**
 * DTO para requisições de entrada na sala via WebSocket.
 */
public class JoinRequest {
    private String roomCode;
    private String playerName;

    public JoinRequest() {
    }

    public JoinRequest(String roomCode, String playerName) {
        this.roomCode = roomCode;
        this.playerName = playerName;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}