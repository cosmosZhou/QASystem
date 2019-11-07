package com.robot.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

import org.apache.log4j.Logger;

import com.robot.semantic.RNN.RNNTopicClassifier;
import com.util.Utility;
import com.util.Utility.Text;

/**
 * @author xpqiu
 * @version 1.0 文档数据读取如下： 输入为数据存放路径（子文件夹不处理） 不同类别存放在各自文件中 类别：文件名 数据：文件内的一行字符
 *          package edu.fudan.ml.data
 */
public class InstanceReader implements Iterable<Instance>, Iterator<Instance> {

	String content = null;
	boolean bDubious = false;
	Text reader;
	File fpath;
	FileNode currentFile;

	public String classNames[];

	public InstanceReader(String path) throws UnsupportedEncodingException, FileNotFoundException {
		this(path, ".data");
	}

	/**
	 * 
	 * @param path
	 *            路径名
	 * @param charsetName
	 *            字符编码
	 * @param filter
	 *            文件类型过滤
	 */
	String filter;

	public InstanceReader(String path, String filter) throws UnsupportedEncodingException, FileNotFoundException {
		LinkedList<File> files = new LinkedList<File>();
		this.filter = filter;
		this.fpath = new File(path);

		if (!fpath.isDirectory()) {
			throw new RuntimeException("输入必须为目录");
		}

		File[] flist = fpath.listFiles();
		for (int i = 0; i < flist.length; i++) {
			//			log.info("reading: " + flist[i].toString());
			if (flist[i].isFile()) {
				if (filter == null)
					files.push(flist[i]);
				else if (flist[i].getName().endsWith(filter))
					files.push(flist[i]);
			} else if (flist[i].isDirectory()) {
				files.push(flist[i]);
			}
		}

		if (files.size() == 0) {
			log.info("找不到合法文件");
		}

		//		Charset charset = Charset.forName(charsetName);

		classNames = new String[files.size()];
		HashSet<String> classNamesSet = new HashSet<String>();
		int i = 0;
		for (File file : files) {
			String className;
			if (file.isDirectory()) {
				className = file.getName();
			} else {
				className = getLabel(file);
			}

			classNames[i++] = className;
			if (!classNamesSet.add(className)) {
				throw new RuntimeException(className + " is already contained in the name set!");
			}
		}
	}

	abstract class TreeNode {
		DirectoryNode parent;
		String label;

		abstract FileNode leftMost();

		abstract FileNode next();
	}

	class FileNode extends TreeNode {
		File fileName;
		HashSet<String> set = new HashSet<String>();

		FileNode(File fpath, DirectoryNode parent, String label) {
			fileName = fpath;
			this.parent = parent;
			this.label = label;
		}

		FileNode next() {
			int index = this.parent.files.indexOf(this);
			if (index + 1 < parent.files.size()) {
				return parent.files.get(index + 1).leftMost();
			}
			return this.parent.next();
		}

		FileNode leftMost() {
			return this;
		}

	}

	class DirectoryNode extends TreeNode {
		LinkedList<TreeNode> files = new LinkedList<TreeNode>();

		DirectoryNode(File fpath, DirectoryNode parent, String label) {
			this.parent = parent;
			this.label = label;
			if (!fpath.isDirectory()) {
				throw new RuntimeException("输入必须为目录");
			}

			File[] flist = fpath.listFiles();
			for (int i = 0; i < flist.length; i++) {
				if (flist[i].isFile()) {
					if (filter == null || flist[i].getName().endsWith(filter))
						files.push(new FileNode(flist[i], this, label == null ? getLabel(flist[i]) : label));
				} else if (flist[i].isDirectory()) {
					files.push(new DirectoryNode(flist[i], this, label == null ? flist[i].getName() : label));
				}
			}

			if (files.size() == 0) {
				log.info("fpath = " + fpath);
				log.info("label = " + label);
				throw new RuntimeException("找不到合法文件");
			}

		}

		FileNode leftMost() {
			TreeNode first = files.peek();
			if (first instanceof FileNode) {
				return (FileNode) first;
			}
			return ((DirectoryNode) first).leftMost();
		}

		@Override
		FileNode next() {
			if (parent == null) {
				return null;
			}

			int index = this.parent.files.indexOf(this);
			if (index + 1 < parent.files.size()) {
				return parent.files.get(index + 1).leftMost();
			}
			return this.parent.next();
		}
	}

	public boolean hasNext() {
		try {
			for (;;) {
				if (reader == null) {
					currentFile = new DirectoryNode(fpath, null, null).leftMost();
				} else if (reader.hasNext()) {
					content = reader.next();
					if (content.startsWith(";")) {
						content = content.substring(1);

						if (content.isEmpty()) {
							continue;
						}
						bDubious = true;
					} else {
						bDubious = false;
					}
					if (!currentFile.set.add(content)) {
						continue;
					}

					return true;
				} else {
					currentFile = currentFile.next();
					if (currentFile == null) {
						return false;
					}
				}
				reader = new Utility.Text(currentFile.fileName.toString());
			}
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public Instance next() {
		Instance inst = new Instance(content, getLabel());
		inst.bDubious = bDubious;
		return inst;
	}

	public String getLabel() {
		return currentFile.label;
	}

	static public String getLabel(File currentFile) {
		return currentFile.getName().substring(0, currentFile.getName().lastIndexOf('.'));
	}

	@Override
	public Iterator<Instance> iterator() {
		// TODO Auto-generated method stub
		return this;
	}

	static Logger log = Logger.getLogger(InstanceReader.class);
}
