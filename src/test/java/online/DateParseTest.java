package online;

import online.abajur.domain.NextGame;
import org.junit.Assert;
import org.junit.Test;

import java.time.format.DateTimeFormatter;

/**
 * Created by gr on 05.11.18.
 */
public class DateParseTest {

    @Test
    public void testParse() throws Exception {
        String date = "9 ноября";
        NextGame game = new NextGame();
        game.setDate(date);
        Assert.assertEquals("09.11.2018",game.getActualDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        game.setDate("19 ноября");
        Assert.assertEquals("19.11.2018",game.getActualDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

    }
}
