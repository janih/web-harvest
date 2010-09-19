import org.webharvest.definition.ScraperConfiguration;
import org.webharvest.runtime.Scraper;

import java.io.IOException;

public abstract class Test {

    // todo: Use unit tests or acceptance tests instead of this!
    public static void main(String[] args) throws IOException {

//        ScraperConfiguration config = new ScraperConfiguration("c:/temp/scrapertest/configs/test2.xml");
        ScraperConfiguration config = new ScraperConfiguration("c:/temp/scrapertest/dddd.xml");
//        ScraperConfiguration config = new ScraperConfiguration( new URL("http://localhost/scripts/test/sample8.xml") );
        Scraper scraper = new Scraper(config, "c:/temp/scrapertest/");

        scraper.setDebug(true);

        long startTime = System.currentTimeMillis();
        scraper.execute();
        System.out.println("time elapsed: " + (System.currentTimeMillis() - startTime));
    }

}