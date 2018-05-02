package online.abajur.domain;

import java.time.ZonedDateTime;
import java.util.Objects;

public class GameStatistic {
    private int gameId;
    private int teamId;
    private String teamName;
    private int tour1;
    private int tour2;
    private int tour3;
    private int tour4;
    private int tour5;
    private int tour6;
    private int tour7;

    private int total;
    private int place;

    private ZonedDateTime updateDate;


    public ZonedDateTime getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(ZonedDateTime updateDate) {
        this.updateDate = updateDate;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public int getTour1() {
        return tour1;
    }

    public void setTour1(int tour1) {
        this.tour1 = tour1;
    }

    public int getTour2() {
        return tour2;
    }

    public void setTour2(int tour2) {
        this.tour2 = tour2;
    }

    public int getTour3() {
        return tour3;
    }

    public void setTour3(int tour3) {
        this.tour3 = tour3;
    }

    public int getTour4() {
        return tour4;
    }

    public void setTour4(int tour4) {
        this.tour4 = tour4;
    }

    public int getTour5() {
        return tour5;
    }

    public void setTour5(int tour5) {
        this.tour5 = tour5;
    }

    public int getTour6() {
        return tour6;
    }

    public void setTour6(int tour6) {
        this.tour6 = tour6;
    }

    public int getTour7() {
        return tour7;
    }

    public void setTour7(int tour7) {
        this.tour7 = tour7;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPlace() {
        return place;
    }

    public void setPlace(int place) {
        this.place = place;
    }

    @Override
    public String toString() {
        return "GameStatistic{" +
                "gameId=" + gameId +
                ", teamId=" + teamId +
                ", teamName='" + teamName + '\'' +
                ", tour1=" + tour1 +
                ", tour2=" + tour2 +
                ", tour3=" + tour3 +
                ", tour4=" + tour4 +
                ", tour5=" + tour5 +
                ", tour6=" + tour6 +
                ", tour7=" + tour7 +
                ", total=" + total +
                ", place=" + place +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameStatistic that = (GameStatistic) o;
        return gameId == that.gameId &&
                teamId == that.teamId &&
                tour1 == that.tour1 &&
                tour2 == that.tour2 &&
                tour3 == that.tour3 &&
                tour4 == that.tour4 &&
                tour5 == that.tour5 &&
                tour6 == that.tour6 &&
                tour7 == that.tour7 &&
                total == that.total &&
                place == that.place &&
                Objects.equals(teamName, that.teamName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(gameId, teamId, teamName, tour1, tour2, tour3, tour4, tour5, tour6, tour7, total, place);
    }
}
