import * as gui from '@cristalix/client-api';

(function(plugin: any) {
	var priceText = gui.text({
		text: 'Цена: §aМнога $$$',
		y: 36,
		scale: 2,
		shadow: true,
		origin: gui.CENTER,
	});
	var titleText = gui.text({
		text: 'Башая витрина',
		y: 21,
		scale: 2,
		shadow: true,
		origin: gui.CENTER,
	});
	var buyText = gui.text({
		text: 'Нажмите §eEnter§f, чтобы купить',
		y: 10,
		shadow: true,
		origin: gui.CENTER,
	});

	var box = gui.rect({
		align: gui.CENTER,
		children: [
			priceText,
			titleText,
			buyText
		]
	});

	System.currentTimeMillis();

	box.enabled = false;

	gui.overlay.push(box)

	var subjects: Subject[] = [];

	type V3 = {
		x: number;
		y: number;
		z: number;
	};

	type Subject = {
		address: string;
		title: string;
		min: V3;
		max: V3;
		cost: number;
	};

	PluginMessages.on(plugin, 'shop', (bb: ByteBuf) => {
		var data = UtilNetty.readString(bb, 65536);
		subjects = JSON.parse(data);
	});

	var activeSubject: Subject = null;

	Events.on(plugin, 'game_loop', (event) => {
		let pos = Player.getTargetBlockPos(5);
		let x = pos.getX();
		let y = pos.getY();
		let z = pos.getZ();

		var newActiveSubject: Subject = null;

		for (let subject of subjects) {
			let a = subject.min;
			let b = subject.max;
			if (a.x <= x && x <= b.x && a.y <= y && y <= b.y && a.z <= z && z <= b.z) {
				newActiveSubject = subject;
			}
		}

		if (newActiveSubject != activeSubject) {
			activeSubject = newActiveSubject;
			if (activeSubject) {
				priceText.text = "Цена: §a" + activeSubject.cost + "$";
				titleText.text = activeSubject.title;
			}
			box.enabled = !!activeSubject;
		}
	});

	Events.on(plugin, 'key_press', (event: KeyPressEvent) => {
		if (event.key == Keyboard.KEY_RETURN && activeSubject) {
			ChatExtensions.sendChatMessage("/buy " + activeSubject.address);
		}
	});

	PluginMessages.on(plugin, 'disable', (bb) => {
		PluginMessages.off(plugin);
		Events.off(plugin);
	})

})(plugin);

