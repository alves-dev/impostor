package com.alves_dev.impostor.service;

import com.alves_dev.impostor.model.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Serviço responsável por gerenciar todas as salas ativas do jogo.
 */
@Service
public class RoomManager {

    private final Map<String, Room> roomsByCode = new ConcurrentHashMap<>();
    private final Random random = new Random();

    // Lista de palavras disponíveis (pode ser expandida ou carregada de arquivo)
    private final List<WordPack> availableWordPacks = new ArrayList<>();

    public RoomManager() {
        initializeWordPacks();
    }

    /**
     * Cria uma nova sala.
     * @param roomName Nome da sala
     * @param ownerName Nome do dono da sala
     * @return A sala criada
     */
    public Room createRoom(String roomName, String ownerName, String sessionId) {
        Player owner = new Player(ownerName, sessionId, true);
        Room room = new Room(roomName, owner.getId());
        room.addPlayer(owner);

        // Garante que o código seja único
        while (roomsByCode.containsKey(room.getCode())) {
            room.setCode(generateUniqueCode());
        }

        roomsByCode.put(room.getCode(), room);
        return room;
    }

    /**
     * Permite que um jogador entre em uma sala existente.
     * @param roomCode Código da sala
     * @param playerName Nome do jogador
     * @param sessionId ID da sessão WebSocket
     * @return A sala atualizada
     * @throws IllegalStateException se a sala não existir ou já estiver em jogo
     */
    public Room joinRoom(String roomCode, String playerName, String sessionId) {
        Room room = roomsByCode.get(roomCode.toUpperCase());

        if (room == null) {
            throw new IllegalStateException("Sala não encontrada com o código: " + roomCode);
        }

        if (room.getGameState() != GameState.WAITING_PLAYERS) {
            throw new IllegalStateException("Não é possível entrar na sala. O jogo já começou.");
        }

        // Verifica se já existe um jogador com esse nome
        boolean nameExists = room.getPlayers().stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(playerName));

        if (nameExists) {
            throw new IllegalStateException("Já existe um jogador com esse nome na sala.");
        }

        Player newPlayer = new Player(playerName, sessionId, false);
        room.addPlayer(newPlayer);

        return room;
    }

    /**
     * Inicia o jogo na sala especificada.
     * @param roomCode Código da sala
     * @return A sala atualizada
     * @throws IllegalStateException se não houver jogadores suficientes
     */
    public Room startGame(String roomCode) {
        Room room = getRoomByCode(roomCode);

        if (room.getPlayerCount() < 3) {
            throw new IllegalStateException("É necessário pelo menos 3 jogadores para iniciar o jogo.");
        }

        // Define o número total de rodadas igual ao número de jogadores
        room.setTotalRounds(room.getPlayerCount());

        // Reseta o estado de todos os jogadores
        room.getPlayers().forEach(player -> {
            player.setHasBeenImpostor(false);
            player.setScore(0);
        });

        // Inicia a primeira rodada
        startNextRound(roomCode);

        return room;
    }

    /**
     * Inicia a próxima rodada do jogo.
     * @param roomCode Código da sala
     * @return A sala atualizada
     */
    public Room startNextRound(String roomCode) {
        Room room = getRoomByCode(roomCode);

        // Verifica se todos já foram impostores
        List<Player> eligiblePlayers = room.getPlayers().stream()
                .filter(p -> !p.hasBeenImpostor())
                .collect(Collectors.toList());

        if (eligiblePlayers.isEmpty()) {
            // Todos já foram impostores, encerra o jogo
            finishGame(roomCode);
            return room;
        }

        // Seleciona um impostor aleatório entre os elegíveis
        Player impostor = eligiblePlayers.get(random.nextInt(eligiblePlayers.size()));
        impostor.setHasBeenImpostor(true);

        // Incrementa a rodada atual
        room.setCurrentRound(room.getCurrentRound() + 1);

        // Seleciona um WordPack aleatório
        WordPack wordPack = availableWordPacks.get(random.nextInt(availableWordPacks.size()));
        room.setCurrentWordPack(wordPack);
        room.setCurrentImpostorId(impostor.getId());

        // Cria objeto Round e adiciona ao histórico
        Round round = new Round(room.getCurrentRound(), impostor.getId());
        round.setWordPack(wordPack);
        room.addRound(round);

        // Reseta os votos de todos os jogadores
        room.getPlayers().forEach(Player::resetVote);

        // Muda o estado para ROUND_STARTED
        room.setGameState(GameState.ROUND_STARTED);

        return room;
    }

    /**
     * Inicia a fase de votação.
     * @param roomCode Código da sala
     * @return A sala atualizada
     */
    public Room startVoting(String roomCode) {
        Room room = getRoomByCode(roomCode);

        if (room.getGameState() != GameState.ROUND_STARTED) {
            throw new IllegalStateException("A votação só pode ser iniciada durante uma rodada.");
        }

        room.setGameState(GameState.VOTING);
        return room;
    }

    /**
     * Registra o voto de um jogador.
     * @param roomCode Código da sala
     * @param playerId ID do jogador que está votando
     * @param votedPlayerId ID do jogador que recebeu o voto
     * @return A sala atualizada
     */
    public Room registerVote(String roomCode, UUID playerId, UUID votedPlayerId) {
        Room room = getRoomByCode(roomCode);

        if (room.getGameState() != GameState.VOTING) {
            throw new IllegalStateException("Não é possível votar fora da fase de votação.");
        }

        Player voter = room.getPlayerById(playerId);
        if (voter == null) {
            throw new IllegalStateException("Jogador não encontrado.");
        }

        if (voter.hasVoted()) {
            throw new IllegalStateException("Jogador já votou nesta rodada.");
        }

        Player votedPlayer = room.getPlayerById(votedPlayerId);
        if (votedPlayer == null) {
            throw new IllegalStateException("Jogador votado não encontrado.");
        }

        // Registra o voto
        voter.vote(votedPlayerId);

        Round currentRound = room.getCurrentRoundObject();
        currentRound.registerVote(playerId, votedPlayerId);

        // Verifica se todos votaram
        if (currentRound.getTotalVotes() == room.getPlayerCount()) {
            finishRound(roomCode);
        }

        return room;
    }

    /**
     * Finaliza a rodada atual e calcula a pontuação.
     * @param roomCode Código da sala
     * @return A sala atualizada
     */
    public Room finishRound(String roomCode) {
        Room room = getRoomByCode(roomCode);
        Round currentRound = room.getCurrentRoundObject();

        if (currentRound == null) {
            throw new IllegalStateException("Não há rodada em andamento.");
        }

        currentRound.setFinished(true);

        // Calcula pontuação
        UUID impostorId = currentRound.getImpostorId();
        UUID mostVotedId = currentRound.getMostVotedPlayer();

        if (mostVotedId != null && mostVotedId.equals(impostorId)) {
            // Impostor foi descoberto - quem votou nele ganha 1 ponto
            room.getPlayers().stream()
                    .filter(p -> votedForImpostor(p, currentRound, impostorId))
                    .forEach(p -> p.addScore(1));
        } else {
            // Impostor não foi descoberto - impostor ganha 2 pontos
            Player impostor = room.getPlayerById(impostorId);
            if (impostor != null) {
                impostor.addScore(2);
            }
        }

        room.setGameState(GameState.ROUND_FINISHED);
        return room;
    }

    /**
     * Finaliza o jogo.
     * @param roomCode Código da sala
     * @return A sala atualizada
     */
    public Room finishGame(String roomCode) {
        Room room = getRoomByCode(roomCode);
        room.setGameState(GameState.GAME_FINISHED);
        return room;
    }

    /**
     * Remove um jogador da sala (geralmente quando desconecta).
     * @param sessionId ID da sessão do jogador
     * @return A sala da qual o jogador foi removido, ou null se não encontrado
     */
    public Room removePlayer(String sessionId) {
        for (Room room : roomsByCode.values()) {
            Player player = room.getPlayerBySessionId(sessionId);
            if (player != null) {
                room.removePlayer(player.getId());

                // Se a sala ficou vazia, remove ela
                if (room.getPlayerCount() == 0) {
                    roomsByCode.remove(room.getCode());
                    return null;
                }

                // Se o dono saiu, transfere a propriedade
                if (player.isOwner() && room.getPlayerCount() > 0) {
                    room.getPlayers().get(0).setOwner(true);
                    room.setOwnerId(room.getPlayers().get(0).getId());
                }

                return room;
            }
        }
        return null;
    }

    /**
     * Obtém uma sala pelo código.
     * @param roomCode Código da sala
     * @return A sala encontrada
     * @throws IllegalStateException se a sala não existir
     */
    public Room getRoomByCode(String roomCode) {
        Room room = roomsByCode.get(roomCode.toUpperCase());
        if (room == null) {
            throw new IllegalStateException("Sala não encontrada: " + roomCode);
        }
        return room;
    }

    /**
     * Obtém todas as salas ativas.
     * @return Lista de todas as salas
     */
    public List<Room> getAllRooms() {
        return new ArrayList<>(roomsByCode.values());
    }

    // Métodos auxiliares privados

    private boolean votedForImpostor(Player player, Round round, UUID impostorId) {
        UUID vote = round.getVotes().get(player.getId());
        return vote != null && vote.equals(impostorId);
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = generateRandomCode();
        } while (roomsByCode.containsKey(code));
        return code;
    }

    private String generateRandomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    /**
     * Inicializa o conjunto de palavras disponíveis.
     * Pode ser expandido ou carregado de um arquivo externo.
     */
    private void initializeWordPacks() {
        // Animais
        availableWordPacks.add(new WordPack("Cachorro", "Animal de estimação"));
        availableWordPacks.add(new WordPack("Gato", "Animal de estimação"));
        availableWordPacks.add(new WordPack("Elefante", "Animal grande"));
        availableWordPacks.add(new WordPack("Leão", "Animal selvagem"));
        availableWordPacks.add(new WordPack("Pinguim", "Animal que vive no frio"));

        // Frutas
        availableWordPacks.add(new WordPack("Banana", "Fruta amarela"));
        availableWordPacks.add(new WordPack("Maçã", "Fruta vermelha ou verde"));
        availableWordPacks.add(new WordPack("Morango", "Fruta pequena e vermelha"));
        availableWordPacks.add(new WordPack("Abacaxi", "Fruta tropical espinhosa"));
        availableWordPacks.add(new WordPack("Melancia", "Fruta grande e verde por fora"));

        // Objetos
        availableWordPacks.add(new WordPack("Cadeira", "Móvel para sentar"));
        availableWordPacks.add(new WordPack("Mesa", "Móvel plano"));
        availableWordPacks.add(new WordPack("Computador", "Equipamento eletrônico"));
        availableWordPacks.add(new WordPack("Celular", "Dispositivo de comunicação"));
        availableWordPacks.add(new WordPack("Livro", "Objeto de leitura"));

        // Profissões
        availableWordPacks.add(new WordPack("Médico", "Profissão da saúde"));
        availableWordPacks.add(new WordPack("Professor", "Profissão da educação"));
        availableWordPacks.add(new WordPack("Bombeiro", "Profissão de emergência"));
        availableWordPacks.add(new WordPack("Cozinheiro", "Profissão da gastronomia"));
        availableWordPacks.add(new WordPack("Policial", "Profissão da segurança"));

        // Esportes
        availableWordPacks.add(new WordPack("Futebol", "Esporte com bola"));
        availableWordPacks.add(new WordPack("Basquete", "Esporte com cesta"));
        availableWordPacks.add(new WordPack("Natação", "Esporte aquático"));
        availableWordPacks.add(new WordPack("Vôlei", "Esporte com rede"));
        availableWordPacks.add(new WordPack("Tênis", "Esporte com raquete"));
    }
}