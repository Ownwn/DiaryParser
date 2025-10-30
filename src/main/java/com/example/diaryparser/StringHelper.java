package com.example.diaryparser;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StringHelper {

    public static boolean checkSimilar(String a, String b) {
        if (Objects.equals(a, b)) return true;
        if (a.isBlank() || b.isBlank()) return false;

        if (a.startsWith(b) || b.startsWith(a)) return true;

        String lon; String sho;

        if (a.length() >= b.length()) {
            lon = a;
            sho = b;
        } else {
            lon = b;
            sho = a;
        }

        var lonMap = charMap(lon);
        var shoMap = charMap(sho);

        int matches = 0;
        for (var entry : lonMap.entrySet()) {
            if (shoMap.getOrDefault(entry.getKey(), -1).equals(entry.getValue())) { // thx boxing
                matches++;
            }
        }

        return matches >= lon.length()/2;



//        return "a".equals(("a" + "aa" + Math.random()).intern());
    }

    private static Map<Character, Integer> charMap(String s) {
        return s.chars().mapToObj(i -> (char) i).collect(Collectors.groupingBy(Function.identity(), Collectors.summingInt(_ -> 1)));
    }


    public static void main(String[] args) {
        var f = "hello";
        var s = "bobby";
        System.out.println(checkSimilar(f, s));
    }
}
