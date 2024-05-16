package UtilitiesToolLib.common.util;

import org.apache.commons.lang3.StringUtils;

import com.ibm.icu.text.Transliterator;

public class JapaneseCharacterUtils {

  private static final Transliterator TRANS_KATA_HIRA = Transliterator.getInstance("Hiragana-Katakana");

  /**
   * Determines if string contains Japanese character.
   * 
   * @param str String
   * @return boolean
   */
  public static boolean hasContainsJapaneseCharacter(String str) {
    boolean result = false;
    for (char c : str.toCharArray()) {
      if (isKana(c) || isKanji(c)) {
        result = true;
        break;
      }
    }
    return result;
  }

  /**
   * Determines if this character is a Japanese Kana.
   * 
   * @param character char
   * @return boolean
   */
  public static boolean isKana(char character) {
    return (isHiragana(character) || isKatakana(character));
  }

  /**
   * Determines if this character is one of the Japanese Hiragana.
   * 
   * @param character char
   * @return boolean
   */
  public static boolean isHiragana(char character) {
    return (('ぁ' <= character) && (character <= 'ゞ'));
  }

  /**
   * Determines if this character is one of the Japanese Katakana.
   * 
   * @param character char
   * @return boolean
   */
  public static boolean isKatakana(char character) {
    return (isHalfWidthKatakana(character) || isFullWidthKatakana(character));
  }

  /**
   * Determines if this character is a Half width Katakana.
   * 
   * @param character char
   * @return boolean
   */
  public static boolean isHalfWidthKatakana(char character) {
    return (('ｦ' <= character) && (character <= 'ﾝ'));
  }

  /**
   * Determines if this character is a Full width Katakana.
   * 
   * @param character char
   * @return boolean
   */
  public static boolean isFullWidthKatakana(char character) {
    return (('ァ' <= character) && (character <= 'ヾ'));
  }

  /**
   * Determines if this character is a Kanji character.
   * 
   * @param character char
   * @return boolean
   */
  public static boolean isKanji(char character) {
    if (('一' <= character) && (character <= '龥')) {
      return true;
    }
    if (('々' <= character) && (character <= '〇')) {
      return true;
    }
    return false;
  }

  /**
   * Analyzer transform text to katakana
   *
   * @param str String
   * @return String
   */
  public static String convertHiraganaToKatakana(String str) {
    if (StringUtils.isEmpty(str)) {
      return str;
    }
    return TRANS_KATA_HIRA.transform(str);
  }
}
