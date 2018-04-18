package online.abajur.domain;

import java.util.List;

public class LandingPageData {

    private List<NextGame> nextGames;
    private List<PrevGame> prevGames;
    private Position position;
    private List<User> team;
    private String title;
    private String chartScript;
    private String lastUpdate;
    private String name;

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

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
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
}
