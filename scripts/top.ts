/*
* ВЕРСИЯ ДЛЯ ПРОСМОТРА, КОМПИЛЯЦИЯ С https://github.com/DelfikPro/d.ts
* ЗАГРУЗКА СКРИПТА ТОЛЬКО ЧЕРЕЗ СЕРВЕР
* */


/// <reference path="../../d.ts/src/api/d.ts" />
import * as easing from '../../d.ts/src/api/easing';
import * as gui from '../../d.ts/src/api/gui';
import {rect} from '../../d.ts/src/api/gui';

type Top = {
    type: string,
    data: User[]
}

type User = {

};

(function(plugin: any) {
	gui.register(plugin);

	let textLines: string[] = [
    	"wonna die",
    ];

    let boardWidth = 200;
    let offset = (boardWidth - 2) / 3;

    let board = gui.rect({
    	width: 200,
    	origin: {x: 0.5, y: 0},

    	children: [
    		rect({
    			z: -1,
    			width: 3, height: 3,
    			align: {x: 0, y: 0},
    			origin: {x: 0.5, y: 0.5},
    			color: {a: 1, r: 1, g: 0, b: 0}
    		}),
    		rect({
    			z: -1,
    			width: 3, height: 3,
    			align: {x: 1, y: 0},
    			origin: {x: 0.5, y: 0.5},
    			color: {a: 1, r: 1, g: 0, b: 0}
    		})
    	]
    });

    let entity = gui.rect({
    	scale: 0.0625 * 0.5,
    	children: [board],
    });

	for (let i = 0; i < textLines.length; i++) {
    	board.children.push(gui.rect({
    		origin: {x: 0.5, y: 0},
    		align: {x: 0.5, y: 0},
    		y: (i + 0.5) * 10 + (offset + 1) / 2,
    		children: [gui.rect({
    			width: 200,
    			height: 9,
    			origin: {x: 0.5, y: 0.5},
    			color: {a: 0.5, r: 0, b: 0, g: 0},
    			children: [
    				gui.text({
    					align: {x: 0.5, y: 0},
    					origin: {x: 0.5, y: 0},
    					text: textLines[i]
    				})
    			]
    		})]
    	}));
    }

	let scroll = 0;

	function updateCulling(): void {
    	for (let lineWrapper of board.children) {
    		let y = lineWrapper.y.value + board.y.value;
    		// let angle = y < 0 ? -90 : y > 100 ? 90 : 0;
    		let scale = y < 0 ? 0 : y > 100 ? 0 : 1;
    		// let line = (lineWrapper as gui.Box).children[0]
    		// if (line.rotationX.toValue != angle) line.rotationX.transit(angle, 250, easing.none);
    		if (lineWrapper.scale.toValue != scale) lineWrapper.scale.transit(scale, 250, easing.none);
    	}
    }

	function updateData(): void {
    	for (let i = 0; i <= textLines.length; i++) {
    	    board.children[i+1] = gui.rect({
    		origin: {x: 0.5, y: 0},
    		align: {x: 0.5, y: 0},
    		y: (i + 0.5) * 10 + (offset + 1) / 2,
    		children: [gui.rect({
    			width: 200,
    			height: 9,
    			origin: {x: 0.5, y: 0.5},
    			color: {a: 0.5, r: 0, b: 0, g: 0},
    			children: [
    				gui.text({
    					align: {x: 0.5, y: 0},
    					origin: {x: 0.5, y: 0},
    					text: textLines[i]
    				})
    			]
    		})]
    	})}
    }

	PluginMessages.on(plugin, 'museum:top', (bb: ByteBuf) => {
	    let data = UtilNetty.readString(bb, 65535);
		let tops: Top[] = JSON.parse(data);
		for (let i = 0; i <= tops.length; i++) {
		    for (let j = 0; j <= tops.length; j++) {
                textLines[j] = tops[i].data.length + " хочу умереть"
		    }
		}
	});

    Events.on(plugin, 'game_loop', () => {
    	let dwheel = Mouse.getDWheel() / 10;
    	if (dwheel) {
    		scroll += dwheel;
            board.y.transit(scroll, 400, easing.outQuint);
    	}
    });

    Events.on(plugin, 'render_pass_ticks', (event: RenderPassEvent) => {
        GL11.glPushMatrix();

        let holoX = 291;
        let holoY = 95;
        let holoZ = -285;

        let t = event.partialTicks;

        GL11.glTranslatef(
        	holoX - Player.getPosX() - (Player.getPosX() - Player.getPrevX()) * t,
        	holoY - Player.getPosY() - (Player.getPosY() - Player.getPrevY()) * t,
        	holoZ - Player.getPosZ() - (Player.getPosZ() - Player.getPrevZ()) * t
        );
        GL11.glScalef(1, -1, -1);

        updateData();
    	updateCulling();
    	entity.render(JavaSystem.currentTimeMillis(), 16, 16);

    	GL11.glPopMatrix();
    });
})(plugin);