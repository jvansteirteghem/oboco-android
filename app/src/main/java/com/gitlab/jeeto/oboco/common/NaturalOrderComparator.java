package com.gitlab.jeeto.oboco.common;

import java.util.Comparator;

/**
 * Probably the best natural strings comparator.
 */
public class NaturalOrderComparator<T> implements Comparator<T> {

    protected final boolean ignoreCase;
    protected final boolean skipSpaces;

    public NaturalOrderComparator() {
        this(false, true);
    }

    public NaturalOrderComparator(final boolean ignoreCase, final boolean skipSpaces) {
        this.ignoreCase = ignoreCase;
        this.skipSpaces = skipSpaces;
    }

    /**
     * Compare digits at certain position in two strings.
     * The longest run of digits wins. That aside, the greatest
     * value wins.
     * @return if numbers are different, only 1 element is returned.
     */
    protected int[] compareDigits(final String str1, int ndx1, final String str2, int ndx2) {
        // iterate all digits in the first string

        int zeroCount1 = 0;
        while (charAt(str1, ndx1) == '0') {
            zeroCount1++;
            ndx1++;
        }

        int len1 = 0;
        while (true) {
            final char char1 = charAt(str1, ndx1);
            final boolean isDigitChar1 = isDigit(char1);
            if (!isDigitChar1) {
                break;
            }
            len1++;
            ndx1++;
        }

        // iterate all digits in the second string and compare with the first

        int zeroCount2 = 0;
        while (charAt(str2, ndx2) == '0') {
            zeroCount2++;
            ndx2++;
        }

        int len2 = 0;

        int ndx1_new = ndx1 - len1;
        int equalNumbers = 0;

        while (true) {
            final char char2 = charAt(str2, ndx2);
            final boolean isDigitChar2 = isDigit(char2);
            if (!isDigitChar2) {
                break;
            }
            if (equalNumbers == 0 && (ndx1_new < ndx1)) {
                equalNumbers = charAt(str1, ndx1_new++) - char2;
            }
            len2++;
            ndx2++;
        }

        // compare

        if (len1 != len2) {
            // numbers are not equals size
            return new int[] {len1 - len2};
        }

        if (equalNumbers != 0) {
            return new int[] {equalNumbers};
        }

        // numbers are equal, but number of zeros is different
        return new int[] {0, zeroCount1 - zeroCount2, ndx1, ndx2};
    }

    @Override
    public int compare(final T o1, final T o2) {
        String str1 = toString(o1);
        String str2 = toString(o2);

        int ndx1 = 0, ndx2 = 0;
        char char1, char2;
        int lastZeroDifference = 0;

        while (true) {
            char1 = charAt(str1, ndx1);
            char2 = charAt(str2, ndx2);

            // skip over spaces in both strings
            if (skipSpaces) {
                while (isSpaceChar(char1)) {
                    ndx1++;
                    char1 = charAt(str1, ndx1);
                }

                while (isSpaceChar(char2)) {
                    ndx2++;
                    char2 = charAt(str2, ndx2);
                }
            }

            // check for numbers

            final boolean isDigitChar1 = isDigit(char1);
            final boolean isDigitChar2 = isDigit(char2);

            if (isDigitChar1 && isDigitChar2) {
                // numbers detected!

                final int[] result = compareDigits(str1, ndx1, str2, ndx2);

                if (result[0] != 0) {
                    // not equals, return
                    return result[0];
                }

                // equals, save zero difference if not already saved
                if (lastZeroDifference == 0) {
                    lastZeroDifference = result[1];
                }

                ndx1 = result[2];
                ndx2 = result[3];
                continue;
            }

            if (char1 == 0 && char2 == 0) {
                // both strings end; the strings are the same
                return lastZeroDifference;
            }

            // compare chars
            if (ignoreCase) {
                char1 = Character.toLowerCase(char1);
                char2 = Character.toLowerCase(char2);
            }

            if (char1 < char2) {
                return -1;
            }
            if (char1 > char2) {
                return 1;
            }

            ndx1++;
            ndx2++;
        }
    }

    private static boolean isDigit(final char c) {
        return Character.isDigit(c);
    }

    private static boolean isSpaceChar(final char c) {
        return Character.isSpaceChar(c) || c == '_';
    }

    /**
     * Safe {@code charAt} that returns 0 when ndx is out of boundaries.
     */
    private static char charAt(final String string, final int ndx) {
        if (ndx >= string.length()) {
            return 0;
        }
        return string.charAt(ndx);
    }

    public String toString(T o) {
        return o.toString();
    }
}