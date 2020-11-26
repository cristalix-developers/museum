import * as gui from '@cristalix/client-api';

(function(plugin: any) {
	gui.register(plugin);

	let text = gui.text({
		text: '',
		y: -50,
		scale: 1,
		origin: gui.BOTTOM,
		align: gui.BOTTOM
	});

	let subjects: Subject[] = [];

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
		uuid: string;
	};

	PluginMessages.on(plugin, 'museumsubjects', (bb: ByteBuf) => {
		subjects = JSON.parse(UtilNetty.readString(bb, 65536));
	});

	let activeSubject: Subject = null;

	Events.on(plugin, 'game_loop', (event) => {
		let pos = Player.getTargetBlockPos(6);
		let x = pos.getX();
		let y = pos.getY();
		let z = pos.getZ();

		let newActiveSubject: Subject = null;
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
				text.text = "Нажмите §bEnter§f для настройки";
			}
			text.enabled = !!activeSubject;
		}
	});

	gui.overlay.push(text);

	Events.on(plugin, 'key_press', (event: KeyPressEvent) => {
		if (event.key == Keyboard.KEY_RETURN && activeSubject) {
			ChatExtensions.sendChatMessage("/gui manipulator " + activeSubject.uuid);
		}
	});
})(plugin);
