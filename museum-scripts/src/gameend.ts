import * as gui from '@cristalix/client-api';
import {Callback, Element} from '@cristalix/client-api';
import * as easing from '@cristalix/client-api/lib/easing';

(function (plugin: any) {

    const menu = gui.rect({
        color: {a: 0, r: 0, g: 0, b: 0},
        align: {x: 0.5, y: 0.5},
        origin: {x: 0.5, y: 0.5},
        enabled: false,
    });

    const left = new gui.Item({
        item: null,
        scale: 5,
        align: {x: 4, y: -4},
        origin: {x: 4, y: -4},
        enabled: false,
    });
    const right = new gui.Item({
        item: null,
        scale: 5,
        color: {a: 0, r: 0, g: 0, b: 0},
        align: {x: 4, y: 4},
        origin: {x: 4, y: 4},
        enabled: false,
        rotationZ: 90
    });

    const text = gui.rect({
        color: {a: 0.62, r: 0, g: 0, b: 0},
        align: {x: 0.5, y: 0.5},
        origin: {x: 0.5, y: 0.5},
        width: 3000,
        height: 0,
        enabled: false
    });

    const title = gui.text({
        text: "§a§lПОБЕДА",
        scale: 4,
        color: {a: 0, r: 0, g: 0, b: 0},
        align: {x: 0.5, y: 5},
        origin: {x: 0.5, y: 5},
        enabled: false,
        shadow: true
    });
    const highlight = gui.text({
        text: "§lПОБЕДА",
        scale: 0,
        color: {a: 0, r: 0, g: 0, b: 0},
        align: {x: 0.5, y: 0.5},
        origin: {x: 0.5, y: 0.5},
        enabled: false,
        shadow: true
    });

    menu.children.push(left, right);
    text.children.push(title, highlight);

    PluginMessages.on(plugin, 'func:gameend', (bb: ByteBuf) => {
        left.item = bb.readItemStack();
        right.item = bb.readItemStack();

        menu.enabled = text.enabled = true;
        menu.children.forEach(view => view.enabled = true);
        text.children.forEach(view => view.enabled = true);

        goCenter(left);
        goCenter(right);
        text.height.transit(300, 2000, easing.none);
        title.alignY.transit(0.5, 3000, easing.outQuad, () => {
            highlight.scale.transit(
                4,
                2000,
                easing.outSin
            )
        });
    });

    Events.on(plugin, 'game_loop', () => {
        menu.children.forEach(view => view.updateAnimatables(1))
    });

    gui.overlay.push(menu, text);
})(plugin);

function goCenter(element: Element, after?: Callback) {
    let duration = 2700;
    let animation = easing.outQuad;
    element.alignX.transit(
        0.5,
        duration,
        animation
    );
    element.alignY.transit(
        0.5,
        duration,
        animation
    );
    element.originX.transit(
        0.5,
        duration,
        animation
    );
    if (after) {
        element.originY.transit(
            0.5,
            duration,
            animation,
            after
        );
    } else {
        element.originY.transit(
            0.5,
            duration,
            animation,
        );
    }
}