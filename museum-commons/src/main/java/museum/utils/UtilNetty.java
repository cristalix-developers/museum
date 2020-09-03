package museum.utils;

import com.google.gson.Gson;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.SneakyThrows;
import museum.packages.MuseumPackage;
import museum.packages.PackageWrapper;

public class UtilNetty {

	private static final Gson gson = new Gson();

	public static TextWebSocketFrame toFrame(MuseumPackage museumPackage) {
		return new TextWebSocketFrame(gson.toJson(new PackageWrapper(museumPackage.getClass().getName(), gson.toJson(museumPackage))));
	}

	@SneakyThrows
	public static MuseumPackage readFrame(TextWebSocketFrame textFrame) {
		PackageWrapper wrapper = gson.fromJson(textFrame.text(), PackageWrapper.class);
		return (MuseumPackage) gson.fromJson(wrapper.getObjectData(), Class.forName(wrapper.getClazz()));
	}

}
