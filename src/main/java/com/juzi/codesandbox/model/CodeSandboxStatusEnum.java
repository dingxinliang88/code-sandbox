package com.juzi.codesandbox.model;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 题目提交枚举
 *
 * @author codejuzi
 */
public enum CodeSandboxStatusEnum {

    // 0 - 待判题、1 - 判题中、2 - AC、3 - Failed
    SUCCEED("成功", 0),
    FAILED("出错", 1),
    ;


    private final String text;

    private final Integer value;

    CodeSandboxStatusEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    public static List<Integer> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    public static CodeSandboxStatusEnum getEnumByValue(Integer value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (CodeSandboxStatusEnum anEnum : CodeSandboxStatusEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public Integer getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
