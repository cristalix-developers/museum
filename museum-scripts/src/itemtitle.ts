import * as gui from '@cristalix/client-api';
import * as easing from '@cristalix/client-api/lib/easing';

(function(plugin: any) {
	let item: ItemStack;
	let lines: gui.Text[] = []

	PluginMessages.on(plugin, 'itemtitle', (bb: ByteBuf) => {
		item = bb.readItemStack();
		let duration = bb.readInt();
		let size = bb.readInt();
		let y = size;

		for (let i = 0; i < size; i++) {
			let line = UtilNetty.readString(bb, 65535);

			let title = new gui.Text({
						text: line,
						align: {x: 0.5, y: 0.6},
						y: y,
						origin: {x: 0.5, y: y},
						scale: 0,
						shadow: true
			});
			y--;

			lines.push(title);
		}
		render(duration);
	});


	function clearElements() {
		lines = [];
		//gui.overlay..clearOverlay();
	} 

	function render(duration: number) {
		Draw.displayItemActivation(item);

		for (let i = 0; i < lines.length; i++) {
			let line = lines[i];
			gui.overlay.push(line);

			line.scale.transit(4, duration, easing.outElastic, () => {
				line.scale.transit(0, duration/4, easing.outElastic, () => {
					if (i + 1 == lines.length)
						clearElements();
				});
			});
		}
	}
})(plugin);
