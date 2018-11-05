package online.abajur.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class NextGame {
    private int id;
    private String location;
    private LocalDateTime date;
    private String name;
    private int players;

    public int getPlayers() {
        return players;
    }

    public void setPlayers(int players) {
        this.players = players;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getFormattedDate() {
        return date.format(DateTimeFormatter.ofPattern("EEE, dd MMMM, HH:mm ", Locale.forLanguageTag("ru")));
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "NextGame{" +
                "id=" + id +
                ", location='" + location + '\'' +
                ", date='" + getFormattedDate() + '\'' +
                ", name='" + name + '\'' +
                ", players=" + players +
                '}';
    }
}
