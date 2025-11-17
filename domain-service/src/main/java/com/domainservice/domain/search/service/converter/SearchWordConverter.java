package com.domainservice.domain.search.service.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.HashMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SearchWordConverter {

	private static final char[] INITIAL_CONSONANT = {
		'ㄱ','ㄲ','ㄴ','ㄷ','ㄸ','ㄹ','ㅁ','ㅂ','ㅃ','ㅅ','ㅆ',
		'ㅇ','ㅈ','ㅉ','ㅊ','ㅋ','ㅌ','ㅍ','ㅎ'
	};

	private static final char[] INITIAL_CONSONANT_QWERTY = {
		'r','R','s','e','E','f','a','q','Q','t','T','d','w','W','c','z','x','v','g'
	};

	private static final String[] MEDIAL_CONSONANT = {
		"ㅏ","ㅐ","ㅑ","ㅒ","ㅓ","ㅔ","ㅕ","ㅖ","ㅗ","ㅘ","ㅙ","ㅚ",
		"ㅛ","ㅜ","ㅝ","ㅞ","ㅟ","ㅠ","ㅡ","ㅢ","ㅣ"
	};

	private static final String[] MEDIAL_CONSONANT_QWERTY = {
		"k","o","i","O","j","p","u","P","h","hk","ho","hl",
		"y","n","nj","np","nl","b","m","ml","l"
	};

	private static final String[] FINAL_CONSONANT = {
		"", "ㄱ", "ㄲ", "ㄳ", "ㄴ", "ㄵ", "ㄶ", "ㄷ", "ㄹ",
		"ㄺ", "ㄻ", "ㄼ", "ㄽ", "ㄾ", "ㄿ", "ㅀ",
		"ㅁ", "ㅂ", "ㅄ", "ㅅ", "ㅆ", "ㅇ",
		"ㅈ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"
	};

	private static final String[] FINAL_CONSONANT_QWERTY = {
		"", "r", "R", "rt", "s", "sw", "sg", "e", "f",
		"fr", "fa", "fq", "ft", "fx", "fv", "fg", "a",
		"q", "qt", "t", "T", "d", "w", "W", "c", "z",
		"x", "v", "g"
	};

	private static final Map<Character, Integer> CHO_MAP = new HashMap<>();
	private static final Map<String, Integer> JUNG_MAP = new HashMap<>();
	private static final Map<String, Integer> JONG_MAP = new HashMap<>();

	static {
		for (int i = 0; i < INITIAL_CONSONANT_QWERTY.length; i++) {
			CHO_MAP.put(INITIAL_CONSONANT_QWERTY[i], i);
		}
		for (int i = 0; i < MEDIAL_CONSONANT_QWERTY.length; i++) {
			JUNG_MAP.put(MEDIAL_CONSONANT_QWERTY[i], i);
		}
		for (int i = 0; i < FINAL_CONSONANT_QWERTY.length; i++) {
			JONG_MAP.put(FINAL_CONSONANT_QWERTY[i], i);
		}
	}

	public static String convertToQwertyInput(String text) {
		StringBuilder result = new StringBuilder();

		for (char ch : text.toCharArray()) {
			if (ch >= 0xAC00 && ch <= 0xD7A3) {
				int unicode = ch - 0xAC00;
				int cho = unicode / (21 * 28);
				int jung = (unicode % (21 * 28)) / 28;
				int jong = unicode % 28;

				result.append(INITIAL_CONSONANT_QWERTY[cho]);
				result.append(MEDIAL_CONSONANT_QWERTY[jung]);
				result.append(FINAL_CONSONANT_QWERTY[jong]);
			} else {
				result.append(ch);
			}
		}
		return result.toString();
	}

	public static String convertToKoreanWord(String input) {
		StringBuilder result = new StringBuilder();
		int i = 0;

		while (i < input.length()) {
			int cho = -1, jung = -1, jong = 0;

			// 초성
			char ch = input.charAt(i);
			Integer choIdx = CHO_MAP.get(ch);
			if (choIdx != null) {
				cho = choIdx;
				i++;
			} else {
				result.append(ch);
				i++;
				continue;
			}

			// 중성
			for (String key : JUNG_MAP.keySet()) {
				if (input.startsWith(key, i)) {
					jung = JUNG_MAP.get(key);
					i += key.length();
					break;
				}
			}
			if (jung == -1) {
				result.append(INITIAL_CONSONANT[cho]);
				continue;
			}

			// 종성
			for (int len = 2; len >= 1; len--) {
				if (i + len <= input.length()) {
					String jongCandidate = input.substring(i, i + len);
					if (JONG_MAP.containsKey(jongCandidate)) {
						jong = JONG_MAP.get(jongCandidate);
						i += len;
						break;
					}
				}
			}

			// 음절 조합
			char syllable = (char) (0xAC00 + (cho * 21 * 28) + (jung * 28) + jong);
			result.append(syllable);
		}
		return result.toString();
	}

}
