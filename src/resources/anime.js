(function (self) {
    var str = '';
    var textSize = 2;
    var topPadding = 34;

    PluginMessages.on(self, 'museum', function (bb) {
        var n = UtilNetty.readVarInt(bb) - 1;
        str = n >= 0 ? n + " " + plural(n) : n === -2 ? "возвращение..." : "";
    });

    function setupTilt() {
        GL11.glRotatef(319.54, -0.977, 0.736, -0.212);
    }

    function plural(n) {
        return n % 10 === 1 && n % 100 !== 11 ? "удар" : (n % 10 < 2 || n % 10 > 4 || n % 100 >= 10 && n % 100 < 20 ? "ударов" : "удара")
    }

    Events.on(self, 'gui_overlay_render', function (e) {
        if (!str)
            return;

        var factor = Draw.getResolution().getScaleFactor();
        var width = Draw.getStringWidth(str) * textSize;
        var x = Display.getWidth() / factor / 2 - width / 2, y = Display.getHeight() / factor / 2 + topPadding;

        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0);
        GL11.glPushMatrix();
        setupTilt();

        Draw.drawRect(0, 0, width + 6, textSize * 11, 0x60000000);
        GL11.glPopMatrix();
        GL11.glTranslatef(0, 0, 10);
        GL11.glPushMatrix();
        setupTilt();

        GL11.glScalef(textSize, textSize, textSize);
        Draw.drawString(str, 2, 2, 0x90FFEAEF);
        GL11.glPopMatrix();
        GL11.glPopMatrix();
    });
})(this);