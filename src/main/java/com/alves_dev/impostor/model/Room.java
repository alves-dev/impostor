package com.alves_dev.impostor.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Representa uma sala de jogo.
 */
public class Room {

    private UUID id;
    private String name;
    private String code;
    private UUID ownerId;
    private List<Player> players;
    private GameState gameState;
    private int currentRound;
    private int totalRounds;
    private List<Round> roundHistory;
    private WordPack currentWordPack;
    private UUID currentImpostorId;

    private static final String CODE_CHARS = "ABCDEFGHIJKLMNPQRSTUVWXYZ123456789";
    private static final int CODE_LENGTH = 4;
    private static final Random random = new Random();

    public Room() {
        this.id = UUID.randomUUID();
        this.code = generateCode();
        this.players = new ArrayList<>();
        this.gameState = GameState.WAITING_PLAYERS;
        this.currentRound = 0;
        this.totalRounds = 0;
        this.roundHistory = new ArrayList<>();
    }

    public Room(String name, UUID ownerId) {
        this();
        this.name = name;
        this.ownerId = ownerId;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public GameState getGameState() {
        return gameState;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public int getTotalRounds() {
        return totalRounds;
    }

    public List<Round> getRoundHistory() {
        return roundHistory;
    }

    public WordPack getCurrentWordPack() {
        return currentWordPack;
    }

    public UUID getCurrentImpostorId() {
        return currentImpostorId;
    }

    // Setters
    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
    }

    public void setTotalRounds(int totalRounds) {
        this.totalRounds = totalRounds;
    }

    public void setRoundHistory(List<Round> roundHistory) {
        this.roundHistory = roundHistory;
    }

    public void setCurrentWordPack(WordPack currentWordPack) {
        this.currentWordPack = currentWordPack;
    }

    public void setCurrentImpostorId(UUID currentImpostorId) {
        this.currentImpostorId = currentImpostorId;
    }

    // Métodos auxiliares
    public void addPlayer(Player player) {
        this.players.add(player);
    }

    public void removePlayer(UUID playerId) {
        this.players.removeIf(p -> p.getId().equals(playerId));
    }

    public Player getPlayerById(UUID playerId) {
        return players.stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst()
                .orElse(null);
    }

    public Player getPlayerBySessionId(String sessionId) {
        return players.stream()
                .filter(p -> p.getSessionId().equals(sessionId))
                .findFirst()
                .orElse(null);
    }

    public Player getOwner() {
        return players.stream()
                .filter(Player::isOwner)
                .findFirst()
                .orElse(null);
    }

    public int getPlayerCount() {
        return players.size();
    }

    public Round getCurrentRoundObject() {
        if (roundHistory.isEmpty()) {
            return null;
        }
        return roundHistory.get(roundHistory.size() - 1);
    }

    public void addRound(Round round) {
        this.roundHistory.add(round);
    }

    private String generateCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
        }
        return code.toString();
    }

    @Override
    public String toString() {
        return "Room{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", players=" + players.size() +
                ", gameState=" + gameState +
                ", currentRound=" + currentRound +
                '}';
    }
}