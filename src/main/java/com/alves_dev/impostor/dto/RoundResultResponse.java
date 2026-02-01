package com.alves_dev.impostor.dto;

import com.alves_dev.impostor.model.Room;
import com.alves_dev.impostor.model.Round;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * DTO para enviar o resultado de uma rodada aos clientes.
 */
public class RoundResultResponse {
    private int roundNumber;
    private UUID impostorId;
    private String impostorName;
    private String word;
    private UUID mostVotedId;
    private String mostVotedName;
    private boolean impostorDiscovered;
    private Map<UUID, Integer> voteCounts;

    public RoundResultResponse() {
        this.voteCounts = new HashMap<>();
    }

    public static RoundResultResponse fromRound(Room room, Round round) {
        RoundResultResponse response = new RoundResultResponse();
        response.setRoundNumber(round.getRoundNumber());
        response.setImpostorId(round.getImpostorId());

        var impostor = room.getPlayerById(round.getImpostorId());
        if (impostor != null) {
            response.setImpostorName(impostor.getName());
        }

        if (round.getWordPack() != null) {
            response.setWord(round.getWordPack().getWord());
        }

        UUID mostVotedId = round.getMostVotedPlayer();
        response.setMostVotedId(mostVotedId);

        if (mostVotedId != null) {
            var mostVoted = room.getPlayerById(mostVotedId);
            if (mostVoted != null) {
                response.setMostVotedName(mostVoted.getName());
            }
            response.setImpostorDiscovered(mostVotedId.equals(round.getImpostorId()));
        }

        // Conta votos por jogador
        Map<UUID, Integer> counts = new HashMap<>();
        for (var player : room.getPlayers()) {
            counts.put(player.getId(), round.getVotesFor(player.getId()));
        }
        response.setVoteCounts(counts);

        return response;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public UUID getImpostorId() {
        return impostorId;
    }

    public void setImpostorId(UUID impostorId) {
        this.impostorId = impostorId;
    }

    public String getImpostorName() {
        return impostorName;
    }

    public void setImpostorName(String impostorName) {
        this.impostorName = impostorName;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public UUID getMostVotedId() {
        return mostVotedId;
    }

    public void setMostVotedId(UUID mostVotedId) {
        this.mostVotedId = mostVotedId;
    }

    public String getMostVotedName() {
        return mostVotedName;
    }

    public void setMostVotedName(String mostVotedName) {
        this.mostVotedName = mostVotedName;
    }

    public boolean isImpostorDiscovered() {
        return impostorDiscovered;
    }

    public void setImpostorDiscovered(boolean impostorDiscovered) {
        this.impostorDiscovered = impostorDiscovered;
    }

    public Map<UUID, Integer> getVoteCounts() {
        return voteCounts;
    }

    public void setVoteCounts(Map<UUID, Integer> voteCounts) {
        this.voteCounts = voteCounts;
    }
}