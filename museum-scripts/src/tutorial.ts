import * as gui from '@cristalix/client-api';

(function(plugin: any) {
	let show = true;

	Events.on(plugin, 'key_press', (e: KeyPressEvent) => {
		if (e.key === Keyboard.KEY_N) {
			show = !show
			title.enabled = show;
			background.enabled = show;
		} else if (e.key === Keyboard.KEY_M) {
			ChatExtensions.sendChatMessage("/gui excavation")
		} else if (e.key === Keyboard.KEY_H) {
			ChatExtensions.sendChatMessage("/helps")
		} else if (e.key === Keyboard.KEY_G) {
			ChatExtensions.sendChatMessage("/prefixes")
		}
	});

	const title = gui.rect({
		color: {a: 0.5, r: 0, g: 1, b: 0},
		align: {x: 0.01, y: 0.4},
	});

	const background = gui.rect({
		width: 195,
		height: 90,
		align: {x: 0, y: 0.39},
        color: {a: 0.6, r: 0, g: 0, b: 0},
    });

	let yScale = 0;
	for (let entry of [
		"§fГорячие клавиши",
		"",
		"§b§lM§f - §bкарта мира §f㸾",
		"§b§lN§f - §bскрыть/показать §fэто окно 㱬",
		"§b§lH§f - §bответы §fна разные вопросы 㗒",
		"§b§lG§f - §fменю префиксов 䁿",
		"",
		"§bПриятной игры в новом году! §f㶅"
	]) {
		title.children.push(gui.text({
			text: entry,
			color: {a: 1, r: 0, g: 1, b: 0},
			origin: {x: 0, y: -yScale},
			align: {x: 0, y: -yScale},
			shadow: true
		}));
		yScale += 1.14;
	}
	title.enabled = show;
    background.enabled = show;

	gui.overlay.push(background);
	gui.overlay.push(title);
})(plugin);