import * as gui from '@cristalix/client-api';
import * as easing from '@cristalix/client-api/lib/easing';

(function (plugin: any) {
    let show = false;
    let startTime = -1;
    let duration = 5;

    PluginMessages.on(plugin, 'museumcast', (bb: ByteBuf) => {
        message.text = UtilNetty.readString(bb, 65536);
        startTime = System.currentTimeMillis(),
        show = true;
    });

    const message = gui.text({
        align: {x: 0.5, y: 0.75},
		origin: gui.CENTER,
        scale: 1.3,
    });

    const menu = gui.rect({
        width: 370,
        height: 0,
        align: {x: 0.5, y: 0},
        origin: {x: 0.5, y: 0.5},
        color: {a: 0.6, r: 0, g: 0, b: 0},
        scale: 1,
        children: [message]
    });

    Events.on(plugin, 'game_loop', () => {
        let active = (System.currentTimeMillis() - startTime) / 1000 < duration;
        message.enabled = active;
        menu.height.transit(
            active ? 80 : 0,
            800,
            easing.outBack,
            () => show = message.enabled = false
        );
    });

    gui.overlay.push(menu);
})(plugin);