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
            throw new RuntimeException(e);
        }
        return executeMessage;
    }

    public static ExecuteMessage getInteractProcessMessage(Process runProcess, String args) {
        ExecuteMessage executeMessage = new ExecuteMessage();

        try (OutputStream outputStream = runProcess.getOutputStream();
             OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream)) {
            // 从控制台输入参数
            String[] arguments = args.split(" ");
            String join = StringUtils.join(arguments, "\n") + "\n";
            outputStreamWriter.write(join);
            // 回车，发送参数
            outputStreamWriter.flush();

            executeMessage.setMessage(getMessage(runProcess.getInputStream()));

            // 释放资源
            runProcess.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return executeMessage;
    }

    private static String getMessage(InputStream is) throws IOException {
        // 通过进程获取正常输出到控制台的信息
        BufferedReader logReader = new BufferedReader(new InputStreamReader(is));
        List<String> logLineList = new ArrayList<>();
        // 逐行读取
        String logLine;
        while ((logLine = logReader.readLine()) != null) {
            logLineList.add(logLine);
        }

        return StringUtils.join(logLineList, "\n");
    }
}
