package com.lyubenblagoev.postfixrest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FileUtils {
	
	private static final Logger log = LoggerFactory.getLogger(FileUtils.class);
	
	private FileUtils() {
		// Prevent class instantiation
	}

	public static void renameFolder(File parentFolder, String oldName, String newName) {
		String parentPath = parentFolder.getPath();
		Path oldPath = Paths.get(parentPath, oldName);
		if (!Files.exists(oldPath)) {
			log.info("The path specified doesn't exist: {}", oldPath);
			return;
		}
		Path newPath = Paths.get(parentPath, newName);
		try {
			Files.move(oldPath, newPath);
		} catch (IOException e) {
			log.error("Failed to rename {} to {}", oldName, newName);
		}
	}
	
	public static void deleteFolder(File parentFolder, String name) {
		Path path = Paths.get(parentFolder.getPath(), name);
		try {
			Files.delete(path);
		} catch (IOException e) {
			log.error("Failed to delete {}", path);
		}
	}

}
