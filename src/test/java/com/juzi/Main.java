package com.juzi;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author codejuzi
 */
public class Main {
    public static void main(String[] args) {
        // /Users/codejuzi/Documents/CodeWorkSpace/Project/JuOj/code-sandbox
        String projectPath = System.getProperty("user.dir");
        // /Users/codejuzi/Documents/CodeWorkSpace/Project/JuOj/code-sandbox/src/main/resources/data/sensitive-word.txt
        String sensitiveWordsFilePath = projectPath + File.separator + "src/main/resources/data/sensitive-word.txt";
        List<String> words = FileUtil.readLines(sensitiveWordsFilePath, StandardCharsets.UTF_8);
        System.out.println(words);
    }
}
