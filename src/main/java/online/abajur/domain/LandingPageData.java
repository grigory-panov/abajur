package online.abajur.domain;

import java.util.List;

public class LandingPageData {

    private List<NextGame> nextGames;
    private List<PrevGame> prevGames;
    private List<TeamStatistic> positions;
    private List<User> team;
    private List<HistoryGame> historyGames;
    private String title;
    private String chartScript;
    private String lastUpdate;
    private String name;
    private String badgeClass;
    private int badgeCount;

    public String getBadgeClass() {
        return badgeClass;
    }

    public void setBadgeClass(String badgeClass) {
        this.badgeClass = badgeClass;
    }

    public int getBadgeCount() {
        return badgeCount;
    }

    public void setBadgeCount(int badgeCount) {
        this.badgeCount = badgeCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public List<NextGame> getNextGames() {
        return nextGames;
    }

    public void setNextGames(List<NextGame> nextGames) {
        this.nextGames = nextGames;
    }

    public List<PrevGame> getPrevGames() {
        return prevGames;
    }

    public void setPrevGames(List<PrevGame> prevGames) {
        this.prevGames = prevGames;
    }

    public List<TeamStatistic> getPositions() {
        return positions;
    }

    public void setPositions(List<TeamStatistic> positions) {
        this.positions = positions;
    }

    public List<User> getTeam() {
        return team;
    }

    public void setTeam(List<User> team) {
        this.team = team;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getChartScript() {
        return chartScript;
    }

    public void setChartScript(String chartScript) {
        this.chartScript = chartScript;
    }

    public List<HistoryGame> getHistoryGames() {
        return historyGames;
    }

    public void setHistoryGames(List<HistoryGame> historyGames) {
        this.historyGames = historyGames;
    }
}
