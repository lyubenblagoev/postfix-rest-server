package com.lyubenblagoev.postfixrest;

import java.io.File;

import org.assertj.core.util.Files;

public final class FileUtils {
	
	private FileUtils() {
		// Prevent class instantiation
	}

	public static boolean renameFolder(File parentFolder, String oldName, String newName) {
		File oldFolder = new File(parentFolder, oldName);
		File newFolder = new File(parentFolder, newName);
		return oldFolder.renameTo(newFolder);
	}
	
	public static void deleteFolder(File parentFolder, String name) {
		File folder = new File(parentFolder, name);
		Files.delete(folder);
	}

}
