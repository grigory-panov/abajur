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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Repository
@Transactional
public class StatisticRepository {

    private final JdbcTemplate template;

    public StatisticRepository(DataSource ds) {
        template = new JdbcTemplate(ds);
    }

    private static TeamStatistic mapTeam(ResultSet resultSet, int i) throws SQLException {
        TeamStatistic stat = new TeamStatistic();
        stat.setGames(resultSet.getInt("games"));
        stat.setPoints(resultSet.getInt("points"));
        stat.setPosition(resultSet.getInt("pos"));
        stat.setPercent(resultSet.getString("percent"));
        return stat;
    }

    private static GameStatistic mapGame(ResultSet resultSet, int i) throws SQLException {
        GameStatistic stat = new GameStatistic();
        stat.setPlace(resultSet.getInt("place"));
        stat.setTeamId(resultSet.getInt("team_id"));
        stat.setTeamName(resultSet.getString("team_name"));
        stat.setGameId(resultSet.getInt("game_id"));
        stat.setTour1(resultSet.getInt("tour1"));
        stat.setTour2(resultSet.getInt("tour2"));
        stat.setTour3(resultSet.getInt("tour3"));
        stat.setTour4(resultSet.getInt("tour4"));
        stat.setTour5(resultSet.getInt("tour5"));
        stat.setTour6(resultSet.getInt("tour6"));
        stat.setTour7(resultSet.getInt("tour7"));
        stat.setTotal(resultSet.getInt("total"));
        stat.setUpdateDate(resultSet.getTimestamp("update_date").toInstant().atZone(ZoneId.of("Europe/Moscow")));
        return stat;
    }

    public boolean saveTeamStatistic(TeamStatistic statistic) {
        try {
            TeamStatistic oldStat = template.queryForObject("select * from team_statistic where team_id = ? and games = ?", StatisticRepository::mapTeam, statistic.getTeamId(), statistic.getGames());
            if (!statistic.equals(oldStat)) {
                template.update("update team_statistic set points=?, pos=?, percent=? where team_id = ? and games = ?",
                        statistic.getPoints(), statistic.getPosition(), statistic.getPercent(), statistic.getTeamId(), statistic.getGames());
                return true;
            }
        } catch (EmptyResultDataAccessException ex) {
            template.update("insert into team_statistic(team_id, games, points, pos, percent) values (?, ?, ?, ?, ?)",
                    statistic.getTeamId(), statistic.getGames(), statistic.getPoints(), statistic.getPosition(), statistic.getPercent());
            return true;
        }
        return false;
    }

    public List<TeamStatistic> getTeamStatistic(int teamId) {
        return template.query("select * from team_statistic where team_id = ? order by games desc LIMIT 5", StatisticRepository::mapTeam, teamId);
    }

    public List<GameStatistic> getGameStatistic(int gameId) {
        return template.query("select * from game_statistic where game_id = ? order by place", StatisticRepository::mapGame, gameId);
    }


    public void updateGameStatistic(GameStatistic stat) {
        template.update("update game_statistic set PLACE=?, tour1=?, tour2=?, tour3=?, tour4=?, tour5=?, tour6=?, tour7=?, total=?, update_date=? where game_id = ? and team_id = ?",
                stat.getPlace(),
                stat.getTour1(),
                stat.getTour2(),
                stat.getTour3(),
                stat.getTour4(),
                stat.getTour5(),
                stat.getTour6(),
                stat.getTour7(),
                stat.getTotal(),
                stat.getUpdateDate().toInstant().atZone(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_INSTANT),
                stat.getGameId(),
                stat.getTeamId()

        );
    }

    public void insertGameStatistic(GameStatistic stat) {
        template.update("insert into game_statistic (game_id, team_id, team_name, PLACE, tour1, tour2, tour3, tour4, tour5, tour6, tour7, total, update_date) values (?,?,?,?, ?,?,?,?,?,?,?, ?,?)",
                stat.getGameId(),
                stat.getTeamId(),
                stat.getTeamName(),
                stat.getPlace(),
                stat.getTour1(),
                stat.getTour2(),
                stat.getTour3(),
                stat.getTour4(),
                stat.getTour5(),
                stat.getTour6(),
                stat.getTour7(),
                stat.getTotal(),
                stat.getUpdateDate().toInstant().atZone(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_INSTANT)
        );
    }
}
