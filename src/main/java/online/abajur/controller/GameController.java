package online.abajur.controller;

import online.abajur.AppException;
import online.abajur.domain.GameStatistic;
import online.abajur.service.MozgvaService;
import online.abajur.service.StatisticService;
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
public class GameController {

    private static final Logger logger = LoggerFactory.getLogger(GameController.class);

    @Autowired
    private MozgvaService mozgvaService;

    @Autowired
    private StatisticService statisticService;


    @RequestMapping(method = RequestMethod.GET, value = "/game/{gameId}")
    public String getIndex(@PathVariable(name = "gameId") Integer gameId, Model model) throws AppException {
        Collection<GameStatistic> statistic = mozgvaService.getGameStatistic(gameId);
        model.addAttribute("data", statistic);
        model.addAttribute("gameId", gameId);
        model.addAttribute("lastUpdate", statistic.isEmpty() ? "" : DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss ZZZ").format(statistic.iterator().next().getUpdateDate()));
        model.addAttribute("version", VERSION);
        return "game";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/game/{gameId}")
    public String postIndex(@PathVariable(name = "gameId") Integer gameId, Model model){
        mozgvaService.clearGamesCache();
        return "redirect:/game/" + gameId;
    }
}
