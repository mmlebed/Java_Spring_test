package com.game.service;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface PlayerService {
    Page<Player> getAllPlayers(Specification<Player> specification, Pageable pageable);
    Long getPlayersCount(Specification<Player> specification);
    Player createPlayer(Player player);
    Player getPlayerById(Long id);
    Player updatePlayer(Long id, Player oldPlayer);
    Player deletePlayerById(Long id);
    Specification<Player> filterByName(String name);
    Specification<Player> filterByTitle(String title);
    Specification<Player> filterByRace(Race race);
    Specification<Player> filterByProfession(Profession profession);
    Specification<Player> filterByBirthday(Long after, Long before);
    Specification<Player> filterByBanned(Boolean isBanned);
    Specification<Player> filterByExperience(Integer min, Integer max);
    Specification<Player> filterByLevel(Integer min, Integer max);
    Specification<Player> filterByUntilNextLevel(Integer min, Integer max);
}
