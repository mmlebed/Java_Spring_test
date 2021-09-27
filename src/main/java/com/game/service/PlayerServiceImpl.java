package com.game.service;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.exceptions.BadRequestException;
import com.game.exceptions.NotFoundException;
import com.game.repository.PlayerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

@Service
public class PlayerServiceImpl  implements PlayerService {
    private static final int VARCHAR_LENGTH_BY_NAME = 12;
    private static final int VARCHAR_LENGTH_BY_TITLE = 30;
    private static final int MAX_SIZE_BY_EXPERIENCE = 10000000;
    private static final long MIN_SIZE_BY_BIRTHDAY = 2000L;
    private static final long MAX_SIZE_BY_BIRTHDAY = 3000L;

    private PlayerRepository playerRepository;

    public PlayerServiceImpl(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    public Page<Player> getAllPlayers(Specification<Player> specification, Pageable pageable) {
        return playerRepository.findAll(specification, pageable);
    }

    @Override
    public Long getPlayersCount(Specification<Player> specification) {
        return playerRepository.count(specification);
    }

    @Override
    public Player createPlayer(Player player) {
        checkName(player.getName());
        checkTitle(player.getTitle());
        checkRace(player.getRace());
        checkProfession(player.getProfession());
        checkBirthday(player.getBirthday());
        checkExperience(player.getExperience());
        if (player.getBanned() == null) {
            player.setBanned(false);
        }
        player.setLevel(calculateLevel(player.getExperience()));
        player.setUntilNextLevel(calculateUntilNextLevel(player.getExperience(), player.getLevel()));
        return playerRepository.saveAndFlush(player);
    }

    @Override
    public Player getPlayerById(Long id) {
        checkId(id);
        return playerRepository.findById(id).orElseThrow(() ->
                new NotFoundException(String.format("Player with id %d not found!", id)));
    }

    @Override
    public Player updatePlayer(Long id, Player oldPlayer) {
        Player newPlayer = getPlayerById(id);
        if(oldPlayer.getName() != null) {
            checkName(oldPlayer.getName());
            newPlayer.setName(oldPlayer.getName());
        }
        if(oldPlayer.getTitle() != null) {
            checkTitle(oldPlayer.getTitle());
            newPlayer.setTitle(oldPlayer.getTitle());
        }
        if(oldPlayer.getRace() != null) {
            checkRace(oldPlayer.getRace());
            newPlayer.setRace(oldPlayer.getRace());
        }
        if(oldPlayer.getProfession() != null) {
            checkProfession(oldPlayer.getProfession());
            newPlayer.setProfession(oldPlayer.getProfession());
        }
        if(oldPlayer.getBirthday() != null) {
            checkBirthday(oldPlayer.getBirthday());
            newPlayer.setBirthday(oldPlayer.getBirthday());
        }
        if(oldPlayer.getBanned() != null) {
            newPlayer.setBanned(oldPlayer.getBanned());
        }
        if(oldPlayer.getExperience() != null) {
            checkExperience(oldPlayer.getExperience());
            newPlayer.setExperience(oldPlayer.getExperience());
        }
        newPlayer.setLevel(calculateLevel(newPlayer.getExperience()));
        newPlayer.setUntilNextLevel(calculateUntilNextLevel(newPlayer.getExperience(), newPlayer.getLevel()));
        return playerRepository.save(newPlayer);
    }

    @Override
    public Player deletePlayerById(Long id) {
        Player deletePlayer = getPlayerById(id);
        playerRepository.delete(deletePlayer);
        return deletePlayer;
    }

    @Override
    public Specification<Player> filterByName(String name) {
        return (root, query, cb) -> name == null ? null : cb.like(root.get("name"), "%" + name + "%");
    }

    @Override
    public Specification<Player> filterByTitle(String title) {
        return (root, query, cb) -> title == null ? null : cb.like(root.get("title"), "%" + title + "%");
    }

    @Override
    public Specification<Player> filterByRace(Race race) {
        return (root, query, cb) -> race == null ? null : cb.equal(root.get("race"), race);
    }

    @Override
    public Specification<Player> filterByProfession(Profession profession) {
        return (root, query, cb) -> profession == null ? null : cb.equal(root.get("profession"), profession);
    }

    @Override
    public Specification<Player> filterByBirthday(Long after, Long before) {
        return (root, query, cb) -> {
            if (after == null && before == null) {
                return null;
            }
            if (after == null) {
                return cb.lessThanOrEqualTo(root.get("birthday"), new Date(before));
            }
            if (before == null) {
                return cb.greaterThanOrEqualTo(root.get("birthday"), new Date(after));
            }
            return cb.between(root.get("birthday"), new Date(after), new Date(before));
        };
    }

    @Override
    public Specification<Player> filterByBanned(Boolean isBanned) {
        return (root, query, cb) -> {
            if (isBanned == null) {
                return null;
            }
            if (isBanned) {
                return cb.isTrue(root.get("banned"));
            }
            return cb.isFalse(root.get("banned"));
        };
    }

    @Override
    public Specification<Player> filterByExperience(Integer min, Integer max) {
        return (root, query, cb) -> {
            if (min == null && max == null) {
                return null;
            }
            if (min == null) {
                return cb.lessThanOrEqualTo(root.get("experience"), max);
            }
            if (max == null) {
                return cb.greaterThanOrEqualTo(root.get("experience"), min);
            }
            return cb.between(root.get("experience"), min, max);
        };
    }

    @Override
    public Specification<Player> filterByLevel(Integer min, Integer max) {
        return (root, query, cb) -> {
            if (min == null && max == null) {
                return null;
            }
            if (min == null) {
                return cb.lessThanOrEqualTo(root.get("level"), max);
            }
            if (max == null) {
                return cb.greaterThanOrEqualTo(root.get("level"), min);
            }
            return cb.between(root.get("level"), min, max);
        };
    }

    @Override
    public Specification<Player> filterByUntilNextLevel(Integer min, Integer max) {
        return (root, query, cb) -> {
            if (min == null && max == null) {
                return null;
            }
            if (min == null) {
                return cb.lessThanOrEqualTo(root.get("untilNextLevel"), max);
            }
            if (max == null) {
                return cb.greaterThanOrEqualTo(root.get("untilNextLevel"), min);
            }
            return cb.between(root.get("untilNextLevel"), min, max);
        };
    }

    public void checkId(Long id) {
        if(id <= 0)
            throw new BadRequestException("Player ID is invalid");
    }

    public void checkName(String name) {
        if(name == null || name.isEmpty() || name.length() > VARCHAR_LENGTH_BY_NAME)
            throw new BadRequestException("Player name is invalid");
    }

    public void checkTitle(String title) {
        if(title.length() > VARCHAR_LENGTH_BY_TITLE)
            throw new BadRequestException("Player title is invalid");
    }

    public void checkRace(Race race) {
        if(race == null)
            throw new BadRequestException("Player race is invalid");
    }

    public void checkProfession(Profession profession) {
        if(profession == null)
            throw new BadRequestException("Player profession is invalid");
    }

    public void checkBirthday(Date birthday) {
        if(birthday == null)
            throw new BadRequestException("Player birthday is invalid");

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(birthday.getTime());
        if (calendar.get(Calendar.YEAR) < MIN_SIZE_BY_BIRTHDAY || calendar.get(Calendar.YEAR) > MAX_SIZE_BY_BIRTHDAY)
            throw new BadRequestException("Player birthday is out of bounds");
    }

    public void checkExperience(Integer experience) {
        if(experience < 0 || experience > MAX_SIZE_BY_EXPERIENCE)
            throw new BadRequestException("Player experience is invalid");
    }

    public Integer calculateLevel(Integer experience) {
        Integer resultLevel;
        resultLevel = (int) Math.sqrt(2500 + 200 * experience);
        resultLevel -= 50;
        resultLevel /= 100;
        return resultLevel;
    }

    private Integer calculateUntilNextLevel(Integer experience, Integer level) {
        return 50 * (level + 1) * (level + 2) - experience;
    }
}
