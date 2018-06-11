package online;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import online.abajur.domain.NextGame;
import online.abajur.domain.PrevGame;
import online.abajur.domain.TeamStatistic;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HomeTest {

    @Test
    public void testParseHome() throws Exception{

        Document document = Jsoup.parse(HomeTest.class.getResourceAsStream("/home.html"), "UTF-8", "http://mozgva.com/");
        System.out.println(document.title());
        document.selectFirst(".contentWrap.mobile").empty();
        Element members = document.selectFirst(".faceCarousel");
        for(Element el : members.children()){
            System.out.println(el.selectFirst("p.name").text() + " " +el.selectFirst(".avatar").attr("style"));
        }
        Element currentPosition = document.selectFirst("tr.current");
        TeamStatistic teamStatistic = new TeamStatistic();
        teamStatistic.setPosition(Integer.parseInt(currentPosition.child(0).text()));
        teamStatistic.setGames(Integer.parseInt(currentPosition.child(2).text()));
        teamStatistic.setPoints(Integer.parseInt(currentPosition.child(3).text()));
        teamStatistic.setPercent(currentPosition.child(4).text());

        System.out.println(teamStatistic);

        Element nextGamesNode = document.selectFirst(".teamGameCarousel");

        List<NextGame> nextGames = new ArrayList<>();
        for(Element el : nextGamesNode.children()){
            NextGame game = new NextGame();
            game.setId(Integer.parseInt(el.selectFirst("a").attr("data-game-id")));
            game.setLocation(el.selectFirst("a.location").text());
            game.setDate(el.selectFirst("ul.ad").child(1).text());
            game.setTime(el.selectFirst("ul.ad").child(2).text());
            nextGames.add(game);
        }
        nextGames.sort(Comparator.comparing(NextGame::getActualDate));
        for(NextGame ng : nextGames){
            System.out.println(ng);
        }

        Element chart = document.selectFirst(".chartMobil");
        System.out.println(chart.selectFirst("script").html());

        Element chartLinks = document.selectFirst(".result_links.hiddenDesc");
        List<PrevGame> prevGames = new ArrayList<>();
        for(Element el : chartLinks.children()){
            PrevGame game = new PrevGame();
            game.setLabel(el.text());
            game.setId(Integer.parseInt(el.attr("href").replace("/games/", "").replace("/result", "")));
            prevGames.add(game);
        }
        for(PrevGame ng : prevGames){
            System.out.println(ng);
        }
    }

    @Test
    public void testParse() {
        String date = "21 апреля " + LocalDate.now().getYear();
        System.out.println(LocalDate.parse(date, DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.forLanguageTag("ru"))));
    }

    @Test
    public void testChart() throws IOException {
        String script = "\n" +
                "//<![CDATA[\n" +
                "(function() { var initChart = function() { var ctx = document.getElementById(\"chart-1\"); var chart = new Chart(ctx, { type: \"bar\", data: {\"labels\":[\"111_2\",\"112_2\",\"113_2\",\"114_1\",\"114_3f\",\"115_1\",\"116_2\",\"117_3 f\",\"118_2\",\"119_3f\"],\"datasets\":[{\"type\":\"line\",\"label\":\"Абажур\",\"yAxisID\":\"scores\",\"data\":[24,26,26,22,35,35,36,39,34,31],\"borderColor\":\"rgba(255, 0, 1, 1)\",\"backgroundColor\":\"transparent\"},{\"type\":\"line\",\"label\":\"Максимум\",\"data\":[33,34,40,45,43,44,41,39,43,40],\"borderColor\":\"rgba(255, 255, 255, 0.7)\",\"backgroundColor\":\"transparent\"},{\"label\":\"Абажур (%)\",\"yAxisID\":\"percents\",\"type\":\"bar\",\"data\":[72.73,76.47,65.0,48.89,81.4,79.55,87.8,100.0,79.07,77.5],\"backgroundColor\":[\"rgba(255, 0, 1, 0.2)\",\"rgba(255, 0, 1, 0.2)\",\"rgba(255, 0, 1, 0.2)\",\"rgba(255, 0, 1, 0.2)\",\"rgba(255, 0, 1, 0.2)\",\"rgba(255, 0, 1, 0.2)\",\"rgba(255, 0, 1, 0.2)\",\"rgba(255, 0, 1, 0.2)\",\"rgba(255, 0, 1, 0.2)\",\"rgba(255, 0, 1, 0.2)\",\"rgba(255, 0, 1, 0.5)\",\"rgba(255, 0, 1, 0.5)\",\"rgba(255, 0, 1, 0.5)\",\"rgba(255, 0, 1, 0.5)\",\"rgba(255, 0, 1, 0.5)\",\"rgba(255, 0, 1, 0.5)\",\"rgba(255, 0, 1, 0.5)\",\"rgba(255, 0, 1, 0.5)\",\"rgba(255, 0, 1, 0.5)\",\"rgba(255, 0, 1, 0.5)\"]}]}, options: {\"responsive\":true,\"maintainAspectRatio\":false,\"scales\":{\"yAxes\":[{\"id\":\"scores\",\"ticks\":{\"suggestedMin\":0,\"suggestedMax\":50,\"fontColor\":\"rgba(255, 255, 255, 0.7)\"}},{\"id\":\"percents\",\"position\":\"right\",\"ticks\":{\"suggestedMin\":0,\"suggestedMax\":100,\"fontColor\":\"rgba(255, 255, 255, 0.7)\"}}],\"xAxes\":[{\"ticks\":{\"fontColor\":\"rgba(255, 255, 255, 0)\"}}]}} }); }; if (typeof Chart !== \"undefined\" && Chart !== null) { initChart(); } else { /* W3C standard */ if (window.addEventListener) { window.addEventListener(\"load\", initChart, false); } /* IE */ else if (window.attachEvent) { window.attachEvent(\"onload\", initChart); } } })();\n" +
                "//]]>";

        StringBuilder sb = new StringBuilder(script);
        int start = sb.indexOf("new Chart(ctx,") + "new Chart(ctx,".length();
        int end = sb.indexOf("); }; if (typeof Chart");

        System.out.println(sb.substring(start, end));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        HashMap o = objectMapper.readValue(sb.substring(start, end), HashMap.class);
        List<Map> datasets = (List) ((Map)o.get("data")).get("datasets");
        System.out.println(o);
        Map team = datasets.stream().filter(t-> t.get("type").equals("line") &&  t.get("yAxisID").equals("scores")).findFirst().orElse(new LinkedHashMap());
        List<Integer> teamPoints = (List<Integer>) team.get("data");
        Map max = datasets.stream().filter(t-> t.get("type").equals("line") &&  t.get("label").equals("Максимум")).findFirst().orElse(new LinkedHashMap());
        List<Integer> maxPoints = (List<Integer>) max.get("data");

        List<Integer> diff = new ArrayList<>();
        for(int i = 0 ; i< maxPoints.size(); i++){
            diff.add(teamPoints.get(i) -  maxPoints.get(i));
        }

        ((Map)((Map)o.get("options")).get("scales")).remove("xAxes");
        ((Map)o.get("data")).put("labels", diff);

        System.out.println(o);

    }

    @Test
    public void testCalendar() throws Exception{
        Document calendar = Jsoup.parse(HomeTest.class.getResourceAsStream("/calendar.html"), "UTF-8", "http://mozgva.com/");
        Elements games = calendar.select(".itemGame-new div.game-content");
        if(games != null){
            for(int i = 0; i< games.size(); i++) {
                System.out.println("name = " + games.get(i).selectFirst("div.name").text() +
                        ", id=" + games.get(i).selectFirst("div.bottom div.list_wrap a").attr("data-game-id") +
                        ", players = " + games.get(i).selectFirst("div.bottom div.list_wrap div.list_count").text()
                );
            }
        }

    }
}
