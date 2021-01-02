import * as gui from '@cristalix/client-api';
import * as easing from '@cristalix/client-api/lib/easing';

(function(plugin: any) {
    let title = gui.text({
        text: '§7???',
        align: {x: 0.5, y: 0.6},
        origin: {x: 0.5, y: 1},
        scale: 0,
        shadow: true
    });
    let subtitle = gui.text({
        text: '§7???',
        align: {x: 0.5, y: 0.6},
        y: 1,
        origin: {x: 0.5, y: 0},
        scale: 0,
        shadow: true
    });

    gui.overlay.push(title, subtitle);

    PluginMessages.on(plugin, 'itemtitle', (bb: ByteBuf) => {
        let item = bb.readItemStack();
        Draw.displayItemActivation(item);
        title.text = UtilNetty.readString(bb, 65535);
        subtitle.text = UtilNetty.readString(bb, 65535);
        title.scale.transit(4, 2700, easing.outElastic, () => {
            title.scale.transit(4, 2700, easing.none, () => {
                title.scale.transit(0, 250, easing.none);
                subtitle.scale.transit(0, 250, easing.none);
            })
        });
        subtitle.scale.transit(2, 500, easing.outElastic);
    });
})(plugin);