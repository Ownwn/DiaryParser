package com.example.diaryparser;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StringHelper {

    public static boolean fuzzy(String needle, String haystack) {
        if (Objects.equals(needle, haystack)) return true;
        needle = needle.toLowerCase();
        haystack = haystack.toLowerCase();

        if (needle.length() > haystack.length()) return false;

        if (needle.isEmpty()) return true;
        if (haystack.isBlank()) return false;

        return true; // todo

//        for (int i = 0; i < needle.length(); i++) {
//            var compareS = needle.substring(0, i) + needle.substring(i+1);
//            if (haystack.contains(compareS)) return true;
//        }
//        return false;
    }

    private static Map<Character, Integer> charMap(String s) {
        return s.chars().mapToObj(i -> (char) i).collect(Collectors.groupingBy(Function.identity(), Collectors.summingInt(_ -> 1)));
    }


    public static void main(String[] args) {
        var f = "christchurch";
        var s = "Christchurch";
        System.out.println(fuzzy(f, s));
    }
}
