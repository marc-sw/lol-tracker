package com.fumano.crawler.model;

import java.util.Objects;

public class Account {

    private final String puuid;
    private final String gameName;
    private final String tagLine;

    public Account(String puuid, String gameName, String tagLine) {
        this.puuid = puuid;
        this.gameName = gameName;
        this.tagLine = tagLine;
    }

    public String getPuuid() {
        return puuid;
    }

    public String getGameName() {
        return gameName;
    }

    public String getTagLine() {
        return tagLine;
    }

    @Override
    public String toString() {
        return "Account{" +
                "puuid='" + puuid + '\'' +
                ", gameName='" + gameName + '\'' +
                ", tagLine='" + tagLine + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(puuid, account.puuid) && Objects.equals(gameName, account.gameName) && Objects.equals(tagLine, account.tagLine);
    }

    @Override
    public int hashCode() {
        return Objects.hash(puuid, gameName, tagLine);
    }
}
