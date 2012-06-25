package com.Dean.CommentDelete;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		if(args.length <= 0){
//			Log("please input the file or dir name;");
//			return ;
//		}
		
		String rootdir = "C:\\Users\\Dean\\old_data\\notebook_develop\\foxit\\3������Ŀ�ͳ�����Դ\\FoxitMobile2.0-20111215\\svn\\FxMobileReader2_0_lite\\src";//args[0];
		rootdir = "C:\\Users\\Dean\\old_data\\notebook_develop\\foxit\\3������Ŀ�ͳ�����Դ\\FoxitMobile2.0-20111215\\svn\\test.java";
		if(rootdir == null){
			Log("input file name is null;");
			return ;
		}
		
		File root = new File(rootdir);
//		CommentHandler.fixOverrideError(root, "java", true);
		CommentHandler.deleteFileComment(root,"java",true);
	}
	
	/**
	 * name can be a file or a dir name
	 * 
	 * @return the success file count
	 * */
	public static int deleteDirFileComment(File file,final String filtersuffix,boolean recursion){
		int ret = 0;
		
		if(!file.canRead()){
			Log("file can not read,name="+file.getAbsolutePath());
			throw new IllegalArgumentException("deleteDirFileComment: file can not read");
		} else {
			Log("start handler file = "+ file.getAbsolutePath());
		}
		
		if(file.isFile()){
			try {
				deleteFileComment(file);
				ret = 1;
			} catch (IOException e) {
				ret = 0;
			}
			Log("Done,is a file,return = "+ ret);
			return ret;
		}
		
		File[] children = null;
		if(filtersuffix == null || filtersuffix.length() <= 0){
			children = file.listFiles();
		} else {
			children = file.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					if(pathname.isDirectory()){
						return true;
					}
					String name = pathname.getAbsolutePath();
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
			});
		}
		
		if(children == null){
			Log("catch Error_Code_No_File ,ret ="+ret);
			return 0;
		}
		final int N = children.length;
		for(int i = 0; i < N ;++i){
			File f = children[i];
			if(f.isDirectory()){
				if(recursion){
					ret += deleteDirFileComment(f,filtersuffix,recursion);
				}
			} else {
				try {
					deleteFileComment(f);
					++ret;
				} catch (IOException e) {
				}
			}
		}
		Log("Done,success file count = "+ ret);
		return ret;
	}
	
	// /*
	
	//*/

	/*
	//
	/// */
	
	/* // */
	
	//   "/*" is a active multi line comment start marker if it is NOT after "//"
	//   "*/" always be a active multi line comment end marker EVEN it after "//"
	
	//  "afde /*deid*/ deinjde /*fdjsie*/"
	/**
	 * if success return 1;
	 * else return error code.
	 * 
	 * ��ʼ��StringBuilder sb��
	 * 
	 * ѭ��b   ���ļ� ���д���ÿ�ζ�ȡһ�д��� line ���������line ==null ����������
	 * havemultilinecomment ��ǿ���ע��"/*"�ǲ����ѳ��֣�
	 * if havemultilinecomment=true��Ѱ�Ҹ������� "*-/"��
	 * 	     ����У�ֱ��ȥ��"*-/"�����֣���havemultilinecomment��Ϊfalse��
	 * 	  else ִ�� continue�������У�
	 * else
	 * 	 ѭ��a �жϵ�һ�� "//" �� ��һ��  "/*"�ĸ��ȳ���
	 * 		"//" �ȳ��֣�line��Ϊ  "//"֮ǰ�����֣�����ѭ��a ��
	 * 		"/*" �ȳ��֣���ѯ�����ǲ����� ����ע�ͽ�������
	 * 			����У��� "/*"�� ����ע�ͽ�����֮������ֶ�ȥ�����ط�����a
	 * 			�����  ����ѭ��a
	 * 
	 * line���뵽sb��
	 * 
	 * ѭ��b�������ر�reader�ȡ�����sbд��ԭ�ļ�
	 * @throws IOException 
	 * 
	 * */
	public static void deleteFileComment(File file) throws IOException{
		final String line_end_character = "\r\n";
		FileReader reader = null;
		BufferedReader br = null;
		StringBuilder content_no_comment = new StringBuilder();
		try {
			reader = new FileReader(file);
			br = new BufferedReader(reader);
			String line;
			boolean havemultilinecomment = false;
			
			while((line = br.readLine()) != null){
				
				int line_end_multi_comment_index = line.indexOf("*/");
				if(havemultilinecomment){
					if(line_end_multi_comment_index >= 0){
						havemultilinecomment = false;
						line = line.substring(line_end_multi_comment_index + 2);
					} else {
						continue;
					}
				}
				
				while(true){
					int line_start_multi_comment_index = line.indexOf("/*");
					int line_single_comment_index = line.indexOf("//");
					
					if(line_single_comment_index < 0 && line_start_multi_comment_index < 0){// 1
						break;
					}
					
					if(line_single_comment_index >= 0 && 
							(line_start_multi_comment_index < 0 || 
							  line_single_comment_index < line_start_multi_comment_index) ){// 2
						line = line.substring(0,line_single_comment_index);
					} else {
						//line_start_multi_comment_index must above or equals zero
						//see the condition at 1 and 2 
						String pre = line.substring(0,line_start_multi_comment_index);
						line_end_multi_comment_index = line.indexOf("*/");
						if(line_start_multi_comment_index > line_end_multi_comment_index){
							line = pre;
							havemultilinecomment = true;
							break;
						} else {
							line = line.substring(line_end_multi_comment_index + 2,line.length());
							line = pre + line;
						}
					}
				}
				
				content_no_comment.append(line);
				if(line.length() > 0){
					content_no_comment.append(line_end_character);
				}
			}
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			if(br != null){
				try {
					br.close();
				} catch (IOException e) {
					//ignore
				}
			}
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e) {
					//ignore
				}
			}
		}
		FileWriter fw = null;
		try {
			fw = new FileWriter(file);
			
			fw.write(content_no_comment.toString());
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
	
	public static void Log(String msg){
		System.out.println(msg);
	}
}
