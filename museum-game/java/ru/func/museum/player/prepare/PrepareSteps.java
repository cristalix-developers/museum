package ru.func.museum.player.prepare;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author func 03.06.2020
 * @project Museum
 */
@Getter
@AllArgsConstructor
public enum PrepareSteps {
    PACKET_HANDLER(new BeforePacketHandler()),
    INVENTORY(new PrepareInventory()),
    MUSEUM(new PrepareMuseum()),
    SCOREBOARD(new PrepareScoreBoard()),
    ANIME(new PrepareJSAnime()),
    HIDE_PLAYERS(new PreparePlayers());

    private Prepare prepare;
}