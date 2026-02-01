package com.alves_dev.impostor.controller;

import com.alves_dev.impostor.model.Room;
import com.alves_dev.impostor.service.RoomManager;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller responsável pela navegação inicial da aplicação.
 * Gerencia a página inicial, criação e entrada em salas.
 */
@Controller
public class HomeController {

    @Autowired
    private RoomManager roomManager;

    /**
     * Exibe a página inicial.
     * @return Template home.html
     */
    @GetMapping("/")
    public String home() {
        return "home";
    }

    /**
     * Cria uma nova sala de jogo.
     * @param roomName Nome da sala
     * @param playerName Nome do jogador (dono da sala)
     * @param session Sessão HTTP
     * @param redirectAttributes Atributos para redirecionamento
     * @return Redirecionamento para a sala criada
     */
    @PostMapping("/room/create")
    public String createRoom(
            @RequestParam("roomName") String roomName,
            @RequestParam("playerName") String playerName,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            // Valida os parâmetros
            if (roomName == null || roomName.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "O nome da sala é obrigatório.");
                return "redirect:/";
            }

            if (playerName == null || playerName.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Seu nome é obrigatório.");
                return "redirect:/";
            }

            // Cria a sala
            String sessionId = session.getId();
            Room room = roomManager.createRoom(roomName.trim(), playerName.trim(), sessionId);

            // Armazena informações na sessão
            session.setAttribute("roomCode", room.getCode());
            session.setAttribute("playerName", playerName.trim());
            session.setAttribute("playerId", room.getOwner().getId().toString());
            session.setAttribute("isOwner", true);

            // Redireciona para a sala
            return "redirect:/room/" + room.getCode();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao criar sala: " + e.getMessage());
            return "redirect:/";
        }
    }

    /**
     * Entra em uma sala existente.
     * @param roomCode Código da sala
     * @param playerName Nome do jogador
     * @param session Sessão HTTP
     * @param redirectAttributes Atributos para redirecionamento
     * @return Redirecionamento para a sala
     */
    @PostMapping("/room/join")
    public String joinRoom(
            @RequestParam("roomCode") String roomCode,
            @RequestParam("playerName") String playerName,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            // Valida os parâmetros
            if (roomCode == null || roomCode.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "O código da sala é obrigatório.");
                return "redirect:/";
            }

            if (playerName == null || playerName.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Seu nome é obrigatório.");
                return "redirect:/";
            }

            // Entra na sala
            String sessionId = session.getId();
            Room room = roomManager.joinRoom(roomCode.trim().toUpperCase(), playerName.trim(), sessionId);

            // Busca o jogador que acabou de entrar
            var player = room.getPlayerBySessionId(sessionId);

            // Armazena informações na sessão
            session.setAttribute("roomCode", room.getCode());
            session.setAttribute("playerName", playerName.trim());
            session.setAttribute("playerId", player.getId().toString());
            session.setAttribute("isOwner", false);

            // Redireciona para a sala
            return "redirect:/room/" + room.getCode();

        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao entrar na sala: " + e.getMessage());
            return "redirect:/";
        }
    }
}