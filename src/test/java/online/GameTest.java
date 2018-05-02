package online;

import online.abajur.domain.GameStatistic;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class GameTest {

    @Test
    public void testParseGameResults() throws Exception{
        int gameId = 1329;
        Document document = Jsoup.parse(GameTest.class.getResourceAsStream("/game.html"), "UTF-8", "http://mozgva.com/");
        System.out.println(document.title());
        Map<Integer, GameStatistic> results = new HashMap<>();
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
                teamResult.setTour1(Integer.parseInt(td.get(1).text()));
                teamResult.setTour2(Integer.parseInt(td.get(2).text()));
                teamResult.setTour3(Integer.parseInt(td.get(3).text()));
                teamResult.setTour4(Integer.parseInt(td.get(4).text()));
                teamResult.setTour5(Integer.parseInt(td.get(5).text()));
                teamResult.setTour6(Integer.parseInt(td.get(6).text()));
                teamResult.setTour7(Integer.parseInt(td.get(7).text()));
                teamResult.setTotal(Integer.parseInt(td.get(8).text()));
                teamResult.setPlace(Integer.parseInt(td.get(9).text()));

                System.out.println(teamResult);
                results.put(teamResult.getTeamId(), teamResult);
                Assert.assertNotNull(teamResult.getTeamName());
                Assert.assertEquals(teamResult.getTotal(), teamResult.getTour1() + teamResult.getTour2() + teamResult.getTour3() + teamResult.getTour4() + teamResult.getTour5() + teamResult.getTour6() + teamResult.getTour7());
            }
        }
        Assert.assertEquals(31, results.size());
    }
}
