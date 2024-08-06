package com.fumano.crawler;

import com.fumano.crawler.exception.*;
import com.fumano.crawler.mapper.Mapper;
import com.fumano.crawler.store.Count;
import com.fumano.crawler.store.Store;
import no.stelar7.api.r4j.basic.APICredentials;
import no.stelar7.api.r4j.basic.cache.impl.EmptyCacheProvider;
import no.stelar7.api.r4j.basic.calling.DataCall;
import no.stelar7.api.r4j.basic.constants.api.regions.LeagueShard;
import no.stelar7.api.r4j.basic.constants.api.regions.RegionShard;
import no.stelar7.api.r4j.impl.R4J;
import no.stelar7.api.r4j.impl.lol.builders.matchv5.match.MatchListBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;


public class Crawler {

    private final Logger logger;
    private final RegionShard regionShard;
    private final LeagueShard leagueShard;
    private final R4J api;
    private final Store store;
    private int accountRow;
    private int matchIdRow;

    public Crawler(RegionShard regionShard, LeagueShard leagueShard, R4J api, String seedGameName, String seedTagLine, Store store)
            throws CrawlerException {
        this.regionShard = regionShard;
        this.leagueShard = leagueShard;
        this.api = api;
        this.store = store;
        logger = LoggerFactory.getLogger(Crawler.class);

        var count = store.getAccountsCount().orElseThrow(() ->
                new CrawlerException("failed to retrieve the amount of accounts from database"));
        if (count == 0) {
            var riotAccount = RetryHandler.get( () -> api.getAccountAPI().getAccountByTag(regionShard, seedGameName, seedTagLine));
            if (riotAccount == null) {
                throw new CrawlerException("failed to fetch the seed account specified int the config file");
            }
            store.addOrIgnoreAccount(Mapper.RiotAccount.map(riotAccount));
        }
        accountRow = store.getCount(Count.AccountRow).orElseThrow(() ->
                new CrawlerException("failed to retrieve accountRow from database"));
        matchIdRow = store.getCount(Count.MatchIdRow).orElseThrow(() ->
                new CrawlerException("failed to retrieve matchIdRow from database"));
    }

    private void crawlNextMatch() throws CrawlerException {
        var matchId = store.getMatchIdAtRow(matchIdRow).orElseThrow(() ->
                new CrawlerException("failed to retrieve matchId at row " + matchIdRow));
        var match = RetryHandler.get(() -> api.getLoLAPI().getMatchAPI().getMatch(regionShard, matchId));
        if (match == null) {
            logger.error("match with id '%s' not found, deleting match".formatted(matchId));
            store.deleteMatchId(matchId);
            crawlNextMatch();
            return;
        }
        for (var participant :match.getParticipants()) {
            if (participant.getPuuid().equals("BOT")) {
                continue;
            }
            store.addOrIgnoreAccount(Mapper.Participant.map(participant));
        }
        matchIdRow++;
        store.setCount(Count.MatchIdRow, matchIdRow);
    }

    private void crawlNextAccount() throws CrawlerException {
        var account = store.getAccountAtRow(accountRow).orElseThrow(() ->
                new CrawlerException("failed to retrieve account at row " + accountRow));
        var matchIds = RetryHandler.get(() -> new MatchListBuilder()
                .withCount(100)
                .withBeginIndex(0)
                .withPlatform(leagueShard)
                .withPuuid(account.getPuuid())
                .get());
        store.addOrIgnoreMatchIds(matchIds);
        accountRow++;
        store.setCount(Count.AccountRow, accountRow);
    }

    private void crawlNext() throws CrawlerException {
        try {
            crawlNextMatch();
            return;
        } catch (CrawlerException ignored) {
        }
        try {
            crawlNextAccount();
        } catch (CrawlerException e) {
            throw new CrawlerException("crawled all accounts and matches");
        }
        crawlNext();
    }

    public void crawlGames(int total) {
        System.out.println("crawling %d games".formatted(total));
        Progress progress = new Progress(total);
        int chainedErrors = 0;
        for (int i = 0; i < total; i++) {
            chainedErrors++;
            try {
                crawlNext();
                progress.increase();
                chainedErrors = 0;
            } catch (CrawlerException e) {
                logger.error(e.getMessage());
            }
            if (chainedErrors >= 5) {
                logger.error("5 errors in a row, to prevent damage the program ends");
                System.exit(1);
            }
        }
        System.out.println();
    }

    public void fillMissingData() throws CrawlerException {
        var puuids = store.getIncompleteAccountPuuids();
        System.out.println("filling %d accounts".formatted(puuids.size()));
        var progress = new Progress(puuids.size());
        for (var puuid: puuids) {
            var riotAccount = RetryHandler.get(() -> api.getAccountAPI().getAccountByPUUID(regionShard, puuid));
            progress.increase();
            if (riotAccount == null) {
                logger.error("account with puuid '%s' not found, deleting account");
                store.deleteAccount(puuid);
                continue;
            }
            store.updateAccount(Mapper.RiotAccount.map(riotAccount));
        }
        System.out.println();
    }

    public static Crawler create(Config config, Connection connection) throws CrawlerException {
        R4J api = new R4J(new APICredentials(config.riotApiKey()));
        DataCall.setCacheProvider(EmptyCacheProvider.INSTANCE);
        String region = config.riotApiRegion();
        LeagueShard leagueShard = LeagueShard.fromString(region).orElseThrow( () -> new RuntimeException(
                new InvalidConfigFieldException(Config.RIOTAPI_REGION_FIELD, region)));
        RegionShard regionShard = leagueShard.toRegionShard();
        return new Crawler(regionShard, leagueShard, api,
                config.crawlerSeedGameName(), config.crawlerSeedTagLine(), new Store(connection));

    }
}

