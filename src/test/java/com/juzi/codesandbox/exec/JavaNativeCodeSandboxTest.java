package com.juzi.codesandbox.exec;


import com.juzi.codesandbox.model.ExecuteCodeRequest;
import com.juzi.codesandbox.model.ExecuteCodeResponse;

import java.util.List;

/**
 * @author codejuzi
 */
public class JavaNativeCodeSandboxTest {
    public static void main(String[] args) {
        nonInteractCode();
//        interactCode();
    }

    static void nonInteractCode() {
        ExecuteCodeRequest request = ExecuteCodeRequest.builder()
                .code("public class Main {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        int a = Integer.parseInt(args[0]);\n" +
                        "        int b = Integer.parseInt(args[1]);\n" +
                        "        System.out.println(a + b);\n" +
                        "    }\n" +
                        "}")
                .inputList(List.of("2 3", "3 4"))
                .build();

        JavaNativeCodeSandbox javaNativeCodeSandbox = new JavaNativeCodeSandbox();
        ExecuteCodeResponse response = javaNativeCodeSandbox.execute(request);
        System.out.println("response = " + response);
    }

    static void interactCode() {
        ExecuteCodeRequest request = ExecuteCodeRequest.builder()
                .code("import java.util.*;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        Scanner cin = new Scanner(System.in);\n" +
                        "        int a = cin.nextInt(), b = cin.nextInt();\n" +
                        "        System.out.println(\"结果：\" + (a + b));\n" +
                        "    }\n" +
                        "}")
                .inputList(List.of("2 3", "3 4"))
                .build();
        JavaNativeCodeSandbox javaNativeCodeSandbox = new JavaNativeCodeSandbox();
        javaNativeCodeSandbox.execute(request);
    }
}