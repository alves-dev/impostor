package com.alves_dev.impostor.dto;

import com.alves_dev.impostor.model.GameState;
import com.alves_dev.impostor.model.Player;
import com.alves_dev.impostor.model.Room;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * DTO para enviar o estado da sala aos clientes.
 */
public class RoomStateResponse {
    private String roomCode;
    private String roomName;
    private GameState gameState;
    private int currentRound;
    private int totalRounds;
    private List<PlayerInfo> players;
    private UUID currentImpostorId;

    public RoomStateResponse() {
    }

    public static RoomStateResponse fromRoom(Room room) {
        RoomStateResponse response = new RoomStateResponse();
        response.setRoomCode(room.getCode());
        response.setRoomName(room.getName());
        response.setGameState(room.getGameState());
        response.setCurrentRound(room.getCurrentRound());
        response.setTotalRounds(room.getTotalRounds());
        response.setCurrentImpostorId(room.getCurrentImpostorId());

        List<PlayerInfo> playerInfos = room.getPlayers().stream()
                .map(PlayerInfo::fromPlayer)
                .collect(Collectors.toList());
        response.setPlayers(playerInfos);

        return response;
    }

    // Getters and Setters
    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
    }

    public int getTotalRounds() {
        return totalRounds;
    }

    public void setTotalRounds(int totalRounds) {
        this.totalRounds = totalRounds;
    }

    public List<PlayerInfo> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerInfo> players) {
        this.players = players;
    }

    public UUID getCurrentImpostorId() {
        return currentImpostorId;
    }

    public void setCurrentImpostorId(UUID currentImpostorId) {
        this.currentImpostorId = currentImpostorId;
    }

    /**
     * Classe interna para informações do jogador.
     */
    public static class PlayerInfo {
        private UUID id;
        private String name;
        private boolean isOwner;
        private int score;
        private boolean hasVoted;

        public static PlayerInfo fromPlayer(Player player) {
            PlayerInfo info = new PlayerInfo();
            info.setId(player.getId());
            info.setName(player.getName());
            info.setOwner(player.isOwner());
            info.setScore(player.getScore());
            info.setHasVoted(player.hasVoted());
            return info;
        }

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isOwner() {
            return isOwner;
        }

        public void setOwner(boolean owner) {
            isOwner = owner;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public boolean isHasVoted() {
            return hasVoted;
        }

        public void setHasVoted(boolean hasVoted) {
            this.hasVoted = hasVoted;
        }
    }
}