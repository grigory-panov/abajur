package online.abajur.scheduler;

import online.abajur.AppException;
import online.abajur.domain.NextGame;
import online.abajur.service.MozgvaService;
import online.abajur.service.StatisticService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class GetGameResultTask {

    private static final Logger logger = LoggerFactory.getLogger(GetGameResultTask.class);


    @Autowired
    private StatisticService statisticService;

    @Autowired
    private MozgvaService mozgvaService;

    @Scheduled(fixedDelay = 3600000)
    public void run(){
        logger.debug("run GetGameResultTask...");

        List<NextGame> games = statisticService.getGamesWithoutStatistic();
        if(!games.isEmpty()){
            mozgvaService.clearGamesCache();
        }
        for(NextGame game : games){
            logger.debug("get info about game " + game.getId());
            try {
                mozgvaService.getGameStatistic(game.getId());
            }catch (AppException ex){
                logger.error(ex.getMessage(), ex);
            }
        }
        logger.debug("run GetGameResultTask...OK");
    }
}
