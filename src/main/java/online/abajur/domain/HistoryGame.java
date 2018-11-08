package online.abajur.domain;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class HistoryGame {
    private int id;
    private String location;
    private ZonedDateTime date;
    private String name;
    private int players;
    private int place;
    private int total;

    public int getPlace() {
        return place;
    }

    public void setPlace(int place) {
        this.place = place;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

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

    public ZonedDateTime getDate() {
        return date;
    }

    public void setDate(ZonedDateTime date) {
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
