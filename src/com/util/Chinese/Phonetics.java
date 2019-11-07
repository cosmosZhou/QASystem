package com.util.Chinese;

import java.util.ArrayList;

import java.util.Arrays;
import com.util.Utility;
//丨乙扌亠 冫扌戈纟戋
//http://chaizi.51240.com/
//http://www.264.cn/chaizi/
public class Phonetics {

	static String[] splitConsonantVowel(String phonetics) {
		int index = -1;
		for (int i = 0; i < phonetics.length(); ++i) {
			switch (phonetics.charAt(i)) {
			case 'b':
			case 'p':
			case 't':
			case 'd':
			case 'h':
			case 'r':
			case 'k':
			case 'g':
			case 'f':
			case 'v':
			case 's':
			case 'z':
				index = i;
				break;
			default:
				continue;
			}
			break;
		}
		++index;
		return new String[] { phonetics.substring(0, index), phonetics.substring(index) };
	}

	double acoustic_Similarity() {
		return 0;
	}

	static void process(String PhoneticLexicon) throws Exception {
		Utility.Text reader = new Utility.Text(PhoneticLexicon);
		String content = reader.fetchContent();
		System.out.println(content);
		ArrayList<String> list = new ArrayList<String>();
		for (String[] str : Utility.regex(content, "(\\S)=([a-z]+)\r\n")) {
			System.out.print(str[0] + " is pronounced as " + str[1]);
			System.out.println(" after spliting Consonant and Vowel = " + Arrays.toString(splitConsonantVowel(str[1])));
			list.add(str[0] + "=" + Arrays.toString(splitConsonantVowel(str[1])));
		}
		Utility.writeString(PhoneticLexicon, list);
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		process("../models/" + "PhoneticLexicon.txt");
	}

}
