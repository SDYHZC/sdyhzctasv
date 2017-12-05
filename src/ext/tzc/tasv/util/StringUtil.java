package ext.tzc.tasv.util;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import wt.util.WTRuntimeException;

public class StringUtil
{
  public static final int QUESTION_MARK_ASCII = 63;
  private static final String DECIMAL_FORMAT = "0.000";

  public static boolean isEmpty(String str)
  {
    return StringUtils.isEmpty(str);
  }

  public static boolean isNotEmpty(String str)
  {
    return StringUtils.isNotEmpty(str);
  }

  public static boolean isNotEmpty(String str, boolean isTrim) {
    if (isTrim) {
      return (StringUtils.isNotEmpty(str)) && (StringUtils.isNotEmpty(str.trim()));
    }
    return StringUtils.isNotEmpty(str);
  }

  public static boolean isEmpty(String str, boolean isTrim) {
    if (isTrim) {
      return (StringUtils.isEmpty(str)) || (StringUtils.isEmpty(str.trim()));
    }
    return StringUtils.isEmpty(str);
  }

  public static List<String> getListFromTokens(String src, String split)
  {
    List tokenList = null;
    if ((isNotEmpty(src)) && (isNotEmpty(split))) {
      String[] strArray = StringUtils.split(src, split);
      tokenList = Arrays.asList(strArray);
    }
    return tokenList;
  }

  public static String replace(String strSource, String strFrom, String strTo)
  {
    return StringUtils.replace(strSource, strFrom, strTo);
  }

  public static int getIndex(String[] array, String str)
  {
    int index = -1;
    if ((array != null) && (str != null)) {
      int size = array.length;

      for (int i = 0; i < size; i++) {
        String temp = array[i];
        if (str.equals(temp)) {
          index = i;
          break;
        }
      }
    }
    return index;
  }

  public static String filterInvalidChar(String str)
  {
    String newStr = null;
    char[] newArray = null;
    if (!isEmpty(str)) {
      int length = str.length();
      newArray = new char[length];
      int codePointCount = str.codePointCount(0, length);

      int i = 0; for (int j = 0; i < codePointCount; j++) {
        int codePoint = str.codePointAt(i);
        if (Character.isSupplementaryCodePoint(codePoint))
          i += 2;
        else {
          i++;
        }
        newArray[j] = (char)codePoint;
      }
      newStr = new String(newArray);
    }

    return newStr;
  }

  public static int compareVersion(String str1, String str2)
  {
    int leg1 = str1.length();
    int leg2 = str2.length();
    if (leg1 > leg2)
      return 1;
    if (leg1 == leg2) {
      int asc1 = 0;
      int asc2 = 0;
      for (int i = 0; i < leg1; i++) {
        asc1 = str1.charAt(i);
        asc2 = str2.charAt(i);
        if (asc1 > asc2)
          return 1;
        if (asc1 != asc2)
        {
          return -1;
        }
      }
    } else {
      return -1;
    }

    return 0;
  }

  public static int compareFullVersion(String str1, String str2)
  {
    String reg = "[,.\\s++]";
    String[] version1 = str1.split(reg);
    String[] version2 = str2.split(reg);
    if (compareVersion(version1[0], version2[0]) > 0)
      return 1;
    if (compareVersion(version1[0], version2[0]) == 0) {
      if (compareVersion(version1[1], version2[1]) > 0)
        return 1;
      if (compareVersion(version1[1], version2[1]) == 0) {
        if ((version1.length > 2) || (version2.length > 2)) {
          if ((version1.length > 2) && (version2.length > 2))
            return compareVersion(version1[2], version2[2]);
          if (version1.length > 2) {
            return 1;
          }
          return -1;
        }

        return 0;
      }

      return -1;
    }

    return -1;
  }

  public static String removeInternalBlank(String s)
  {
    Pattern p = Pattern.compile("\\s*|\t|\r|\n");
    Matcher m = p.matcher(s);
    char[] str = s.toCharArray();
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < str.length; i++) {
      if (str[i] != ' ') break;
      sb.append(' ');
    }

    String after = m.replaceAll("");
    StringUtils.remove(s, ' ');
    return sb.toString() + after;
  }

  public static boolean isNumeric(String str)
  {
    return StringUtils.isNumeric(str);
  }

  public static String trimAllWhitespace(String str)
  {
    return StringUtils.remove(str, ' ');
  }

  public static boolean isNumer(String value) {
    if (isNotEmpty(value, true))
    {
      return value.matches("^[0-9]+\\.{0,1}[0-9]{0,6}$");
    }
    throw new WTRuntimeException(value.toString());
  }
}