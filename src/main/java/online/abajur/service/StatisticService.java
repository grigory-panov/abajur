package online.abajur.service;

import online.abajur.domain.TeamStatistic;
import online.abajur.repository.StatisticRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
@CacheConfig(cacheNames = "statistic")
public class StatisticService {

    @Autowired
    private StatisticRepository statisticRepository;

    @Autowired
    private CacheManager cacheManager;

    @Cacheable
    public List<TeamStatistic> getTeamStatistic(){
        return statisticRepository.getTeamStatistic();
    }

    @CacheEvict(allEntries = true, condition = "#result == true")
    public boolean saveTeamStatistic(TeamStatistic teamStatistic) {
        return statisticRepository.saveTeamStatistic(teamStatistic);
    }
}
