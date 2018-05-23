package com.example.lixiang.imageload.utils;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import org.kymjs.kjframe.utils.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 文件操作工具包
 * 
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class FileUtil {



	//递归删除文件夹
	public static void deleteFile(File file) {
		if (file.exists()) {//判断文件是否存在
			if (file.isFile()) {//判断是否是文件
				file.delete();//删除文件
			} else if (file.isDirectory()) {//否则如果它是一个目录
				File[] files = file.listFiles();//声明目录下所有的文件 files[];
				for (int i = 0;i < files.length;i ++) {//遍历目录下所有的文件
					deleteFile(files[i]);//把每个文件用这个方法进行迭代
				}
				file.delete();//删除文件夹
			}
		} else {
//			System.out.println("所删除的文件不存在");
		}
	}

	//获取一个文件夹下所有文件的名字
	public static String getFilesName(File file) {
		String str = "";
		if (file.exists()) {
			LinkedList<File> list = new LinkedList<File>();
			File[] files = file.listFiles();
			for (File file2 : files) {
				if (file2.isDirectory()) {
					list.add(file2);
				} else {
					str = str+file2.getName()+",";
				}
			}
			File temp_file;
			while (!list.isEmpty()) {
				temp_file = list.removeFirst();
				files = temp_file.listFiles();
				for (File file2 : files) {
					if (file2.isDirectory()) {
						list.add(file2);
					} else {
						str = str+file2.getName()+",";
					}
				}
			}
		} else {
			return  str;
		}
		return str;
	}



	/**
	 * 写文本文件 在Android系统中，文件保存在 /data/data/PACKAGE_NAME/files 目录下
	 * getCacheDir()方法用于获取/data/data//cache目录
	 *
	 * getFilesDir()方法用于获取/data/data//files目录
	 *
	 * @param context
	 */
	public static void write(Context context, String fileName, String content) {
		if (content == null)
			content = "";

		try {
			FileOutputStream fos = context.openFileOutput(fileName,
					Context.MODE_PRIVATE);
			fos.write(content.getBytes());

			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取默认缓存地址文件夹
	 * 	getCacheDir()方法用于获取/data/data//cache目录
	 * getFilesDir()方法用于获取/data/data//files目录
	 *
	 */
	public static File getBaseDiskCacheDir(Context context, String destFileDirName)
	{
		String cachePath;
		if (checkSaveLocationExists())//检查是否安装SD卡
		{
			cachePath = context.getExternalCacheDir().getPath();
		} else
		{
			cachePath = context.getCacheDir().getPath();
		}
		File dir = new File(cachePath + File.separator + destFileDirName);
		if (!dir.exists())
		{
			dir.mkdirs();
		}
		return dir;
	}



	/**
 * 获取默认缓存地址文件夹
 * 	getCacheDir()方法用于获取/data/data//cache目录
 * getFilesDir()方法用于获取/data/data//files目录
 * 
 */
	public static File getDiskCacheDir(Context context, String uniqueName, String fileNmae)
	{

		return new File(getBaseDiskCacheDir(context,uniqueName), fileNmae);
	}

	
	
	
	
	
	/**
	 * 读取文本文件
	 * 
	 * @param context
	 * @param fileName
	 * @return
	 */
	public static String read(Context context, String fileName) {
		try {
			FileInputStream in = context.openFileInput(fileName);
			return readInStream(in);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String readInStream(InputStream inStream) {
		try {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			byte[] buffer = new byte[512];
			int length = -1;
			while ((length = inStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, length);
			}

			outStream.close();
			inStream.close();
			return outStream.toString();
		} catch (IOException e) {
			Log.i("FileTest", e.getMessage());
		}
		return null;
	}

	public static File createFile(String folderPath, String fileName) {
		File destDir = new File(folderPath);
		if (!destDir.exists()) {
			destDir.mkdirs();
		}
		return new File(folderPath, fileName + fileName);
	}

	/**
	 * 向手机写图片
	 * 
	 * @param buffer
	 * @param folder
	 * @param fileName
	 * @return
	 */
	public static boolean writeFile(byte[] buffer,
			String folderPath,String fileName) {
		boolean writeSucc = false;

//		boolean sdCardExist = Environment.getExternalStorageState().equals(
//				android.os.Environment.MEDIA_MOUNTED);

//		String folderPath = "";
//		if (sdCardExist) {
//			folderPath = Environment.getExternalStorageDirectory()
//					+ File.separator + folder + File.separator;
//		} else {
//			writeSucc = false;
//		}

		File fileDir = new File(folderPath);
		if (!fileDir.exists()) {
			fileDir.mkdirs();
		}

		File file = new File(folderPath,fileName);
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
			out.write(buffer);
			writeSucc = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {

				out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return writeSucc;
	}

	/**
	 * 根据文件绝对路径获取文件名
	 * 
	 * @param filePath
	 * @return
	 */
	public static String getFileName(String filePath) {
		if (StringUtils.isEmpty(filePath))
			return "";
		return filePath.substring(filePath.lastIndexOf(File.separator) + 1);
	}

	/**
	 * 根据文件的绝对路径获取文件名但不包含扩展名
	 * 
	 * @param filePath
	 * @return
	 */
	public static String getFileNameNoFormat(String filePath) {
		if (StringUtils.isEmpty(filePath)) {
			return "";
		}
		int point = filePath.lastIndexOf('.');
		return filePath.substring(filePath.lastIndexOf(File.separator) + 1,
				point);
	}

	/**
	 * 获取文件扩展名
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getFileFormat(String fileName) {
		if (StringUtils.isEmpty(fileName))
			return "";

		int point = fileName.lastIndexOf('.');
		return fileName.substring(point + 1);
	}

	/**
	 * 获取文件大小
	 * 
	 * @param filePath
	 * @return
	 */
	public static long getFileSize(String filePath) {
		long size = 0;

		File file = new File(filePath);
		if (file != null && file.exists()) {
			size = file.length();
		}
		return size;
	}

	/**
	 * 获取文件大小
	 * 
	 * @param size
	 *            字节
	 * @return
	 */
	public static String getFileSize(long size) {
		if (size <= 0)
			return "0";
		java.text.DecimalFormat df = new java.text.DecimalFormat("##.##");
		float temp = (float) size / 1024;
		if (temp >= 1024) {
			return df.format(temp / 1024) + "M";
		} else {
			return df.format(temp) + "K";
		}
	}
	public static float  getFileSizeToK(long size) {
		if (size <= 0)
			return 0;
		float temp = (float) size / 1024;
		return temp ;
	}
	/**
	 * 转换文件大小
	 * 
	 * @param fileS
	 * @return B/KB/MB/GB
	 */
	public static String formatFileSize(long fileS) {
		java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");
		String fileSizeString = "";
		if (fileS < 1024) {
			fileSizeString = df.format((double) fileS) + "B";
		} else if (fileS < 1048576) {
			fileSizeString = df.format((double) fileS / 1024) + "KB";
		} else if (fileS < 1073741824) {
			fileSizeString = df.format((double) fileS / 1048576) + "MB";
		} else {
			fileSizeString = df.format((double) fileS / 1073741824) + "G";
		}
		return fileSizeString;
	}

	/**
	 * 获取目录文件大小
	 * 
	 * @param dir
	 * @return
	 */
	public static long getDirSize(File dir) {
		if (dir == null) {
			return 0;
		}
		if (!dir.isDirectory()) {
			return 0;
		}
		long dirSize = 0;
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isFile()) {
				dirSize += file.length();
			} else if (file.isDirectory()) {
				dirSize += file.length();
				dirSize += getDirSize(file); // 递归调用继续统计
			}
		}
		return dirSize;
	}

	/**
	 * 获取目录文件个数
	 * 
	 * @param emojiFragment
	 * @return
	 */
	public static long getFileList(File dir) {
		long count = 0;
		File[] files = dir.listFiles();
		count = files.length;
		for (File file : files) {
			if (file.isDirectory()) {
				count = count + getFileList(file);// 递归
				count--;
			}
		}
		return count;
	}

	public static byte[] toBytes(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int ch;
		while ((ch = in.read()) != -1) {
			out.write(ch);
		}
		byte buffer[] = out.toByteArray();
		out.close();
		return buffer;
	}

	/**
	 * 检查文件是否存在
	 * 
	 * @param name
	 * @return
	 */
	public static boolean checkFileExists(String name) {
		boolean status;
		if (!name.equals("")) {
			File path = Environment.getExternalStorageDirectory();
			File newPath = new File(path.toString() + name);
			status = newPath.exists();
		} else {
			status = false;
		}
		return status;
	}

	/**
	 * 检查路径是否存在
	 * 
	 * @param path
	 * @return
	 */
	public static boolean checkFilePathExists(String path) {
		return new File(path).exists();
	}

	/**
	 * 计算SD卡的剩余空间
	 * 
	 * @return 返回-1，说明没有安装sd卡
	 */
	public static long getFreeDiskSpace( Context context) {
//		long freeSpace = 0;
		if (checkSaveLocationExists())//检查是否安装SD卡
		{
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
				StatFs fs = new StatFs(Environment.getExternalStorageDirectory().getPath());
				//Android API18之前：fs.getAvailableBlocks()*fs.getBlockSize()
				//Android API18之前：fs.getAvailableBlocks()*fs.getBlockSize()
				return fs.getAvailableBlocks()*fs.getBlockSize()/1024/1024;
//				return Long.parseLong(Formatter.formatFileSize(context, (fs.getAvailableBytes())))/1024/1024;
			}
			return  -1;
		} else
		{
			StatFs fs = new StatFs(Environment.getDataDirectory().getPath());
			return fs.getAvailableBlocks()*fs.getBlockSize()/1024/1024;
//			return Long.parseLong(Formatter.formatFileSize(context, (fs.getAvailableBytes())))/1024/1024;
		}


//		String status = Environment.getExternalStorageState();
//		long freeSpace = 0;
//		if (status.equals(Environment.MEDIA_MOUNTED)) {
//			try {
//				File path = Environment.getExternalStorageDirectory();
//				StatFs stat = new StatFs(path.getPath());
//				long blockSize = stat.getBlockSize();
//				long availableBlocks = stat.getAvailableBlocks();
//				freeSpace = availableBlocks * blockSize / 1024;
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		} else {
//			return -1;
//		}
//		return (freeSpace);
	}

	/**
	 * 新建目录
	 * 
	 * @param directoryName
	 * @return
	 */
	public static boolean createDirectory(String directoryName) {
		boolean status;
		if (!directoryName.equals("")) {
			File path = Environment.getExternalStorageDirectory();
			File newPath = new File(path.toString() + directoryName);
			status = newPath.mkdir();
			status = true;
		} else
			status = false;
		return status;
	}

	/**
	 * 检查是否安装SD卡
	 * 
	 * @return
	 */
	public static boolean checkSaveLocationExists() {
		String sDCardStatus = Environment.getExternalStorageState();
		boolean status;
		if (sDCardStatus.equals(Environment.MEDIA_MOUNTED)) {
			status = true;
		} else
			status = false;
		return status;
	}

	/**
	 * 检查是否安装外置的SD卡
	 * 
	 * @return
	 */
	public static boolean checkExternalSDExists() {

		Map<String, String> evn = System.getenv();
		return evn.containsKey("SECONDARY_STORAGE");
	}

	/**
	 * 删除目录(包括：目录里的所有文件)
	 * 
	 * @param fileName
	 * @return
	 */
	public static boolean deleteExternalStorageDirectory(String fileName) {
		boolean status;


		if (!fileName.equals("")) {

			File path = Environment.getExternalStorageDirectory();
			File newPath = new File(path.toString() + fileName);

			return deleteDirectory(newPath);

		} else
			status = false;
		return status;
	}

	public static boolean deleteDirectory(File newPath) {
		boolean status;
		SecurityManager checker = new SecurityManager();
		checker.checkDelete(newPath.toString());
		if (newPath.isDirectory()) {
			String[] listfile = newPath.list();
			try {
				for (int i = 0; i < listfile.length; i++) {
					File deletedFile = new File(newPath.toString() + "/"
							+ listfile[i]);
					deletedFile.delete();
				}
				newPath.delete();
//					Log.i("DirectoryManager deleteDirectory", fileName);
				status = true;
			} catch (Exception e) {
				e.printStackTrace();
				status = false;
			}

		} else
			status = false;

		return status;
	}

	/**
	 * 删除缓存目录下文件
	 * 
	 * @param fileName
	 * @return
	 */
	public  boolean deleteFile(String fileName) {
		boolean status;
		SecurityManager checker = new SecurityManager();

		if (!fileName.equals("")) {

			File path = Environment.getExternalStorageDirectory();
			File newPath = new File(path.toString() + fileName);
			checker.checkDelete(newPath.toString());
			if (newPath.isFile()) {
				try {
//					Log.i("DirectoryManager deleteFile", fileName);
					newPath.delete();
					status = true;
				} catch (SecurityException se) {
					se.printStackTrace();
					status = false;
				}
			} else
				status = false;
		} else
			status = false;
		return status;
	}

	/**
	 * 删除空目录
	 * 
	 * 返回 0代表成功 ,1 代表没有删除权限, 2代表不是空目录,3 代表未知错误
	 * 
	 * @return
	 */
	public static int deleteBlankPath(String path) {
		File f = new File(path);
		if (!f.canWrite()) {
			return 1;
		}
		if (f.list() != null && f.list().length > 0) {
			return 2;
		}
		if (f.delete()) {
			return 0;
		}
		return 3;
	}

	/**
	 * 重命名
	 * 
	 * @param oldName
	 * @param newName
	 * @return
	 */
	public static boolean reNamePath(String oldName, String newName) {
		File f = new File(oldName);
		return f.renameTo(new File(newName));
	}

	/**
	 * 删除文件
	 * 
	 * @param filePath
	 */
	public static boolean deleteFileWithPath(String filePath) {
		SecurityManager checker = new SecurityManager();
		File f = new File(filePath);
		checker.checkDelete(filePath);
		if (f.isFile()) {
			f.delete();
			return true;
		}
		return false;
	}

	/**
	 * 清空一个文件夹
	 * 
	 * @param files
	 */
	public static void clearFileWithPath(String filePath) {
		List<File> files = FileUtil.listPathFiles(filePath);
		if (files.isEmpty()) {
			return;
		}
		for (File f : files) {
			if (f.isDirectory()) {
				clearFileWithPath(f.getAbsolutePath());
			} else {
				f.delete();
			}
		}
	}

	/**
	 * 获取SD卡的根目录
	 * 
	 * @return
	 */
	public static String getSDRoot() {

		return Environment.getExternalStorageDirectory().getAbsolutePath();
	}

	/**
	 * 获取手机外置SD卡的根目录
	 * 
	 * @return
	 */
	public static String getExternalSDRoot() {

		Map<String, String> evn = System.getenv();

		return evn.get("SECONDARY_STORAGE");
	}

	/**
	 * 列出root目录下所有子目录
	 * 
	 * @param path
	 * @return 绝对路径
	 */
	public static List<String> listPath(String root) {
		List<String> allDir = new ArrayList<String>();
		SecurityManager checker = new SecurityManager();
		File path = new File(root);
		checker.checkRead(root);
		// 过滤掉以.开始的文件夹
		if (path.isDirectory()) {
			for (File f : path.listFiles()) {
				if (f.isDirectory() && !f.getName().startsWith(".")) {
					allDir.add(f.getAbsolutePath());
				}
			}
		}
		return allDir;
	}

	/**
	 * 获取一个文件夹下的所有文件
	 * 
	 * @param root
	 * @return
	 */
	public static List<File> listPathFiles(String root) {
		List<File> allDir = new ArrayList<File>();
		SecurityManager checker = new SecurityManager();
		File path = new File(root);
		checker.checkRead(root);
		File[] files = path.listFiles();
		for (File f : files) {
			if (f.isFile())
				allDir.add(f);
			else
				listPath(f.getAbsolutePath());
		}
		return allDir;
	}

	public enum PathStatus {
		SUCCESS, EXITS, ERROR
	}

	/**
	 * 创建目录
	 *
	 */
	public static PathStatus createPath(String newPath) {
		File path = new File(newPath);
		if (path.exists()) {
			return PathStatus.EXITS;
		}
		if (path.mkdir()) {
			return PathStatus.SUCCESS;
		} else {
			return PathStatus.ERROR;
		}
	}

	/**
	 * 截取路径名
	 * 
	 * @return
	 */
	public static String getPathName(String absolutePath) {
		int start = absolutePath.lastIndexOf(File.separator) + 1;
		int end = absolutePath.length();
		return absolutePath.substring(start, end);
	}

	/**
	 * 获取应用程序缓存文件夹下的指定目录
	 * 
	 * @param context
	 * @param dir
	 * @return
	 */
	public static String getAppCache(Context context, String dir) {
		String savePath = context.getCacheDir().getAbsolutePath() + "/" + dir
				+ "/";
		File savedir = new File(savePath);
		if (!savedir.exists()) {
			savedir.mkdirs();
		}
		savedir = null;
		return savePath;
	}





	/**
	 * 复制单个文件
	 * @param oldPath String 原文件路径 如：c:/fqf.txt
	 * @param newPath String 复制后路径 如：f:/fqf.txt
	 * @return boolean
	 */
	public static void copyFile(String oldPath, String newPath) {
		try {
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPath);
			if (oldfile.exists()) { //文件存在时
				InputStream inStream = new FileInputStream(oldPath); //读入原文件
				FileOutputStream fs = new FileOutputStream(newPath);
				byte[] buffer = new byte[1444];
				int length;
				while ( (byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread; //字节数 文件大小
//					System.out.println(bytesum);
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
			}
		}
		catch (Exception e) {
			System.out.println("复制单个文件操作出错");
			e.printStackTrace();

		}

	}



	//写数据到SD中的文件
	public void writeFileSdcardFile(String fileName,String write_str) throws IOException{
		try{

			FileOutputStream fout = new FileOutputStream(fileName);
			byte [] bytes = write_str.getBytes();

			fout.write(bytes);
			fout.close();
		}

		catch(Exception e){
			e.printStackTrace();
		}
	}






	/**
	 * 重命名
	 *@author LiXaing
	 *create at 2017/4/4 22:51
	 */
	public static boolean rename(File oldFile,String newFileName) throws ReNameException {
		if(!oldFile.exists())
		{
			throw new NullPointerException("oldFile not found");
		}
		String rootPath = oldFile.getParent();
		File newFile = new File(rootPath + File.separator + newFileName);

		if (oldFile.renameTo(newFile))
		{
			System.out.println("rename -> 修改成功!");
			return true;
		}
		else
		{
			System.out.println("rename -> 修改失败");
			throw new ReNameException("Current file rename failed");
		}
	}

	static class ReNameException extends Exception{
//    ReNameFailException

		public ReNameException()
		{

		}
		public ReNameException(String message)
		{
			super(message);
		}



	}

}