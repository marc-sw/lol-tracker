package com.fumano.crawler.store;

import com.fumano.crawler.model.Account;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Store {

    private final Connection connection;

    public Store(Connection connection) {
        this.connection = connection;
        prepareTables();
    }

    private void prepareTables() {
        try (var statement = connection.createStatement()) {
            connection.setAutoCommit(false);
            statement.addBatch("""
CREATE TABLE IF NOT EXISTS accounts (
    puuid TEXT PRIMARY KEY NOT NULL,
    game_name TEXT NOT NULL,
    tag_line TEXT NOT NULL
);
"""
            );
            statement.addBatch("""
CREATE TABLE IF NOT EXISTS match_ids (
    match_id TEXT PRIMARY KEY NOT NULL
);
""");
            statement.addBatch("""
CREATE TABLE IF NOT EXISTS counts (
    name TEXT PRIMARY KEY NOT NULL,
    value INTEGER NOT NULL
);
""");
            statement.addBatch("""
INSERT OR IGNORE INTO counts (name, value) VALUES
    ('account_row', 0),
    ('match_id_row', 0)
""");
            statement.executeBatch();
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addOrIgnoreAccount(Account account) {
        try (var statement = connection.prepareStatement(
                "INSERT OR IGNORE INTO accounts (puuid, game_name, tag_line) VALUES (?,?,?)")) {
            statement.setString(1, account.getPuuid());
            statement.setString(2, account.getGameName());
            statement.setString(3, account.getTagLine());
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Account> getAccount(String puuid) {
        try (var statement = connection.prepareStatement(
                "SELECT puuid, game_name, tag_line FROM accounts WHERE puuid = ?")) {
            statement.setString(1, puuid);
            try (var resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Account(
                        resultSet.getString(1),
                        resultSet.getString(2),
                        resultSet.getString(3)
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Account> getAccountAtRow(int row) {
        try (var statement = connection.prepareStatement(
                "SELECT puuid, game_name, tag_line FROM accounts LIMIT 1 OFFSET ?")) {
            statement.setInt(1, row);
            try (var resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Account(
                        resultSet.getString(1),
                        resultSet.getString(2),
                        resultSet.getString(3)
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Integer> getAccountsCount() {
        try (var statement = connection.createStatement();
             var resultSet = statement.executeQuery("SELECT COUNT() FROM accounts")) {
            if (!resultSet.next()) {
                return Optional.empty();
            }
            return Optional.of(resultSet.getInt(1));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getIncompleteAccountPuuids() {
        try (var statement = connection.createStatement();
             var resultSet = statement.executeQuery(
                     "SELECT puuid, game_name, tag_line FROM accounts WHERE game_name = '' OR tag_line = ''")) {
            List<String> puuids = new ArrayList<>();
            while (resultSet.next()) {
                puuids.add(resultSet.getString(1));
            }
            return puuids;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateAccount(Account account) {
        try (var statement = connection.prepareStatement(
                "UPDATE accounts SET game_name = ?, tag_line = ? WHERE puuid = ?")) {
            statement.setString(1, account.getGameName());
            statement.setString(2, account.getTagLine());
            statement.setString(3, account.getPuuid());
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteAccount(String puuid) {
        try (var statement = connection.prepareStatement("DELTE FROM accounts WHERE puuid = ?")) {
            statement.setString(1, puuid);
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Integer> getCount(Count count) {
        try (var statement = connection.prepareStatement("SELECT value FROM counts WHERE name = ?")) {
            statement.setString(1, count.toString());
            try (var resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(resultSet.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setCount(Count count, int value) {
        try (var statement = connection.prepareStatement("UPDATE counts SET value = ? WHERE name = ?")) {
            statement.setInt(1, value);
            statement.setString(2, count.toString());
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addOrIgnoreMatchIds(List<String> matchIds) {
        try (var statement = connection.prepareStatement("INSERT OR IGNORE INTO match_ids (match_id) VALUES (?)")) {
            connection.setAutoCommit(false);
            int batchSize = 0;
            for (String matchId: matchIds) {
                statement.setString(1, matchId);
                statement.addBatch();
                batchSize++;
                if (batchSize == 500) {
                    statement.executeBatch();
                    connection.commit();
                    batchSize = 0;
                }
            }
            if (batchSize > 0) {
                statement.executeBatch();
                connection.commit();
            }
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<String> getMatchIdAtRow(int row) {
        try (var statement = connection.prepareStatement("SELECT match_id FROM match_ids LIMIT 1 OFFSET ?")) {
            statement.setInt(1, row);
            try (var resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(resultSet.getString(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteMatchId(String matchId) {
        try (var statement = connection.prepareStatement("DELETE FROM match_ids WHERE match_id = ?")) {
            statement.setString(1, matchId);
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Integer> getMatchIdsCount() {
        try (var statement = connection.prepareStatement("SELECT COUNT() FROM match_ids");
             var resultSet = statement.executeQuery()) {
            if (!resultSet.next()) {
                return Optional.empty();
            }
            return Optional.of(resultSet.getInt(1));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
