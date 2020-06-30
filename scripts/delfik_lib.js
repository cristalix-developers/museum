const c1 = 1.70158;
const c2 = c1 * 1.525;
const c3 = c1 + 1;
const c4 = 2 * Math.PI / 3;
const c5 = 2 * Math.PI / 4.5;
const pow = Math.pow;
const sin = Math.sin;

const outBackEasing = function (x) {
    return 1 + c3 * (x - 1) * (x - 1) * (x - 1) + c1 * (x - 1) * (x - 1);
};
const outElasticEasing = function (x) {
    return x === 0 ? 0 : x === 1 ? 1 : pow(2, -10 * x) * sin((x * 10 - 0.75) * c4) + 1
};
const noEasing = function (x) {
    return x;
};

let p = -1;

const CENTER = 0.5;
const LEFT = 0;
const RIGHT = 1;
const TOP = 0;
const BOTTOM = 1;

function transitWrap(defaultValue) {
    return {
        started: 0.0,
        duration: 0.0,
        fromValue: 0.0,
        value: defaultValue,
        toValue: 0.0,
        easingFunction: function (value) {
            return value;
        },


        transit: function (value, duration, easingFunction) {

            this.fromValue = this.value;
            this.toValue = value;
            this.started = System.currentTimeMillis();
            this.duration = duration;
            this.running = true;
            this.easingFunction = easingFunction;

        },

        update: function (time) {
            if (!this.started) return;
            let part = (time - this.started) / this.duration;
            p = part;
            if (part > 1.0) {
                this.value = this.toValue;
                this.started = 0.0;
            } else {
                part = this.easingFunction(part);
                this.value = this.fromValue + (this.toValue - this.fromValue) * part;
            }
        }
    }
}

function intColor(a, r, g, b) {
    return a * 255 << 24 | r * 255 << 16 | g * 255 << 8 | b * 255;
}

function createStack(data) {
    let x = data.x || 0;
    let y = data.y || 0;
    let color = data.color || {a: 1};
    let a = color.a || 0;
    let r = color.r || 0;
    let g = color.g || 0;
    let b = color.b || 0;

    let scale = data.scale || 1;

    let align = data.align || [0, 0];
    let alignX = align[0] || 0;
    let alignY = align[1] || 0;

    let origin = data.origin || [0, 0];
    let originX = origin[0] || 0;
    let originY = origin[1] || 0;

    let rotation = data.rotation || 0;


    return {
        x: transitWrap(x),
        y: transitWrap(y),
        a: transitWrap(a),
        r: transitWrap(r),
        g: transitWrap(g),
        b: transitWrap(b),
        scale: transitWrap(scale),
        alignX: alignX,
        alignY: alignY,
        originX: originX,
        originY: originY,
        rotation: transitWrap(rotation),
        lastColor: 0,

        render: function (time, parentWidth, parentHeight, elementWidth, elementHeight) {
            this.x.update(time);
            this.y.update(time);
            this.a.update(time);
            this.r.update(time);
            this.g.update(time);
            this.b.update(time);
            this.scale.update(time);
            this.rotation.update(time);
            GL11.glTranslatef(parentWidth * this.alignX, parentHeight * this.alignY, 0)
            GL11.glScalef(this.scale.value, this.scale.value, 1);
            GL11.glRotatef(this.rotation.value, 0, 0, 1);
            GL11.glTranslatef(-elementWidth * this.originX, -elementHeight * this.originY, 0);
            GL11.glTranslatef(this.x.value, this.y.value, 0);
            this.lastColor = intColor(this.a.value, this.r.value, this.g.value, this.b.value);
        }
    }
}

function createRectangle(data) {
    let stack = createStack(data);
    let width = data.width || 0;
    let height = data.height || 0;
    let children = data.children || [];
    return {
        stack: stack,
        width: width,
        height: height,
        children: children,

        render: function (time, parentWidth, parentHeight) {

            GL11.glPushMatrix();
            this.stack.render(time, parentWidth, parentHeight, this.width, this.height);
            Draw.drawRect(0, 0, this.width, this.height, this.stack.lastColor);

            for (var i = 0; i < children.length; i++) {
                children[i].render(time, this.width, this.height);
            }
            GL11.glPopMatrix();
        }
    }
}

function createText(data) {
    let stack = createStack(data);
    let text = data.text || "";
    return {
        stack: stack,
        text: text,

        render: function (time, parentWidth, parentHeight) {
            GL11.glPushMatrix();
            this.stack.render(time, parentWidth, parentHeight, Draw.getStringWidth(this.text), 9);
            Draw.drawString(text, 0, 0);
            GL11.glPopMatrix();
        }
    }
}

const content = createRectangle({
    origin: [CENTER, CENTER],
    width: 100,
    height: 50,
    children: [
        createText({
            x: 10,
            y: 10,
            text: "Hello, kasdo."
        })
    ]
});

const container = createRectangle({children: [content]});

let lastMouseX = -1;
let lastMouseY = -1;

Events.on(self, 'gui_overlay_render', function (e) {
    let factor = Draw.getResolution().getScaleFactor();
    let screenWidth = Display.getWidth() / factor;
    let screenHeight = Display.getHeight() / factor;
    Draw.drawString(p + "", 1, 1);
    let mouseX = Mouse.getX() / factor;
    let mouseY = screenHeight - Mouse.getY() / factor;
    if (lastMouseX !== mouseX || lastMouseY !== mouseY) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        container.stack.x.value += (mouseX - container.stack.x.value) / 8;
        container.stack.y.value += (mouseY - container.stack.y.value) / 8;
        container.stack.x.transit(mouseX, 300, outBackEasing);
        container.stack.y.transit(mouseY, 300, outBackEasing);
    }
    container.render(System.currentTimeMillis(), screenWidth, screenHeight);
});

var lmbPressed = false;
var rmbPressed = false;
var big = false;
Events.on(self, 'game_loop', function (e) {
    if (Mouse.isButtonDown(0)) {
        if (!lmbPressed) {
            lmbPressed = true;
            // content.stack.rotation.transit(Math.random() * 720 - 360, 500, outBackEasing);
            content.stack.a.transit(Math.random(), 500, noEasing);
            content.stack.r.transit(Math.random(), 500, noEasing);
            content.stack.g.transit(Math.random(), 500, noEasing);
            content.stack.b.transit(Math.random(), 500, noEasing);
        }
    } else lmbPressed = false;
    if (Mouse.isButtonDown(1)) {
        if (!rmbPressed) {
            rmbPressed = true;
            big = !big;
            content.stack.scale.transit(big ? 1.6 : 0.5, 250, outBackEasing);
        }
    } else rmbPressed = false;

    // str += Mouse.getDWheel();

})
