package com.domainservice.domain.search.service.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PrefixConverter {

	private static final char[] INITIAL_CONSONANT = {
		'ㄱ','ㄲ','ㄴ','ㄷ','ㄸ','ㄹ','ㅁ','ㅂ','ㅃ','ㅅ','ㅆ',
		'ㅇ','ㅈ','ㅉ','ㅊ','ㅋ','ㅌ','ㅍ','ㅎ'
	};

	private static final char[] INITIAL_CONSONANT_QWERTY = {
		'r','R','s','e','E','f','a','q','Q','t','T','d','w','W','c','z','x','v','g'
	};

	private static final String[] MEDIAL_CONSONANT_QWERTY = {
		"k","o","i","O","j","p","u","P","h","hk","ho","hl",
		"y","n","nj","np","nl","b","m","ml","l"
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

	// 길이 순으로 탐색하기 위한 키 배열
	private static final String[] JUNG_KEYS;
	private static final String[] JONG_KEYS;

	static {
		// 초성 매핑
		for (int i = 0; i < INITIAL_CONSONANT_QWERTY.length; i++) {
			CHO_MAP.put(INITIAL_CONSONANT_QWERTY[i], i);
		}

		// 중성 매핑
		for (int i = 0; i < MEDIAL_CONSONANT_QWERTY.length; i++) {
			JUNG_MAP.put(MEDIAL_CONSONANT_QWERTY[i], i);
		}

		// 종성 매핑
		for (int i = 0; i < FINAL_CONSONANT_QWERTY.length; i++) {
			JONG_MAP.put(FINAL_CONSONANT_QWERTY[i], i);
		}

		// 중성 키: 긴 문자열(두 글자) 우선 탐색
		JUNG_KEYS = JUNG_MAP.keySet().toArray(new String[0]);
		Arrays.sort(JUNG_KEYS, (a, b) -> Integer.compare(b.length(), a.length()));

		// 종성 키: ""(빈 문자열)은 제외하고 긴 문자열 우선
		JONG_KEYS = JONG_MAP.keySet()
			.stream()
			.filter(k -> !k.isEmpty())
			.sorted((a, b) -> Integer.compare(b.length(), a.length()))
			.toArray(String[]::new);
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
			// 1. 초성
			char ch = input.charAt(i);
			Integer choIdx = CHO_MAP.get(ch);

			if (choIdx == null) { // 한글 두벌식 키가 아니면 그대로 출력
				result.append(ch);
				i++;
				continue;
			}

			int cho = choIdx;
			i++;

			// 2. 중성 (긴 키부터 시도: hk, ho, hl, nj, np, nl, ml ...)
			int jung = -1;
			for (String key : JUNG_KEYS) {
				if (input.startsWith(key, i)) {
					jung = JUNG_MAP.get(key);
					i += key.length();
					break;
				}
			}

			if (jung == -1) {
				// 모음을 못 찾으면 초성만 단독 출력
				result.append(INITIAL_CONSONANT[cho]);
				continue;
			}

			// 3. 종성 (뒤에 모음이 오면 초성으로 넘기기 위해 lookahead 사용)
			int jong = 0;
			for (String key : JONG_KEYS) {
				if (!input.startsWith(key, i)) {
					continue;
				}

				int nextIndex = i + key.length();

				// lookahead: 종성 후보 뒤에 모음이 오면, 이 자음은 다음 음절의 초성으로 쓰는 게 자연스러움
				boolean hasNextVowel = false;
				if (nextIndex < input.length()) {
					for (String jungKey : JUNG_KEYS) {
						if (input.startsWith(jungKey, nextIndex)) {
							hasNextVowel = true;
							break;
						}
					}
				}

				if (hasNextVowel) {
					// 이 종성 후보는 사용하지 않고, 다음 음절의 초성으로 넘긴다
					continue;
				}

				// 종성 확정
				jong = JONG_MAP.get(key);
				i = nextIndex;
				break;
			}

			// 4. 음절 조합
			char syllable = (char) (0xAC00 + (cho * 21 * 28) + (jung * 28) + jong);
			result.append(syllable);
		}

		return result.toString();
	}
}
