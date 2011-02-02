package com.gluster.storage.management.core.utils;

import java.io.File;
import java.io.FileInputStream;

import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;

public class FileUtil {
	public String readFileAsString(File file) {
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			byte[] data = new byte[fileInputStream.available()];
			fileInputStream.read(data);
			fileInputStream.close();
			
			return new String(data);
		} catch (Exception e) {
			e.printStackTrace();
			throw new GlusterRuntimeException("Could not read file [" + file + "]", e);
		}
	}
}