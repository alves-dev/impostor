package com.alves_dev.impostor.dto;

import java.util.UUID;

/**
 * DTO para requisições de voto via WebSocket.
 */
public class VoteRequest {
    private String roomCode;
    private UUID playerId;
    private UUID votedPlayerId;

    public VoteRequest() {
    }

    public VoteRequest(String roomCode, UUID playerId, UUID votedPlayerId) {
        this.roomCode = roomCode;
        this.playerId = playerId;
        this.votedPlayerId = votedPlayerId;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public UUID getVotedPlayerId() {
        return votedPlayerId;
    }

    public void setVotedPlayerId(UUID votedPlayerId) {
        this.votedPlayerId = votedPlayerId;
    }
}