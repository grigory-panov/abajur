package online.abajur.domain;

public class Position {
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
    public String toString() {
        return "Position{" +
                "games=" + games +
                ", position=" + position +
                ", points=" + points +
                ", percent='" + percent + '\'' +
                '}';
    }
}
