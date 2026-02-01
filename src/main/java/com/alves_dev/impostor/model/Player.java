package com.alves_dev.impostor.model;

import java.util.UUID;

/**
 * Representa um jogador dentro de uma sala de jogo.
 */
public class Player {

    private UUID id;
    private String name;
    private String sessionId;
    private boolean isOwner;
    private int score;
    private boolean hasBeenImpostor;
    private UUID currentVote;
    private boolean hasVoted;

    public Player() {
        this.id = UUID.randomUUID();
        this.score = 0;
        this.hasBeenImpostor = false;
        this.hasVoted = false;
    }

    public Player(String name, String sessionId, boolean isOwner) {
        this();
        this.name = name;
        this.sessionId = sessionId;
        this.isOwner = isOwner;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSessionId() {
        return sessionId;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public int getScore() {
        return score;
    }

    public boolean hasBeenImpostor() {
        return hasBeenImpostor;
    }

    public UUID getCurrentVote() {
        return currentVote;
    }

    public boolean hasVoted() {
        return hasVoted;
    }

    // Setters
    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setOwner(boolean owner) {
        isOwner = owner;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setHasBeenImpostor(boolean hasBeenImpostor) {
        this.hasBeenImpostor = hasBeenImpostor;
    }

    public void setCurrentVote(UUID currentVote) {
        this.currentVote = currentVote;
    }

    public void setHasVoted(boolean hasVoted) {
        this.hasVoted = hasVoted;
    }

    // Métodos auxiliares
    public void addScore(int points) {
        this.score += points;
    }

    public void resetVote() {
        this.currentVote = null;
        this.hasVoted = false;
    }

    public void vote(UUID playerId) {
        this.currentVote = playerId;
        this.hasVoted = true;
    }

    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", isOwner=" + isOwner +
                ", score=" + score +
                ", hasBeenImpostor=" + hasBeenImpostor +
                '}';
    }
}