package online.abajur.domain;

import java.time.ZonedDateTime;

public class Player {
    private int teamId;
    private String teamName;
    private ZonedDateTime lastUpdateDate;
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

    public ZonedDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(ZonedDateTime lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    @Override
    public String toString() {
        return "Player{" +
                "teamId=" + teamId +
                ", teamName='" + teamName + '\'' +
                ", lastUpdateDate=" + lastUpdateDate +
                ", badgeClass='" + badgeClass + '\'' +
                ", badgeCount=" + badgeCount +
                '}';
    }
}
