package online.abajur.service;

import online.abajur.domain.GameStatistic;
import online.abajur.domain.HistoryGame;
import online.abajur.domain.NextGame;
import online.abajur.domain.TeamStatistic;
import online.abajur.repository.StatisticRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@CacheConfig(cacheNames = "statistic")
public class StatisticService {

    @Autowired
    private StatisticRepository statisticRepository;

    @Autowired
    private CacheManager cacheManager;

    @Cacheable
    public List<TeamStatistic> getTeamStatistic(int teamId){
        return statisticRepository.getTeamStatistic(teamId);
    }

    public List<GameStatistic> getGameStatistic(int gameId){
        return statisticRepository.getGameStatistic(gameId);
    }

    @CacheEvict(allEntries = true, condition = "#result == true")
    public boolean saveTeamStatistic(TeamStatistic teamStatistic) {
        return statisticRepository.saveTeamStatistic(teamStatistic);
    }

    @Transactional
    public void saveGameStatistic(Map<Integer, GameStatistic> results, int gameId) {
        List<GameStatistic> old = statisticRepository.getGameStatistic(gameId);
        for(GameStatistic gs : old){
            if(!gs.equals(results.get(gs.getTeamId()))){
                statisticRepository.updateGameStatistic(results.get(gs.getTeamId()));
            }
            results.remove(gs.getTeamId());
        }
        for(GameStatistic gs : results.values()){
            statisticRepository.insertGameStatistic(gs);
        }
    }

    @Transactional
    public void saveGame(NextGame game){
        if(statisticRepository.getGame(game.getId()) == null) {
            statisticRepository.saveGame(game);
        }
    }


    @Transactional
    public List<HistoryGame> getHistoryGames(int teamId) {
        return statisticRepository.getHistoryGames(teamId);
    }

    @Transactional
    public List<NextGame> getGamesWithoutStatistic() {
        return statisticRepository.getGamesWithoutStatistic();
    }
}
