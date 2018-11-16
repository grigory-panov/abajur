package online.abajur.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import online.abajur.AppException;
import online.abajur.DownloadPageException;
import online.abajur.ParsePageException;
import online.abajur.domain.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class MozgvaService {

    private static final Logger logger = LoggerFactory.getLogger(MozgvaService.class);

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private StatisticService statisticService;

    @Cacheable(cacheNames = "landing")
    public LandingPageData getLandingPageData(int teamId) throws AppException {
        LandingPageData data = new LandingPageData();
        try {
            Document document = Jsoup.connect("http://mozgva.com/teams/" + teamId).get();
            logger.info(document.title());
            data.setTitle(document.title());
            data.setName(document.title().replace(" - Это Мозгва, детка!", ""));
            // clear duplicates
            document.selectFirst(".contentWrap.mobile").empty();
            Element members = document.selectFirst(".faceCarousel");
            List<User> team = new ArrayList<>();
            for (Element el : members.children()) {
                logger.info(el.selectFirst("p.name").text() + " " + el.selectFirst(".avatar").attr("style"));
                User user = new User();
                user.setName(el.selectFirst("p.name").text());
                String url = el.selectFirst(".avatar").attr("style").replace("background-image: url('", "").replace("')", "");
                if(url.startsWith("/")){
                    user.setImage("https://mozgva.com" + url);
                }else{
                    user.setImage("https://mozgva.com/" + url);
                }
                team.add(user);
            }
            data.setTeam(team);
            Element currentPosition = document.selectFirst("tr.current");
            TeamStatistic teamStatistic = new TeamStatistic();
            String pos = currentPosition.child(0).text();
            String points = currentPosition.child(3).text();
            teamStatistic.setPosition(StringUtils.isNumeric(pos) ? Integer.parseInt(pos) : null);
            teamStatistic.setGames(Integer.parseInt(currentPosition.child(2).text()));
            teamStatistic.setPoints(StringUtils.isNumeric(points) ? Integer.parseInt(points) : 0);
            teamStatistic.setPercent(currentPosition.child(4).text());
            teamStatistic.setTeamId(teamId);

            data.setBadgeCount(getBageCountByPoints(teamStatistic.getPoints()));
            data.setBadgeClass(getBageClassByPercent(teamStatistic.getPercent()));
            logger.info(teamStatistic.toString());
            statisticService.saveTeamStatistic(teamStatistic);
            data.setPositions(statisticService.getTeamStatistic(teamId));

            Element nextGamesNode = document.selectFirst(".teamGameCarousel");

            List<NextGame> nextGames = new ArrayList<>();
            if(nextGamesNode != null) {
                for (Element el : nextGamesNode.children()) {
                    NextGame game = new NextGame();
                    game.setId(Integer.parseInt(el.selectFirst("a").attr("data-game-id")));
                    game.setLocation(el.selectFirst("a.location").text());
                    String date = el.selectFirst("ul.ad").child(1).text();
                    String time = el.selectFirst("ul.ad").child(2).text();
                    ZonedDateTime localDate = ZonedDateTime.parse(date + " " + LocalDate.now().getYear() + " " + time,
                            DateTimeFormatter.ofPattern("d MMMM yyyy HH:mm", Locale.forLanguageTag("ru")).withZone(ZoneId.of("Europe/Moscow")));
                    game.setDate(localDate);                    
                    nextGames.add(game);
                }
            }
            nextGames.sort(Comparator.comparing(NextGame::getDate));

            if(nextGames.size() > 0){
                Document calendar = Jsoup.connect("http://mozgva.com/calendar?city_id=1").get();
                Elements games = calendar.select(".itemGame-new div.game-content");
                if(games != null){
                    for(int i = 0; i< games.size(); i++) {
                        int gameId = Integer.parseInt(games.get(i).selectFirst("div.bottom-content div.list_wrap a").attr("data-game-id"));
                        for(NextGame nextGame : nextGames){
                            if(nextGame.getId() == gameId){
                                nextGame.setName(games.get(i).selectFirst("div.name").text());
                                Element count = games.get(i).selectFirst("div.bottom-content div.list_wrap div.list_count");
                                if(count != null && StringUtils.isNumeric(count.text())) {
                                    nextGame.setPlayers(Integer.parseInt(count.text()));
                                }
                                statisticService.saveGame(nextGame);
                            }
                        }
                    }
                }

            }
            for (NextGame ng : nextGames) {
                logger.info(ng.toString());
            }
            data.setNextGames(nextGames);

            Element chart = document.selectFirst(".chartMobil");
            if(chart != null) {
                String newScript = changeChartScript(chart.selectFirst("script").html());
                data.setChartScript(newScript);
            }

            Element chartLinks = document.selectFirst(".result_links.hiddenDesc");
            List<PrevGame> prevGames = new ArrayList<>();
            if(chartLinks != null){
                for (Element el : chartLinks.children()) {
                    PrevGame game = new PrevGame();
                    game.setLabel(el.text());
                    game.setId(Integer.parseInt(el.attr("href").replace("/games/", "").replace("/result", "")));
                    prevGames.add(game);
                }
            }
            for (PrevGame ng : prevGames) {
                logger.info(ng.toString());
            }
            data.setPrevGames(prevGames);

            data.setHistoryGames(statisticService.getHistoryGames(teamId));
            data.setLastUpdate(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss ZZZ").format(ZonedDateTime.now().toInstant().atZone(ZoneId.of("Europe/Moscow"))));
            return data;

        }catch (IOException ex){
            throw new DownloadPageException("Cannot load page from mozgva.com", ex);
        }catch (NullPointerException ex){
            throw new ParsePageException("Cannot parse page from mozgva.com, check page structure, source was changed", ex);
        }
    }

    public static String changeChartScript(String script) throws ParsePageException {

        try {
            StringBuilder sb = new StringBuilder(script);
            int start = sb.indexOf("new Chart(ctx,") + "new Chart(ctx,".length();
            int end = sb.indexOf("); }; if (typeof Chart");
            if(start == -1 || end == -1 ) {
                return script;
            }

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            Map o = objectMapper.readValue(sb.substring(start, end), LinkedHashMap.class);
            List<Map> datasets = (List) ((Map) o.get("data")).get("datasets");

            Map team = datasets.stream().filter(t -> t.get("type").equals("line") && t.get("yAxisID").equals("scores")).findFirst().orElse(new LinkedHashMap());
            team.put("borderColor",  "rgba(0, 123, 255, 1)");
            List<Integer> teamPoints = (List<Integer>) team.get("data");
            Map max = datasets.stream().filter(t -> t.get("type").equals("line") && t.get("label").equals("Максимум")).findFirst().orElse(new LinkedHashMap());
            List<Integer> maxPoints = (List<Integer>) max.get("data");

            Map perc = datasets.stream().filter(t -> t.get("type").equals("bar") && t.get("yAxisID").equals("percents")).findFirst().orElse(new LinkedHashMap());
            perc.put("backgroundColor",  "rgba(0, 123, 255, 0.5)");

            List<Integer> diff = new ArrayList<>();
            for (int i = 0; i < maxPoints.size(); i++) {
                diff.add(teamPoints.get(i) - maxPoints.get(i));
            }

            ((Map) ((Map) o.get("options")).get("scales")).remove("xAxes");
            ((Map) o.get("options")).put("onClick", "");

            ((Map) o.get("data")).put("labels", diff);

            return sb.replace(start, end, objectMapper.writeValueAsString(o)).toString();//.replaceAll("\"showGameResults\"", "");
        }catch (IOException ex){
            throw new ParsePageException("cannot parse chart script");
        }
    }

    public static String getBageClassByMozgvaClass(String mozgvaName){
        switch(mozgvaName){
            case "green": return "badge-success";
            case "blue": return "badge-info";
            case "bronze": return "badge-orange";
            case "silver": return "badge-light";
            case "gold": return "badge-warning";
            case "multicolored": return "badge-warning";
            default:
                return "badge-success";
        }
    }

    private String getBageClassByPercent(String percent) {
        try {
            double p = Double.parseDouble(percent.replace("%", ""));
            if(p < 70){
                return "badge-success";
            }
            if(p < 75){
                return "badge-info";
            }
            if(p < 80){
                return "badge-orange";
            }
            if(p < 85){
                return "badge-light";
            }
            if(p <= 100){
                return "badge-warning";
            }
            return "badge-success";
        }catch (Exception ex){
            return "badge-success";
        }

    }

    private int getBageCountByPoints(int points) {
        if(points > 1000){
            return 4;
        }
        if(points > 500){
            return 3;
        }

        if(points > 300){
            return 2;
        }
        if(points >= 200){
            return 1;
        }

        return 0;
    }


    @Cacheable(cacheNames = "games")
    public Collection<GameStatistic> getGameStatistic(int gameId) throws AppException {
        try {
            Document document = Jsoup.connect("http://mozgva.com/games/" + gameId + "/result").get();
            logger.info("title {}", document.title());
            Map<Integer, GameStatistic> results = new LinkedHashMap<>();
            Elements resultPages = document.select(".games_result");
            for(int page = 0; page < resultPages.size(); page++) {
                Element resultsTable = resultPages.get(page).selectFirst("tbody");
                for (Element tr : resultsTable.select("tr")) { // tr
                    GameStatistic teamResult = new GameStatistic();
                    teamResult.setGameId(gameId);
                    Elements td = tr.select("td");

                    Element teamNameDiv = td.get(0).selectFirst("div.name").selectFirst("a");
                    String href = teamNameDiv.attr("href");
                    teamResult.setTeamId(Integer.parseInt(href.substring(href.lastIndexOf('/')).replace("/", "")));
                    teamResult.setTeamName(teamNameDiv.text());
                    teamResult.setTour1(NumberUtils.toInt(td.get(1).text()));
                    teamResult.setTour2(NumberUtils.toInt(td.get(2).text()));
                    teamResult.setTour3(NumberUtils.toInt(td.get(3).text()));
                    teamResult.setTour4(NumberUtils.toInt(td.get(4).text()));
                    teamResult.setTour5(NumberUtils.toInt(td.get(5).text()));
                    teamResult.setTour6(NumberUtils.toInt(td.get(6).text()));
                    teamResult.setTour7(NumberUtils.toInt(td.get(7).text()));
                    teamResult.setTotal(NumberUtils.toInt(td.get(8).text()));
                    teamResult.setPlace(NumberUtils.toInt(td.get(9).text()));
                    teamResult.setUpdateDate(ZonedDateTime.now(ZoneId.of("Europe/Moscow")));
                    Element teamBadge = td.get(0).selectFirst("div.wreath").selectFirst("img");
                    if(teamBadge != null){
                        String[] parts = teamBadge.attr("src").replace("/assets/wreaths/", "").split("-");
                        if(parts[0].endsWith("wreath")){
                            teamResult.setBadgeCount(Integer.parseInt(parts[1]));
                            teamResult.setBadgeClass(MozgvaService.getBageClassByMozgvaClass(parts[2]));
                        }
                    }
                    logger.info(teamResult.toString());
                    results.put(teamResult.getTeamId(), teamResult);
                }
            }
            List<GameStatistic> ret = new ArrayList<>(results.values());
            statisticService.saveGameStatistic(results, gameId);
            return ret;
        }catch (IOException ex){
            throw new DownloadPageException("Cannot load page from mozgva.com", ex);
        }catch (NullPointerException ex){
            throw new ParsePageException("Cannot parse page " + "http://mozgva.com/games/" + gameId + "/result" +" from mozgva.com, check page structure, source was changed", ex);
        }
    }

    @Cacheable(cacheNames = "players")
    public Collection<Player> getGamePlayers(Integer gameId) throws AppException{
        try {
            Document document = Jsoup.connect("http://mozgva.com/teams/list?game_id=" + gameId).get();
            logger.info("title {}", document.title());
            List<Player> ret = new ArrayList<>();
            Element ul = document.selectFirst(".teams_list ul");
            Elements rows = ul.select("li");
            for (int i = 0; i< rows.size(); i++) { // li
                Player player = new Player();
                Element a = rows.get(i).selectFirst("div.team_name a");
                if(a!= null) {
                    player.setTeamId(Integer.parseInt(a.attr("href").replace("/teams/", "")));
                    player.setTeamName(a.text());
                }
                Element img = rows.get(i).selectFirst("div.venki img");
                if(img != null){
                    String[] parts = img.attr("src").replace("/assets/wreaths/", "").split("-");
                    if(parts[0].endsWith("wreath")){
                        player.setBadgeCount(Integer.parseInt(parts[1]));
                        player.setBadgeClass(getBageClassByMozgvaClass(parts[2]));
                    }
                   // wreath-4-gold-f0049625af050149b4b94f8ecacf6a8b17fb595a8cf78123f35c93016a3a62b0.png
                }
                player.setLastUpdateDate(ZonedDateTime.now());
                logger.info(player.toString());
                if(player.getTeamName() != null) {
                    ret.add(player);
                }
            }
            return ret;
        }catch (IOException ex){
            throw new DownloadPageException("Cannot load page from mozgva.com", ex);
        }catch (NullPointerException ex){
            throw new ParsePageException("Cannot parse page " + "http://mozgva.com/teams/list?game_id=" + gameId  +" from mozgva.com, check page structure, source was changed", ex);
        }
    }

    public void clearCache() {
        cacheManager.getCache("landing").clear();
    }

    public void clearGamesCache() {
        cacheManager.getCache("games").clear();
    }

    public void clearPlayersCache() {
        cacheManager.getCache("players").clear();
    }


}
