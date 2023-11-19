package com.juzi.codesandbox.exec;


import com.juzi.codesandbox.model.ExecuteCodeRequest;
import com.juzi.codesandbox.model.ExecuteCodeResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Arrays;

/**
 * @author codejuzi
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class JavaNativeCodeSandboxTest {

    @Resource
    JavaNativeArgsCodeSandbox javaNativeArgsCodeSandbox;

    @Resource
    JavaNativeAcmCodeSandbox javaNativeAcmCodeSandbox;

    @Test
    public void nonInteractCode() {
        ExecuteCodeRequest request = ExecuteCodeRequest.builder()
                .code("public class Main {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        int exec = Integer.parseInt(args[0]);\n" +
                        "        int b = Integer.parseInt(args[1]);\n" +
                        "        System.out.println(exec + b);\n" +
                        "    }\n" +
                        "}")
                .inputList(Arrays.asList("2 3", "3 4"))
                .build();

        ExecuteCodeResponse response = javaNativeArgsCodeSandbox.execute(request);
        System.out.println("response = " + response);
    }

    @Test
    public void interactCode() {
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
                .inputList(Arrays.asList("2 3", "3 4"))
                .build();
        ExecuteCodeResponse response = javaNativeAcmCodeSandbox.execute(request);
        System.out.println("response = " + response);
    }
}