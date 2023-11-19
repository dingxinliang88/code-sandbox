package com.juzi.codesandbox.model;

import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 代码沙箱执行代码状态枚举
 *
 * @author codejuzi
 */
@Getter
public enum CodeSandboxStatusEnum {

    SUCCESS("成功", 0),
    FAILED("出错", 1);


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
}
