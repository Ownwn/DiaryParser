package com.example.diaryparser;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// map of headings, and all lines of text under heading
public record Note(String date, Map<String, List<String>> entries) {
    public String totalContent() {
        return entries().values().stream()
                .map(e -> String.join(" ", e))
                .collect(Collectors.joining(" "));
    }
}