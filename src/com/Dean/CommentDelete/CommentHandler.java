package com.Dean.CommentDelete;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

///*

	//*/

	/*
	//
	/// */
	
	/* // */
	
	//   "/*" is a active multi line comment start marker if it is NOT after "//"
	//   "*/" always be a active multi line comment end marker EVEN it after "//"
	
	//  "afde /*deid*/ deinjde /*fdjsie*/"

// need to avoid normal line such as:
//  String s = "// test a normal string/*";// this is a comment "de" /* "


public class CommentHandler extends AbsFileHandler{

	public interface ICommentListener{
		public void handlerComment(String comment);
		public void handlerContentLine(String line);
	}
	
	protected static boolean filterSuffix(File f,String filtersuffix,boolean recursion){
		if(f.isDirectory()){
			if(recursion){
				return true;
			} else {
				return false;
			}
		}
		
		String name = f.getAbsolutePath();
		int index = name.lastIndexOf(".");
		if(index < 0 || index >= name.length() - 1){
			return false;
		}
		String suffix = name.substring(index + 1);
		if(filtersuffix.equalsIgnoreCase(suffix)){
			return true;
		}
		return false;
	}
	
	/**
	 * accept a directory or a file, delete file all comment
	 * 
	 * will return success file count;
	 * 
	 * @param filtersuffix ;such as "java","cpp"
	 * */
	public static int deleteFileComment(final File file,final String filtersuffix,final boolean recursion){
		return directoryRecursion(file, recursion, new IDirectoryListener() {
			@Override
			public int handlerOneFile(File f) {
				return deleteFileComment(f);
			}
			@Override
			public boolean fileterFile(File f) {
				return filterSuffix(f, filtersuffix, recursion);
			}
		});
	}
	
	/**
	 * delete file all comment
	 * 
	 * if success return 0;
	 * else return -1
	 * */
	private static int deleteFileComment(File file){
		int ret = 1;
		final StringBuilder content_no_comment = new StringBuilder();
		
		ret = dealCodeFile(file, new ICommentListener() {
			@Override
			public void handlerContentLine(String line) {
				content_no_comment.append(line);
				content_no_comment.append(line_end_character);
			}
			@Override
			public void handlerComment(String comment) {}
		});
		
		if(ret == 0){
			try {
				writeStringToFile(file,content_no_comment.toString());
			} catch (IOException e) {
				return -1;
			}
		}
		return ret;
	}
	
	/**
	 * accept a directory or a file, delete file all comment
	 * comment all file @Override mark,if it not in comment.
	 * 
	 * will return success file count;
	 * 
	 * @param filtersuffix ;such as "java","cpp"
	 * */
	public static int fixOverrideError(File file,final String filtersuffix,final boolean recursion){
		return directoryRecursion(file, recursion, new IDirectoryListener() {
			
			@Override
			public int handlerOneFile(File f) {
				return fixFileOverrideError(f);
			}
			
			@Override
			public boolean fileterFile(File f) {
				return filterSuffix(f, filtersuffix, recursion);
			}
		});
	}
	
	/**
	 * comment all file @Override mark.
	 * 
	 * if success return 0;
	 * else return -1.
	 * */
	private static int fixFileOverrideError(File file){
		int ret = 0;
		
		final StringBuilder fixed_content = new StringBuilder();
		
		ret = dealCodeFile(file,new ICommentListener() {
			@Override
			public void handlerContentLine(String line) {
				String fixed = line.replace("@Override", "");
				fixed = fixed.replace("@override", "");
				fixed_content.append(fixed);
				fixed_content.append(line_end_character);
			}
			@Override
			public void handlerComment(String comment) {}
		});
		
		if(ret == 0){
			try {
				writeStringToFile(file,fixed_content.toString());
			} catch (IOException e) {
				return -1;
			}
		}
		return ret;
	}
	
	/**
	 * if success return 0;
	 * else return -1.
	 * */
	public static int dealCodeFile(File file,ICommentListener l){
//		FileReader reader = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		
		try {
//			reader = new FileReader(file);
			isr = new InputStreamReader(new FileInputStream(file),"UTF-8");
			br = new BufferedReader(isr);
			dealCodeFile(br, l);
		}catch (FileNotFoundException e) {
			return -1;
		} catch (IOException e) {
			return -1;
		} finally {
			if(br != null){
				try {
					br.close();
				} catch (IOException e) {
					//ignore
				}
			}
			if(isr != null){
				try {
					isr.close();
				} catch (IOException e) {
					//ignore
				}
			}
		}
		return 0;
	}
	
	/**
	 * //fix that situation that // or /* may be in ""
	 * 
	 * if // or /* really in "" return false;
	 * else return true
	 * */
	private static boolean commentEnsure(String line,int find_index){
		//fix that may be the // or /* in ""
		int start = 0;
		int end = -1;
		String t = "\"";
		while((start = line.indexOf(t, start)) > 0){
			end = line.indexOf(t, start + 1);
			if(find_index > start && end > find_index){
				return false;
			}
			
			if(end < 0 || start > find_index){
				break;
			} else {
				start = end + 1;
			}
		}
		
		return true;
	}
	/**
	 * if success return 0;
	 * else return -1.
	 * 
	 * 初始化StringBuilder sb；
	 * 
	 * 循环b   打开文件 读行处理，每次读取一行存入 line 变量；如果line ==null 结束，否则：
	 * havemultilinecomment 标记跨行注释"/*"是不是已出现；
	 * if havemultilinecomment=true，寻找该行有无 "*-/"，
	 * 	     如果有，直接去掉"*-/"的文字，将havemultilinecomment置为false；
	 * 	  else 执行 continue跳过此行；
	 * else
	 * 	 循环a 判断第一个 "//" 和 第一个  "/*"哪个先出现
	 * 		"//" 先出现；line置为  "//"之前的文字，结束循环a ；
	 * 		"/*" 先出现；查询本行是不是有 跨行注释结束符，
	 * 			如果有，将 "/*"和 跨行注释结束符之间的文字都去掉，重返步骤a
	 * 			如果无  结束循环a
	 * 
	 * line加入到sb，
	 * 
	 * 循环b结束；关闭reader等。。将sb写入原文件
	 * @throws IOException 
	 * 
	 * 2012.06.24;避免删除 String s = "// this is a normal string";
	 * 这样的的正常的句子;
	 * 不用考虑引号不出现在同一行的情况;基本上不可能有语言会允许引号的前半段在一行，后半段在另一行的情况
	 * */
	private static void dealCodeFile(BufferedReader br,ICommentListener l) throws IOException{
		if(l == null){
			throw new IllegalArgumentException("CommentHandler dealCodeFile ,the listener can not be NULL");
		}
		StringBuilder multi_line_comment = new StringBuilder();
		String line;
		boolean havemultilinecomment = false;
		while((line = br.readLine()) != null){
			int line_end_multi_comment_index = line.indexOf("*/");
			if(havemultilinecomment){
				if(line_end_multi_comment_index >= 0){
					havemultilinecomment = false;
					multi_line_comment.append(line.substring(0,line_end_multi_comment_index + 2));
					multi_line_comment.append(line_end_character);
					l.handlerComment(multi_line_comment.toString());
					multi_line_comment = new StringBuilder();
					
					line = line.substring(line_end_multi_comment_index + 2);
				} else {
					multi_line_comment.append(line);
					multi_line_comment.append(line_end_character);
					continue;
				}
			}
			
			while(true){
				int line_start_multi_comment_index = line.indexOf("/*");
				int line_single_comment_index = line.indexOf("//");
				
				while(!commentEnsure(line, line_start_multi_comment_index)){
					line_start_multi_comment_index = line.indexOf("/*",line_start_multi_comment_index + 1);
				}
				while(!commentEnsure(line, line_single_comment_index)){
					line_single_comment_index = line.indexOf("//",line_single_comment_index + 1);
				}
				
				if(line_single_comment_index < 0 && line_start_multi_comment_index < 0){// 1
					break;
				}
				
				if(line_single_comment_index >= 0 && 
						(line_start_multi_comment_index < 0 || 
						  line_single_comment_index < line_start_multi_comment_index) ){// 2
					
					String comment = line.substring(line_single_comment_index);
					l.handlerComment(comment);
					line = line.substring(0,line_single_comment_index);
				} else {
					//line_start_multi_comment_index must above or equals zero
					//see the condition at 1 and 2 
					String pre = line.substring(0,line_start_multi_comment_index);
					line_end_multi_comment_index = line.indexOf("*/");
					if(line_start_multi_comment_index > line_end_multi_comment_index){
						line = pre;
						multi_line_comment = new StringBuilder(line.substring(line_start_multi_comment_index));
						multi_line_comment.append(line_end_character);
						havemultilinecomment = true;
						break;
					} else {
						String comment = line.substring(line_start_multi_comment_index,line_end_multi_comment_index + 2);
						l.handlerComment(comment);
						
						line = line.substring(line_end_multi_comment_index + 2,line.length());
						line = pre + line;
					}
				}
			}
			
			l.handlerContentLine(line);
		}
	}
}
