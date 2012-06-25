package com.Dean.CommentDelete;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;

public class AbsFileHandler {
	public static final String line_end_character = "\r\n";
	
	
	public interface IDirectoryListener{
		/**
		 * if success return 0;else return -1
		 * */
		public int handlerOneFile(File f);
		public boolean fileterFile(File f);
	}
	
	protected static void writeStringToFile(File file,String content) throws IOException{
		FileWriter fw = null;
		try {
			fw = new FileWriter(file);
			
			fw.write(content);
		} catch (IOException e) {
			throw e;
		} finally {
			if(fw != null){
				try {
					fw.close();
				} catch (IOException e) {
					//ignore
				}
			}
		}
	}
	
	/**
	 * name can be a file or a dir name
	 * return the success file count
	 * */
	protected static int directoryRecursion(File file,boolean recursion,final IDirectoryListener l){
		if(l == null){
			return 0;
		}
		int count = 0;
		
		if(!file.canRead()){
			Log("file can not read,name="+file.getAbsolutePath());
			return 0;
		} else {
			Log("start handler file = "+ file.getAbsolutePath());
		}
		
		if(file.isFile()){
			int ret = l.handlerOneFile(file);
			Log("Done,is a file,return = "+ ret);
			if(ret == 0){
				count = 1;
			} else {
				count = 0;
			}
			return count;
		}
		
		File[] children = null;
		children = file.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return l.fileterFile(pathname);
			}
		});
		
		if(children == null){
			//Log("no children");
			return 0;
		}
		final int N = children.length;
		for(int i = 0; i < N ;++i){
			File f = children[i];
			int err;
			if(f.isDirectory()){
				if(recursion){
					count += directoryRecursion(f, recursion, l);
				}
			} else {
				err = l.handlerOneFile(f);
				Log("hander a file,return = "+ err);
				if(err == 0){
					++count;
				}
			}
		}
		Log("Done,success file count = "+ count);
		return count;
	}
	
	public static void Log(String msg){
		System.out.println(msg);
	}
}
