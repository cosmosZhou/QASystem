package com.robot.semantic;

import com.robot.util.EditDistance;
import com.robot.util.ISimilarity;

/**
 * @author xpqiu
 * @version 1.0
 * @since 1.0
 */
public class EditDistanceLevenshtein extends EditDistance implements ISimilarity<String> {
	/**
	 * the length of the lexeme to be compared
	 */
	static final private int WORDLEN = 2;

	public EditDistanceLevenshtein() {
		//
		// String dataFile =
		// "\\\\10.11.7.3\\f$\\对于共享版《同义词词林》的改进\\improvedThesaurus.data";
		// synSet = (HashSet<String>) Thesaurus.buildSynonymSet(dataFile);
	}

	/**
	 * 将x转换到y的编辑距离，可以自定义一些代价
	 */
	public float calc(String str1, String str2) {
		int n = str1.length();
		int m = str2.length();

		if (n == 0) {
			return m;
		}
		if (m == 0) {
			return n;
		}

		// lattice of n + 1 rows and m + 1 columns
		float d[][] = new float[n + 1][m + 1];
		// initialize the 0th column;
		for (int i = 0; i <= n; i++) {
			d[i][0] = i;
		}
		// initialize the 0th row;;
		for (int j = 0; j <= m; j++) {
			d[0][j] = j;
		}
		// iterate over str1;
		for (int i = 1; i <= n; i++) {
			char cX = str1.charAt(i - 1);
			// 去匹配str2
			for (int j = 1; j <= m; j++) {

				// evaluate substitution cost in accordance with the synonymous
				// lexicon;
				for (int ii = 1; ii <= WORDLEN; ii++) {
					if (ii + i - 1 > str1.length())
						break;
					for (int jj = 1; jj <= WORDLEN; jj++) {
						if (jj + j - 1 > str2.length())
							break;
						// compare a lexeme of length ii with another lexeme of
						// length jj;
						float distance = (float) (1 - Thesaurus.instance.similarity(str1.substring(i - 1, ii + i - 1), str2.substring(j - 1, jj + j - 1)));
						if (distance < 0.3) {

							// float dist = d[i - 1][j - 1] + 0.1f;
							float dist = d[i - 1][j - 1] + distance;

							if (d[i + ii - 1][j + jj - 1] > 0)
								d[i + ii - 1][j + jj - 1] = Math.min(d[i + ii - 1][j + jj - 1], dist);
							else
								d[i + ii - 1][j + jj - 1] = dist;
						}

					}
				}

				char cY = str2.charAt(j - 1);
				float temp = (cX == cY ? d[i - 1][j - 1] // match
						: substitutionCost(cX, cY) + d[i - 1][j - 1]);
				if (d[i][j] > 0) {
					temp = Math.min(temp, d[i][j]);
				}
				// cost of substitution
				d[i][j] = Math.min(temp,
						// cost of deletion
						Math.min(delCost(cX) + d[i - 1][j],
								// cost of insertion
								insCost(cY) + d[i][j - 1]));
			}
		}
		return d[n][m];

	}

	public static EditDistanceLevenshtein instance;
	static {
		instance = new EditDistanceLevenshtein();
	}

	public static void main(String[] args) {

		//		String str1 = "发行时间";
		String str1 = "生日";

		String str2 = "出生日期";
		System.out.println("ld=" + instance.calc(str1, str2));
		System.out.println("sim=" + instance.sim(str1, str2));
	}

}
