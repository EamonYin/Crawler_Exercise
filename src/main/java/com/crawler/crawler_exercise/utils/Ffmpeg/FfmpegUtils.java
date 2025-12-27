package com.crawler.crawler_exercise.utils.Ffmpeg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FfmpegUtils {

    /**
     * 执行一次 ffmpeg（或任意外部命令），并对其生命周期进行完整托管。
     *
     * <p>该方法会：
     * <ul>
     *   <li>启动一个外部进程（通常是 ffmpeg）</li>
     *   <li>同步读取进程的标准输出/错误输出（防止缓冲区阻塞）</li>
     *   <li>在指定超时时间内等待进程结束</li>
     *   <li>校验进程退出码，非 0 视为失败</li>
     * </ul>
     *
     * <p><b>注意：</b>
     * <ul>
     *   <li>此方法为阻塞方法，会一直阻塞到进程结束或超时</li>
     *   <li>command 中的第一个元素必须是可执行程序路径（如 ffmpeg）</li>
     *   <li>所有命令参数必须为 String，不能包含 Path 等其他类型</li>
     * </ul>
     *
     * @param command   外部进程命令及参数列表
     *                  <p>示例：
     *                  <pre>
     *                  List.of(
     *                      "/usr/bin/ffmpeg",
     *                      "-y",
     *                      "-i", "/tmp/input.mp3",
     *                      "-ac", "1",
     *                      "/tmp/output.wav"
     *                  )
     *                  </pre>
     *
     * @param timeoutMs 允许外部进程执行的最大时间（毫秒）
     *                  <p>超过该时间进程仍未结束，将被强制终止
     *
     * @throws IOException
     *         <ul>
     *           <li>外部进程启动失败（如命令不存在、无执行权限）</li>
     *           <li>进程正常结束但退出码非 0（ffmpeg 执行失败）</li>
     *         </ul>
     *
     * @throws InterruptedException
     *         当前线程在等待进程结束时被中断
     *         <p>调用方应自行决定是否恢复中断状态
     *
     * @throws TimeoutException
     *         外部进程在指定时间内未完成执行，被强制终止
     */
    public static void runFfmpeg(List<String> command, long timeoutMs)
            throws IOException, InterruptedException, TimeoutException {

        // 使用 ProcessBuilder 创建外部进程
        // ProcessBuilder 不关心具体执行什么程序，只会执行 command.get(0)
        ProcessBuilder pb = new ProcessBuilder(command);

        // 将标准错误流（stderr）合并到标准输出流（stdout）
        // ffmpeg 默认大量输出到 stderr，不合并会导致缓冲区阻塞
        pb.redirectErrorStream(true);

        // 启动外部进程（真正创建 OS 级子进程）
        Process p = pb.start();

        // 持续读取子进程输出，直到流关闭
        // 必须读取，否则子进程可能因输出缓冲区满而阻塞
        try (BufferedReader r = new BufferedReader(
                new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = r.readLine()) != null) {
                // 可以在这里记录 ffmpeg 日志
                // log.debug("[ffmpeg] {}", line);
            }
        }

        // 在指定超时时间内等待进程结束
        boolean finished = p.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
        if (!finished) {
            // 超时仍未结束，强制杀死进程
            p.destroyForcibly();
            throw new TimeoutException("ffmpeg timeout");
        }

        // 进程已结束，检查退出码
        // 约定：exit code == 0 表示成功，非 0 表示失败
        int exitCode = p.exitValue();
        if (exitCode != 0) {
            throw new IOException("ffmpeg failed, exit=" + exitCode);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException, TimeoutException {

        Path input = Paths.get("/data/audio/source.mp3");
        Path output = Paths.get("/data/audio/mono.wav");

        List<String> command = List.of(
                "/usr/bin/ffmpeg",  // ← 这一步就是“调用 ffmpeg”
                "-y",
                "-i", input.toString(),
                "-ac", "1",
                output.toString()
        );

        runFfmpeg(command, 30_000);
    }

}
