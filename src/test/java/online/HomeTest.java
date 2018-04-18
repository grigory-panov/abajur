package online;
import online.abajur.domain.NextGame;
import online.abajur.domain.Position;
import online.abajur.domain.PrevGame;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

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
        Position position = new Position();
        position.setPosition(Integer.parseInt(currentPosition.child(0).text()));
        position.setGames(Integer.parseInt(currentPosition.child(2).text()));
        position.setPoints(Integer.parseInt(currentPosition.child(3).text()));
        position.setPercent(currentPosition.child(4).text());

        System.out.println(position);

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
        nextGames.sort(Comparator.comparingInt(NextGame::getId));
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
}
