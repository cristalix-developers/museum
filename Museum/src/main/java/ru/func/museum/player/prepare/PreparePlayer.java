package ru.func.museum.player.prepare;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author func 03.06.2020
 * @project Museum
 */
@Getter
@AllArgsConstructor
public enum PreparePlayer {
    PACKET_HANDLER(new BeforePacketHandler()),
    INVENTORY(new PrepareInventory()),
    MUSEUM(new PrepareMuseum()),
    SCOREBOARD(new PrepareScoreBoard()),;

    private Prepare prepare;
}
