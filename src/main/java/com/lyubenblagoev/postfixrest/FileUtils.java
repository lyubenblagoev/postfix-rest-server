package com.lyubenblagoev.postfixrest;

import java.io.File;

public class FileUtils {

	public static boolean renameFolder(File parentFolder, String oldName, String newName) {
		File oldFolder = new File(parentFolder, oldName);
		File newFolder = new File(parentFolder, newName);
		return oldFolder.renameTo(newFolder);
	}
	
	public static boolean deleteFolder(File parentFolder, String name) {
		File folder = new File(parentFolder, name);
		return folder.delete();
	}

}
