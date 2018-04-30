package online.abajur.service;

import online.abajur.AppException;
import online.abajur.DownloadPageException;
import online.abajur.ParsePageException;
import online.abajur.domain.*;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class MozgvaService {

    private static final Logger logger = LoggerFactory.getLogger(MozgvaService.class);

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private StatisticService statisticService;

    @Cacheable(cacheNames = "landing")
    public LandingPageData getLandingPageData(int teamId) throws AppException{
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
                user.setImage(el.selectFirst(".avatar").attr("style").replace("background-image: url('", "").replace("')", ""));
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

            logger.info(teamStatistic.toString());
            statisticService.saveTeamStatistic(teamStatistic);
            data.setPositions(statisticService.getTeamStatistic());

            Element nextGamesNode = document.selectFirst(".teamGameCarousel");

            List<NextGame> nextGames = new ArrayList<>();
            if(nextGamesNode != null) {
                for (Element el : nextGamesNode.children()) {
                    NextGame game = new NextGame();
                    game.setId(Integer.parseInt(el.selectFirst("a").attr("data-game-id")));
                    game.setLocation(el.selectFirst("a.location").text());
                    game.setDate(el.selectFirst("ul.ad").child(1).text());
                    game.setTime(el.selectFirst("ul.ad").child(2).text());
                    nextGames.add(game);
                }
            }
            nextGames.sort(Comparator.comparingInt(NextGame::getId));
            for (NextGame ng : nextGames) {
                logger.info(ng.toString());
            }
            data.setNextGames(nextGames);

            Element chart = document.selectFirst(".chartMobil");
            if(chart != null) {
                data.setChartScript(chart.selectFirst("script").html());
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
            data.setLastUpdate(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss ZZZ").format(ZonedDateTime.now().toInstant().atZone(ZoneId.of("Europe/Moscow"))));
            return data;

        }catch (IOException ex){
            throw new DownloadPageException("Cannot load page from mozgva.com", ex);
        }catch (NullPointerException ex){
            throw new ParsePageException("Cannot parse page from mozgva.com, check page structure, source was changed", ex);
        }
    }


    public void clearCache() {
        cacheManager.getCache("landing").clear();
    }
}
