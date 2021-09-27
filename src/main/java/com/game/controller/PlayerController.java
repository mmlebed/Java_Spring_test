package com.game.controller;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.service.PlayerServiceImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/players")
public class PlayerController {
    private final PlayerServiceImpl playerService;

    public PlayerController(PlayerServiceImpl playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    @ResponseBody
    public List<Player> getAllPlayers(@RequestParam(value = "name", required = false)  String name,
                                      @RequestParam(value = "title", required = false)  String title,
                                      @RequestParam(value = "race", required = false) Race race,
                                      @RequestParam(value = "profession", required = false) Profession profession,
                                      @RequestParam(value = "after", required = false) Long after,
                                      @RequestParam(value = "before", required = false) Long before,
                                      @RequestParam(value = "banned", required = false) Boolean banned,
                                      @RequestParam(value = "minExperience", required = false) Integer minExperience,
                                      @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
                                      @RequestParam(value = "minLevel", required = false) Integer minLevel,
                                      @RequestParam(value = "maxLevel", required = false) Integer maxLevel,
                                      @RequestParam(value = "order", required = false, defaultValue = "ID") PlayerOrder playerOrder,
                                      @RequestParam(value = "pageNumber", defaultValue = "0") Integer pageNumber,
                                      @RequestParam(value = "pageSize", defaultValue = "3") Integer pageSize) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(playerOrder.getFieldName()));

        return playerService.getAllPlayers(
                Specification.where(playerService.filterByName(name))
                        .and(playerService.filterByTitle(title))
                        .and(playerService.filterByRace(race))
                        .and(playerService.filterByProfession(profession))
                        .and(playerService.filterByBirthday(after, before))
                        .and(playerService.filterByBanned(banned))
                        .and(playerService.filterByExperience(minExperience, maxExperience))
                        .and(playerService.filterByLevel(minLevel, maxLevel)),
                pageable).getContent();
    }

    @GetMapping("/count")
    @ResponseBody
    public Long getPlayersCount(@RequestParam(value = "name", required = false)  String name,
                                @RequestParam(value = "title", required = false)  String title,
                                @RequestParam(value = "race", required = false) Race race,
                                @RequestParam(value = "profession", required = false) Profession profession,
                                @RequestParam(value = "after", required = false) Long after,
                                @RequestParam(value = "before", required = false) Long before,
                                @RequestParam(value = "banned", required = false) Boolean banned,
                                @RequestParam(value = "minExperience", required = false) Integer minExperience,
                                @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
                                @RequestParam(value = "minLevel", required = false) Integer minLevel,
                                @RequestParam(value = "maxLevel", required = false) Integer maxLevel) {
        return playerService.getPlayersCount(Specification.where(playerService.filterByName(name))
                .and(playerService.filterByTitle(title))
                .and(playerService.filterByRace(race))
                .and(playerService.filterByProfession(profession))
                .and(playerService.filterByBirthday(after, before))
                .and(playerService.filterByBanned(banned))
                .and(playerService.filterByExperience(minExperience, maxExperience))
                .and(playerService.filterByLevel(minLevel, maxLevel)));
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<Player> createPlayer(@RequestBody Player player) {
        playerService.createPlayer(player);
        return ResponseEntity.ok(player);
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Player> getPlayerById(@PathVariable Long id) {
        Player player = playerService.getPlayerById(id);
        return ResponseEntity.ok(player);
    }

    @PostMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Player> updatePlayer(@PathVariable Long id, @RequestBody Player oldPlayer) {
        Player newPlayer = playerService.updatePlayer(id, oldPlayer);
        return ResponseEntity.ok(newPlayer);
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Player> deletePlayerById(@PathVariable Long id) {
        playerService.deletePlayerById(id);
        return ResponseEntity.ok().build();
    }
}
