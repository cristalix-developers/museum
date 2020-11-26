import * as gui from '@cristalix/client-api';

(function(plugin: any) {
	let show = true;

	Events.on(plugin, 'key_press', (e: KeyPressEvent) => {
		if (e.key === Keyboard.KEY_J) {
			show = !show
			title.enabled = show;
			background.enabled = show;
		}
	});

	const title = gui.rect({
		color: {a: 0.5, r: 0, g: 1, b: 0},
		align: {x: 0.015, y: 0.03},
	});
	const background = gui.rect({
        width: 235,
        height: 165,
        color: {a: 0.6, r: 0, g: 0, b: 0},
    });

	let yScale = 0;
	for (let entry of [
		"§eНаходите останки динозавров на ",
		"§eраскопках, ставьте/окрашивайте фонтаны",
		"§eи витрины §f(§bПКМ§f)§e, взлетите!",
		"§eПосетители кидают монеты, собирайте их.",
		"",
		"§6Как начать раскопки?",
		"§bМеню §f-> §bЭкспедиции §f-> §bОстров Бриз",
		"§fКол-во ударов ограничено!",
		"§fЛавка продает еду (перед музеем),",
		"§fно еда кончается, поэтому",
		"§fзаказывайте продукты в меню, забирайте",
		"§fгруз у желтой метки и несите в лавку.",
		"",
		"§f- Магазин построек §b§l/shop",
		"§fНажмите '§b§lJ§f' - §bскрыть/показать §fэто окно"
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