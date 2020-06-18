package ru.func.museum.player.prepare;

import ru.func.museum.App;
import ru.func.museum.player.User;

public interface Prepare {
    void execute(User user, App app);
}
