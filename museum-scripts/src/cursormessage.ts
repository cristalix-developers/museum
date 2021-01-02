import * as gui from '@cristalix/client-api';
import * as easing from '@cristalix/client-api/lib/easing';

(function (plugin: any) {
    const menu = gui.rect({
        width: 100,
        height: 100,
        color: {a: 0, r: 0, g: 0, b: 0},
        align: {x: 0.5, y: 0.7},
        origin: {x: 0.5, y: 0.5}
    });

    PluginMessages.on(plugin, 'museumcursor', (bb: ByteBuf) => {
        let message = gui.text({
            align: gui.TOP_LEFT,
            origin: gui.TOP_LEFT,
            text: UtilNetty.readString(bb, 65536),
            scale: 1,
        });
        message.alignX.transit(
            Math.random() - .5,
            3300,
            easing.bothSin
        );
        message.alignY.transit(
            0.4,
            3400,
            easing.bothSin,
            () => {
                menu.children.shift();
            }
        );
        message.r.transit(
            0.2,
            3300,
            easing.inSin
        );
        menu.children.push(message);
    });

    Events.on(plugin, 'game_loop', () => {
        menu.children.forEach(view => view.updateAnimatables(1))
    });
    gui.overlay.push(menu);
})(plugin);