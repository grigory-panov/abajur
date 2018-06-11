package online;

import online.abajur.domain.Player;
import online.abajur.service.MozgvaService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Assert;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;


public class MembersTest {
    @Test
    public void testParseHome() throws Exception{

        Document document = Jsoup.parse(HomeTest.class.getResourceAsStream("/members.html"), "UTF-8", "http://mozgva.com/");
        System.out.println(document.title());
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
                    player.setBadgeClass(MozgvaService.getBageClassByMozgvaClass(parts[2]));
                }
                // wreath-4-gold-f0049625af050149b4b94f8ecacf6a8b17fb595a8cf78123f35c93016a3a62b0.png
            }
            player.setLastUpdateDate(ZonedDateTime.now());
            System.out.println(player);
            if(player.getTeamName() != null) {
                ret.add(player);
            }
        }

        Assert.assertEquals(35, ret.size());

    }
}
