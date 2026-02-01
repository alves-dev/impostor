package com.alves_dev.impostor.model;

/**
 * Encapsula a palavra do jogo e a dica para o impostor.
 */
public class WordPack {

    private String word;
    private String impostorHint;

    public WordPack() {
    }

    public WordPack(String word, String impostorHint) {
        this.word = word;
        this.impostorHint = impostorHint;
    }

    // Getters
    public String getWord() {
        return word;
    }

    public String getImpostorHint() {
        return impostorHint;
    }

    // Setters
    public void setWord(String word) {
        this.word = word;
    }

    public void setImpostorHint(String impostorHint) {
        this.impostorHint = impostorHint;
    }

    @Override
    public String toString() {
        return "WordPack{" +
                "word='" + word + '\'' +
                ", impostorHint='" + impostorHint + '\'' +
                '}';
    }
}
