package com.alves_dev.impostor.controller;

import com.alves_dev.impostor.model.Room;
import com.alves_dev.impostor.service.RoomManager;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller responsável pela renderização da sala de jogo.
 */
@Controller
public class RoomController {

    @Autowired
    private RoomManager roomManager;

    /**
     * Renderiza a página da sala de jogo.
     * @param code Código da sala
     * @param session Sessão HTTP
     * @param model Model para enviar dados ao Thymeleaf
     * @param redirectAttributes Atributos para redirecionamento
     * @return Template room.html ou redirecionamento para home em caso de erro
     */
    @GetMapping("/room/{code}")
    public String showRoom(
            @PathVariable("code") String code,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            // Valida se o código foi fornecido
            if (code == null || code.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Código de sala inválido.");
                return "redirect:/";
            }

            // Busca a sala
            Room room = roomManager.getRoomByCode(code.toUpperCase());

            // Obtém informações da sessão
            String playerName = (String) session.getAttribute("playerName");
            String playerId = (String) session.getAttribute("playerId");
            Boolean isOwner = (Boolean) session.getAttribute("isOwner");

            // Valida se o jogador está na sessão
            if (playerName == null || playerId == null) {
                redirectAttributes.addFlashAttribute("error", "Você precisa entrar na sala primeiro.");
                return "redirect:/";
            }

            // Valida se o jogador realmente está na sala
            var player = room.getPlayerById(java.util.UUID.fromString(playerId));
            if (player == null) {
                redirectAttributes.addFlashAttribute("error", "Você não está nesta sala.");
                session.invalidate();
                return "redirect:/";
            }

            // Envia dados para o template
            model.addAttribute("roomCode", room.getCode());
            model.addAttribute("roomName", room.getName());
            model.addAttribute("playerName", playerName);
            model.addAttribute("playerId", playerId);
            model.addAttribute("isOwner", isOwner != null && isOwner);
            model.addAttribute("gameState", room.getGameState().toString());
            model.addAttribute("playerCount", room.getPlayerCount());

            return "room";

        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao acessar a sala: " + e.getMessage());
            return "redirect:/";
        }
    }
}