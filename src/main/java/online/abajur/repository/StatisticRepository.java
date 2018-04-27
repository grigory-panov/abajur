package online.abajur.repository;

import online.abajur.domain.GameStatistic;
import online.abajur.domain.TeamStatistic;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Repository
@Transactional
public class StatisticRepository {

    private static final Map<Integer, TeamStatistic> storage = Collections.synchronizedMap(new LinkedHashMap<>());

    public void saveGameStatistic(GameStatistic statistic){

    }

    public boolean saveTeamStatistic(TeamStatistic statistic){
        if(!statistic.equals(storage.get(statistic.getPosition()))) {
            storage.put(statistic.getPosition(), statistic);
            return true;
        }
        return false;
    }


    public List<TeamStatistic> getTeamStatistic() {
        List<TeamStatistic> h = new ArrayList<>(storage.values());
        Collections.reverse(h);
        return h.subList(0, Math.min(h.size(), 5));
    }
}
