package com.alves_dev.impostor.dto;

/**
 * DTO para enviar informações privadas ao jogador.
 * Contém a palavra (para jogadores normais) ou a dica (para o impostor).
 */
public class PrivatePlayerInfo {
    private boolean isImpostor;
    private String word;
    private String hint;

    public PrivatePlayerInfo() {
    }

    public PrivatePlayerInfo(boolean isImpostor, String word, String hint) {
        this.isImpostor = isImpostor;
        this.word = word;
        this.hint = hint;
    }

    /**
     * Cria informação para jogador normal (recebe a palavra).
     */
    public static PrivatePlayerInfo forNormalPlayer(String word) {
        return new PrivatePlayerInfo(false, word, null);
    }

    /**
     * Cria informação para impostor (recebe apenas a dica).
     */
    public static PrivatePlayerInfo forImpostor(String hint) {
        return new PrivatePlayerInfo(true, null, hint);
    }

    public boolean isImpostor() {
        return isImpostor;
    }

    public void setImpostor(boolean impostor) {
        isImpostor = impostor;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }
}