package com.example.diaryparser;

import java.util.List;
import java.util.Map;

// map of headings, and all lines of text under heading
public record Note(Map<String, List<String>> entries) {
}