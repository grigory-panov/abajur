package online.abajur.domain;

import java.util.Objects;

public class TeamStatistic {
    private int games;
    private Integer position;
    private int points;
    private String percent;

    public int getGames() {
        return games;
    }

    public void setGames(int games) {
        this.games = games;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getPercent() {
        return percent;
    }

    public void setPercent(String percent) {
        this.percent = percent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamStatistic that = (TeamStatistic) o;
        return games == that.games &&
                points == that.points &&
                Objects.equals(position, that.position) &&
                Objects.equals(percent, that.percent);
    }

    @Override
    public int hashCode() {

        return Objects.hash(games, position, points, percent);
    }

    @Override
    public String toString() {
        return "TeamStatistic{" +
                "games=" + games +
                ", position=" + position +
                ", points=" + points +
                ", percent='" + percent + '\'' +
                '}';
    }
}
