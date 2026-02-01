package com.alves_dev.impostor.dto;

/**
 * DTO para requisições de ações na sala (start-round, start-voting, next-round).
 */
public class RoomActionRequest {
    private String roomCode;

    public RoomActionRequest() {
    }

    public RoomActionRequest(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }
}