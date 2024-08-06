package com.fumano.crawler.mapper;

import com.fumano.crawler.model.Account;
import no.stelar7.api.r4j.pojo.lol.match.v5.MatchParticipant;
import no.stelar7.api.r4j.pojo.shared.RiotAccount;

public class Mapper {
    public static final Mapping<RiotAccount, Account> RiotAccount = (a) ->
            new Account(a.getPUUID(), a.getName(), a.getTag());

    public static final Mapping<MatchParticipant, Account> Participant = (p) ->
            new Account(p.getPuuid(), p.getRiotIdName(), p.getRiotIdTagline());
}
