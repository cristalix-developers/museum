package museum.service.data.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

@Getter
@RequiredArgsConstructor
public class Config {

	private final String fileName;
	private String contents;

	public boolean refresh() {
		try {
			String newContents = Base64.getEncoder().encodeToString(String.join("\n", Files.readAllLines(new File(fileName).toPath())).getBytes());
			if (newContents.equals(contents)) return false;
			contents = newContents;
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

}
