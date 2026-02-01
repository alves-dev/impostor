// Variáveis globais
let stompClient = null;
let currentGameState = 'WAITING_PLAYERS';
let roomState = null;

// Elementos do DOM
const elements = {
    // Estados do jogo
    waitingPlayers: document.getElementById('waitingPlayers'),
    roundStarted: document.getElementById('roundStarted'),
    voting: document.getElementById('voting'),
    roundFinished: document.getElementById('roundFinished'),
    gameFinished: document.getElementById('gameFinished'),

    // Botões (owner)
    btnStartGame: document.getElementById('btnStartGame'),
    btnStartVoting: document.getElementById('btnStartVoting'),
    btnNextRound: document.getElementById('btnNextRound'),

    // Status de conexão
    statusIndicator: document.getElementById('statusIndicator'),
    statusText: document.getElementById('statusText'),

    // Listas de jogadores
    playersList: document.getElementById('playersList'),
    playersListRound: document.getElementById('playersListRound'),

    // Informações da rodada
    currentRound: document.getElementById('currentRound'),
    totalRounds: document.getElementById('totalRounds'),

    // Palavra/Dica do jogador
    normalPlayerInfo: document.getElementById('normalPlayerInfo'),
    impostorInfo: document.getElementById('impostorInfo'),
    playerWord: document.getElementById('playerWord'),
    impostorHint: document.getElementById('impostorHint'),

    // Votação
    votingPlayers: document.getElementById('votingPlayers'),
    voteCount: document.getElementById('voteCount'),
    totalPlayers: document.getElementById('totalPlayers'),

    // Resultados
    resultWord: document.getElementById('resultWord'),
    resultImpostor: document.getElementById('resultImpostor'),
    resultMostVoted: document.getElementById('resultMostVoted'),
    impostorDiscovered: document.getElementById('impostorDiscovered'),
    impostorNotDiscovered: document.getElementById('impostorNotDiscovered'),
    scoreTableBody: document.getElementById('scoreTableBody'),
    finalScoreTableBody: document.getElementById('finalScoreTableBody')
};

// Conectar ao WebSocket quando a página carregar
window.addEventListener('DOMContentLoaded', function() {
    connectWebSocket();
    setupEventListeners();
});

// Conectar ao WebSocket
function connectWebSocket() {
    updateConnectionStatus('connecting', 'Conectando...');

    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function(frame) {
        console.log('Conectado: ' + frame);
        updateConnectionStatus('connected', 'Conectado');

        // Inscrever-se nos tópicos
        subscribeToTopics();

    }, function(error) {
        console.error('Erro na conexão:', error);
        updateConnectionStatus('disconnected', 'Desconectado');

        // Tentar reconectar após 3 segundos
        setTimeout(connectWebSocket, 3000);
    });
}

// Inscrever-se nos tópicos WebSocket
function subscribeToTopics() {
    // Tópico geral da sala
    stompClient.subscribe('/topic/room/' + ROOM_CODE, function(message) {
        const state = JSON.parse(message.body);
        handleRoomStateUpdate(state);
    });

    // Tópico privado do jogador
    stompClient.subscribe('/topic/room/' + ROOM_CODE + '/private/' + PLAYER_ID, function(message) {
        const info = JSON.parse(message.body);
        handlePrivatePlayerInfo(info);
    });

    // Tópico de resultados
    stompClient.subscribe('/topic/room/' + ROOM_CODE + '/result', function(message) {
        const result = JSON.parse(message.body);
        handleRoundResult(result);
    });

    // Tópico de erros
    stompClient.subscribe('/topic/room/' + ROOM_CODE + '/error', function(message) {
        const error = message.body;
        alert('Erro: ' + error);
    });
}

// Configurar event listeners dos botões
function setupEventListeners() {
    if (elements.btnStartGame) {
        elements.btnStartGame.addEventListener('click', startGame);
    }

    if (elements.btnStartVoting) {
        elements.btnStartVoting.addEventListener('click', startVoting);
    }

    if (elements.btnNextRound) {
        elements.btnNextRound.addEventListener('click', nextRound);
    }
}

// Atualizar status de conexão
function updateConnectionStatus(status, text) {
    elements.statusIndicator.className = 'status-indicator ' + status;
    elements.statusText.textContent = text;
}

// Atualizar estado da sala
function handleRoomStateUpdate(state) {
    console.log('Estado da sala atualizado:', state);
    roomState = state;
    currentGameState = state.gameState;

    // Atualizar UI baseado no estado
    updateGameStateUI(state.gameState);

    // Atualizar informações gerais
    updatePlayersList(state.players);
    updateRoundInfo(state.currentRound, state.totalRounds);

    // Atualizar controles do owner
    updateOwnerControls(state);
}

// Atualizar UI baseado no estado do jogo
function updateGameStateUI(gameState) {
    // Esconder todos os estados
    Object.values(elements).forEach(el => {
        if (el && el.classList && el.classList.contains('game-state')) {
            el.style.display = 'none';
        }
    });

    // Mostrar o estado atual
    switch(gameState) {
        case 'WAITING_PLAYERS':
            elements.waitingPlayers.style.display = 'block';
            break;
        case 'ROUND_STARTED':
            elements.roundStarted.style.display = 'block';
            break;
        case 'VOTING':
            elements.voting.style.display = 'block';
            renderVotingButtons();
            break;
        case 'ROUND_FINISHED':
            elements.roundFinished.style.display = 'block';
            break;
        case 'GAME_FINISHED':
            elements.gameFinished.style.display = 'block';
            renderFinalScoreboard();
            break;
    }
}

// Atualizar lista de jogadores
function updatePlayersList(players) {
    if (!players) return;

    // Lista na tela de espera
    if (elements.playersList) {
        elements.playersList.innerHTML = '';
        players.forEach(player => {
            const li = document.createElement('li');
            li.innerHTML = `
                <span>${player.name} ${player.owner ? '👑' : ''}</span>
                <span class="player-score">${player.score} pts</span>
            `;
            elements.playersList.appendChild(li);
        });
    }

    // Lista durante a rodada
    if (elements.playersListRound) {
        elements.playersListRound.innerHTML = '';
        players.forEach(player => {
            const li = document.createElement('li');
            li.innerHTML = `
                <span>${player.name}</span>
                <span>${player.hasVoted ? '<span class="vote-indicator">✓ Votou</span>' : ''}</span>
            `;
            elements.playersListRound.appendChild(li);
        });
    }

    // Contadores de votação
    if (elements.totalPlayers) {
        elements.totalPlayers.textContent = players.length;
    }

    const votedCount = players.filter(p => p.hasVoted).length;
    if (elements.voteCount) {
        elements.voteCount.textContent = votedCount;
    }
}

// Atualizar informações da rodada
function updateRoundInfo(currentRound, totalRounds) {
    if (elements.currentRound) {
        elements.currentRound.textContent = currentRound;
    }
    if (elements.totalRounds) {
        elements.totalRounds.textContent = totalRounds;
    }
}

// Atualizar controles do owner
function updateOwnerControls(state) {
    if (!IS_OWNER) return;

    // Botão de iniciar jogo
    if (elements.btnStartGame) {
        elements.btnStartGame.disabled = state.players.length < 3;
    }
}

// Lidar com informações privadas do jogador
function handlePrivatePlayerInfo(info) {
    console.log('Informação privada recebida:', info);

    if (info.impostor) {
        // Sou o impostor
        elements.normalPlayerInfo.style.display = 'none';
        elements.impostorInfo.style.display = 'block';
        elements.impostorHint.textContent = info.hint;
    } else {
        // Sou um jogador normal
        elements.normalPlayerInfo.style.display = 'block';
        elements.impostorInfo.style.display = 'none';
        elements.playerWord.textContent = info.word;
    }
}

// Lidar com resultado da rodada
function handleRoundResult(result) {
    console.log('Resultado da rodada:', result);

    // Mostrar informações do resultado
    elements.resultWord.textContent = result.word;
    elements.resultImpostor.textContent = result.impostorName;
    elements.resultMostVoted.textContent = result.mostVotedName || 'Empate';

    // Mostrar se o impostor foi descoberto
    if (result.impostorDiscovered) {
        elements.impostorDiscovered.style.display = 'block';
        elements.impostorNotDiscovered.style.display = 'none';
    } else {
        elements.impostorDiscovered.style.display = 'none';
        elements.impostorNotDiscovered.style.display = 'block';
    }

    // Atualizar placar
    updateScoreboard();
}

// Renderizar botões de votação
function renderVotingButtons() {
    if (!roomState || !elements.votingPlayers) return;

    elements.votingPlayers.innerHTML = '';

    roomState.players.forEach(player => {
        // Não pode votar em si mesmo
        if (player.id === PLAYER_ID) return;

        const button = document.createElement('button');
        button.className = 'vote-button';
        button.textContent = player.name;
        button.onclick = () => vote(player.id);

        // Marcar se já votou neste jogador
        const myPlayer = roomState.players.find(p => p.id === PLAYER_ID);
        if (myPlayer && myPlayer.hasVoted) {
            button.disabled = true;
        }

        elements.votingPlayers.appendChild(button);
    });
}

// Atualizar placar
function updateScoreboard() {
    if (!roomState || !elements.scoreTableBody) return;

    elements.scoreTableBody.innerHTML = '';

    // Ordenar por pontuação
    const sortedPlayers = [...roomState.players].sort((a, b) => b.score - a.score);

    sortedPlayers.forEach(player => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${player.name} ${player.owner ? '👑' : ''}</td>
            <td><strong>${player.score}</strong></td>
        `;
        elements.scoreTableBody.appendChild(tr);
    });
}

// Renderizar placar final
function renderFinalScoreboard() {
    if (!roomState || !elements.finalScoreTableBody) return;

    elements.finalScoreTableBody.innerHTML = '';

    // Ordenar por pontuação
    const sortedPlayers = [...roomState.players].sort((a, b) => b.score - a.score);

    sortedPlayers.forEach((player, index) => {
        const tr = document.createElement('tr');
        let medal = '';
        if (index === 0) medal = '🥇';
        else if (index === 1) medal = '🥈';
        else if (index === 2) medal = '🥉';

        tr.innerHTML = `
            <td>${medal} ${index + 1}º</td>
            <td>${player.name} ${player.owner ? '👑' : ''}</td>
            <td><strong>${player.score}</strong></td>
        `;
        elements.finalScoreTableBody.appendChild(tr);
    });
}

// ========== AÇÕES DO JOGO ==========

function startGame() {
    if (!stompClient) return;

    stompClient.send('/app/start-game', {}, JSON.stringify({
        roomCode: ROOM_CODE
    }));
}

function startVoting() {
    if (!stompClient) return;

    stompClient.send('/app/start-voting', {}, JSON.stringify({
        roomCode: ROOM_CODE
    }));
}

function vote(votedPlayerId) {
    if (!stompClient) return;

    stompClient.send('/app/vote', {}, JSON.stringify({
        roomCode: ROOM_CODE,
        playerId: PLAYER_ID,
        votedPlayerId: votedPlayerId
    }));
}

function nextRound() {
    if (!stompClient) return;

    stompClient.send('/app/next-round', {}, JSON.stringify({
        roomCode: ROOM_CODE
    }));
}

// Desconectar ao sair da página
window.addEventListener('beforeunload', function() {
    if (stompClient) {
        stompClient.disconnect();
    }
});