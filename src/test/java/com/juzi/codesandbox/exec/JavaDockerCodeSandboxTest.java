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
public class JavaDockerCodeSandboxTest {

    @Resource
    private JavaDockerCodeSandbox javaDockerCodeSandbox;

    @Test
    public void nonInteractCode() {
        ExecuteCodeRequest request = ExecuteCodeRequest.builder()
                .code("public class Main {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        int a = Integer.parseInt(args[0]);\n" +
                        "        int b = Integer.parseInt(args[1]);\n" +
                        "        System.out.println(a + b);\n" +
                        "    }\n" +
                        "}")
                .inputList(Arrays.asList("2 3", "3 4"))
                .build();

        ExecuteCodeResponse response = javaDockerCodeSandbox.execute(request);
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
                .inputList(Arrays.asList("2 3", "3 4"))
                .build();
        JavaDockerCodeSandbox codeSandbox = new JavaDockerCodeSandbox();
        ExecuteCodeResponse response = codeSandbox.execute(request);
        System.out.println("response = " + response);
    }
}