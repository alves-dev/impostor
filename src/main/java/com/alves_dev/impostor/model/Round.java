package com.alves_dev.impostor.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Representa uma rodada do jogo.
 */
public class Round {

    private int roundNumber;
    private UUID impostorId;
    private Map<UUID, UUID> votes; // playerId -> votedPlayerId
    private boolean finished;
    private WordPack wordPack;

    public Round() {
        this.votes = new HashMap<>();
        this.finished = false;
    }

    public Round(int roundNumber, UUID impostorId) {
        this();
        this.roundNumber = roundNumber;
        this.impostorId = impostorId;
    }

    // Getters
    public int getRoundNumber() {
        return roundNumber;
    }

    public UUID getImpostorId() {
        return impostorId;
    }

    public Map<UUID, UUID> getVotes() {
        return votes;
    }

    public boolean isFinished() {
        return finished;
    }

    public WordPack getWordPack() {
        return wordPack;
    }

    // Setters
    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public void setImpostorId(UUID impostorId) {
        this.impostorId = impostorId;
    }

    public void setVotes(Map<UUID, UUID> votes) {
        this.votes = votes;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public void setWordPack(WordPack wordPack) {
        this.wordPack = wordPack;
    }

    // Métodos auxiliares
    public void registerVote(UUID playerId, UUID votedPlayerId) {
        this.votes.put(playerId, votedPlayerId);
    }

    public boolean hasPlayerVoted(UUID playerId) {
        return this.votes.containsKey(playerId);
    }

    public int getTotalVotes() {
        return this.votes.size();
    }

    public UUID getMostVotedPlayer() {
        Map<UUID, Integer> voteCounts = new HashMap<>();

        for (UUID votedPlayer : votes.values()) {
            voteCounts.put(votedPlayer, voteCounts.getOrDefault(votedPlayer, 0) + 1);
        }

        UUID mostVoted = null;
        int maxVotes = 0;

        for (Map.Entry<UUID, Integer> entry : voteCounts.entrySet()) {
            if (entry.getValue() > maxVotes) {
                maxVotes = entry.getValue();
                mostVoted = entry.getKey();
            }
        }

        return mostVoted;
    }

    public int getVotesFor(UUID playerId) {
        return (int) votes.values().stream()
                .filter(votedId -> votedId.equals(playerId))
                .count();
    }

    @Override
    public String toString() {
        return "Round{" +
                "roundNumber=" + roundNumber +
                ", impostorId=" + impostorId +
                ", votes=" + votes.size() +
                ", finished=" + finished +
                '}';
    }
}
