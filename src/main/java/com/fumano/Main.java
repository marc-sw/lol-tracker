package com.fumano;

import com.fumano.crawler.Config;
import com.fumano.crawler.Crawler;
import com.fumano.crawler.exception.CrawlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(Main.class);

        if (args.length == 0) {
            logger.error("amount of matches to crawl not set");
            return;
        }

        int matches;
        try {
            matches = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            logger.error("first argument needs to be an integer");
            return;
        }

        try (var connection = DriverManager.getConnection("jdbc:sqlite:" + Config.DATA_SOURCE_FILE)) {
            var crawler = Crawler.create(Config.Load(), connection);
            crawler.crawlGames(matches);
            crawler.fillMissingData();
        } catch (SQLException e) {
            logger.error("failed to start sql connection: {}", e.getMessage());
        } catch (CrawlerException e) {
            logger.error(e.getMessage());
        } catch (Exception e) {
            logger.error("unexpected error: {}", e.getMessage());
        }
    }
}