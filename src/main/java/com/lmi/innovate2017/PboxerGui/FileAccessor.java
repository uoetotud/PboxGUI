package com.lmi.innovate2017.PboxerGui;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileAccessor {	
	public static List<String> read(Path filePath) throws IOException {
		if (Files.exists(filePath, LinkOption.NOFOLLOW_LINKS)) {
			return Files.readAllLines(filePath);
		}
		
		return null;
	}
	
	public static void write(Path filePath, int lineNo, String value) throws IOException {
		if (!Files.exists(filePath, LinkOption.NOFOLLOW_LINKS)) {
		    Files.createFile(filePath);
		    List<String> initLines = new ArrayList<String>();
		    initLines.add("0");
		    initLines.add("0");
		    Files.write(filePath, initLines, StandardCharsets.UTF_8);
		}
		
		List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
		if (lines.size() != 2) {
			lines.clear();
			lines.add("0");
			lines.add("0");
		}
		
		lines.set(lineNo, value);
		Files.write(filePath, lines, StandardCharsets.UTF_8);
	}
}