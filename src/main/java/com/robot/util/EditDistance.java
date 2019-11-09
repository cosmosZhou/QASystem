package com.robot.util;

/**
 * 计算编辑距离
 * 
 * @author xpqiu
 * @version 1.0
 * @since 1.0
 */
public class EditDistance {

	public float calcNormalise(String word, String word2) {
		float distance = calc(word, word2);
		int len = word.length() > word2.length() ? word.length() : word2.length();
		return (len - distance) / len;
	}

	/**
	 * 将x转换到y的编辑距离，可以自定义一些代价
	 * 
	 * @param cSeq1
	 * @param cSeq2
	 * @return 距离
	 */
	public float calc(String cSeq1, String cSeq2) {
		// +1 : 下标为0节点为动态规划的起点
		// cSeq1.length >= cSeq2.length > 1
		int xsLength = cSeq1.length() + 1; // > ysLength
		int ysLength = cSeq2.length() + 1; // > 2

		float[] lastSlice = new float[ysLength];
		float[] currentSlice = new float[ysLength];

		// first slice is just inserts
		currentSlice[0] = 0;
		// y inserts down first column of lattice
		for (int y = 1; y < ysLength; ++y)
			currentSlice[y] = currentSlice[y - 1] + insCost(cSeq2.charAt(y - 1));
		for (int x = 1; x < xsLength; ++x) {
			char cX = cSeq1.charAt(x - 1);
			// exchange between lastSlice and currentSlice
			float[] lastSliceTmp = lastSlice;
			lastSlice = currentSlice;
			currentSlice = lastSliceTmp;
			// x deletes across first row of lattice
			currentSlice[0] = lastSlice[0] + delCost(cSeq1.charAt(x - 1));
			for (int y = 1; y < ysLength; ++y) {
				int yMinus1 = y - 1;
				char cY = cSeq2.charAt(yMinus1);
				// unfold this one step further to put 1 + outside all mins on
				// match
				currentSlice[y] = Math.min(
						cX == cY ? lastSlice[yMinus1] // match
								: substitutionCost(cX, cY) + lastSlice[yMinus1], // 替换代价
						Math.min(delCost(cX) + lastSlice[y], // 删除代价
								insCost(cY) + currentSlice[yMinus1])); // 插入代价
			}
		}
		return currentSlice[currentSlice.length - 1];
	}

	static String noCostChars = "的 最和";
	static String maxCostChars = "不否";

	/**
	 * @param c
	 * @return 插入代价
	 */
	protected static float insCost(char c) {
		if (noCostChars.indexOf(c) != -1)
			return 0;
		if (maxCostChars.indexOf(c) != -1)
			return 5;
		return 1;
	}

	/**
	 * 删除
	 * 
	 * @param c
	 * @return 删除代价
	 */
	protected static float delCost(char c) {
		if (noCostChars.indexOf(c) != -1)
			return 0;
		if (maxCostChars.indexOf(c) != -1)
			return 5;
		return 1;
	}

	static char[][] substitutionCostChars = new char[][] { { 'C', 'G' } };

	/**
	 * x and y are bound to be different
	 * 
	 * @param x
	 * @param y
	 * @return cost
	 */
	protected static float substitutionCost(char x, char y) {
		int cost = 1;
		for (char[] xy : substitutionCostChars) {
			if (xy[0] == x && xy[1] == y) {
				cost = 2;
				break;
			} else if (xy[0] == y && xy[1] == x) {
				cost = 2;
				break;
			}
		}
		return cost;// noCostChars.indexOf(c)!=-1?1:0;
	}

	public float sim(String str1, String str2) {
		float ld = calc(str1, str2);
		return 1 - ld / Math.max(str1.length(), str2.length());
	}
}
