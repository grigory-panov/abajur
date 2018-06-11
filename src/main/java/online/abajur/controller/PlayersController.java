package online.abajur.controller;

import online.abajur.AppException;
import online.abajur.domain.Player;
import online.abajur.service.MozgvaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.time.format.DateTimeFormatter;
import java.util.Collection;

import static online.abajur.repository.SettingsRepository.VERSION;

@Controller
public class PlayersController {
    private static final Logger logger = LoggerFactory.getLogger(PlayersController.class);

    @Autowired
    private MozgvaService mozgvaService;

    @RequestMapping(method = RequestMethod.GET, value = "/players/{gameId}")
    public String getIndex(@PathVariable(name = "gameId") Integer gameId, Model model) throws AppException {
        Collection<Player> players = mozgvaService.getGamePlayers(gameId);
        model.addAttribute("data", players);
        model.addAttribute("gameId", gameId);
        model.addAttribute("lastUpdate", players.isEmpty() ? "" : DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss ZZZ").format(players.iterator().next().getLastUpdateDate()));
        model.addAttribute("version", VERSION);
        return "players";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/players/{gameId}")
    public String postIndex(@PathVariable(name = "gameId") Integer gameId, Model model){
        mozgvaService.clearPlayersCache();
        return "redirect:/players/" + gameId;
    }

}
