package com.fumano.crawler.store;

public enum Count {
    AccountRow("account_row"), MatchIdRow("match_id_row");

    private final String name;

    Count(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
