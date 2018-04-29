package online.abajur.repository;

import online.abajur.domain.GameStatistic;
import online.abajur.domain.TeamStatistic;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@Transactional
public class StatisticRepository {

    private final JdbcTemplate template;

    public StatisticRepository(DataSource ds) {
        template = new JdbcTemplate(ds);
    }

    private static TeamStatistic mapRow(ResultSet resultSet, int i) throws SQLException {
        TeamStatistic stat = new TeamStatistic();
        stat.setGames(resultSet.getInt("games"));
        stat.setPoints(resultSet.getInt("points"));
        stat.setPosition(resultSet.getInt("pos"));
        stat.setPercent(resultSet.getString("percent"));
        return stat;
    }

    public void saveGameStatistic(GameStatistic statistic) {

    }

    public boolean saveTeamStatistic(TeamStatistic statistic) {
        try {
            TeamStatistic oldStat = template.queryForObject("select * from team_statistic where games = ?", StatisticRepository::mapRow, statistic.getGames());
            if (!statistic.equals(oldStat)) {
                template.update("update team_statistic set points=?, pos=?, percent=? where games = ?",
                        statistic.getPoints(), statistic.getPosition(), statistic.getPercent(), statistic.getGames());
                return true;
            }
        } catch (EmptyResultDataAccessException ex) {
            template.update("insert into team_statistic(games, points, pos, percent) values (?, ?, ?, ?)",
                    statistic.getGames(), statistic.getPoints(), statistic.getPosition(), statistic.getPercent());
            return true;
        }
        return false;
    }

    public List<TeamStatistic> getTeamStatistic() {
        return template.query("select * from team_statistic order by games desc LIMIT 5", StatisticRepository::mapRow);
    }
}
