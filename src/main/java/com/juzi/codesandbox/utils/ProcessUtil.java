package com.juzi.codesandbox.utils;

import com.juzi.codesandbox.model.ExecuteMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.StopWatch;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 程序进程执行工具类
 *
 * @author codejuzi
 */
@Slf4j
public class ProcessUtil {

    /**
     * 获取进程执行信息
     *
     * @param processType 进程类型
     * @param runProcess  进程
     * @return 执行信息
     */
    public static ExecuteMessage getRunProcessMessage(String processType, Process runProcess) {
        ExecuteMessage executeMessage = new ExecuteMessage();
        StopWatch stopWatch = new StopWatch();

        try {
            stopWatch.start();
            // 等待Process执行结束，得到退出状态码
            int exitValue = runProcess.waitFor();
            executeMessage.setExitValue(exitValue);

            // 正常编译，退出
            if (exitValue == 0) {
                log.info("{} Success!", processType);
            }
            // 出现异常
            else {
                log.error("{} Failed! ExitValue: {}", processType, exitValue);
                executeMessage.setErrorMessage(getMessage(runProcess.getErrorStream()));
            }
            executeMessage.setMessage(getMessage(runProcess.getInputStream()));
            stopWatch.stop();
            executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
        } catch (IOException | InterruptedException e) {
            log.error(processType + "failed：", e);
        }
        return executeMessage;
    }

    /**
     * 执行交互式进程并获取信息
     *
     * @param runProcess 进程
     * @param input      输入
     * @return 执行信息
     */
    public static ExecuteMessage getInteractProcessMessage(Process runProcess, String input) throws IOException {
        ExecuteMessage executeMessage = new ExecuteMessage();

        StringReader inputReader = new StringReader(input);
        BufferedReader inputBufferedReader = new BufferedReader(inputReader);

        //计时
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        //输入（模拟控制台输入）
        PrintWriter consoleInput = new PrintWriter(runProcess.getOutputStream());
        String line;
        while ((line = inputBufferedReader.readLine()) != null) {
            consoleInput.println(line);
            consoleInput.flush();
        }
        consoleInput.close();

        //获取输出
        BufferedReader userCodeOutput = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
        List<String> outputList = new ArrayList<>();
        String outputLine;
        while ((outputLine = userCodeOutput.readLine()) != null) {
            outputList.add(outputLine);
        }
        userCodeOutput.close();

        //获取错误输出
        BufferedReader errorOutput = new BufferedReader(new InputStreamReader(runProcess.getErrorStream()));
        List<String> errorList = new ArrayList<>();
        String errorLine;
        while ((errorLine = errorOutput.readLine()) != null) {
            errorList.add(errorLine);
        }
        errorOutput.close();

        stopWatch.stop();
        executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
        executeMessage.setMessage(StringUtils.join(outputList, "\n"));
        executeMessage.setErrorMessage(StringUtils.join(errorList, "\n"));
        runProcess.destroy();

        return executeMessage;
    }

    /**
     * 获取流is的输出
     *
     * @param is 输出流
     * @return 输出信息
     */
    private static String getMessage(InputStream is) throws IOException {
        // 通过进程获取正常输出到控制台的信息
        BufferedReader logReader = new BufferedReader(new InputStreamReader(is));
        List<String> logLineList = new ArrayList<>();
        // 逐行读取
        String logLine;
        while ((logLine = logReader.readLine()) != null) {
            logLineList.add(logLine);
        }
        logReader.close();
        return StringUtils.join(logLineList, "\n");
    }
}
