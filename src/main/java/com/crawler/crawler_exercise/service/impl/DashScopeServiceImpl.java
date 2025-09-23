package com.crawler.crawler_exercise.service.impl;

import com.alibaba.cloud.ai.dashscope.audio.DashScopeAudioSpeechOptions;
import com.alibaba.cloud.ai.dashscope.audio.synthesis.*;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.dashscope.audio.asr.recognition.Recognition;
import com.alibaba.dashscope.audio.asr.recognition.RecognitionParam;
import com.alibaba.dashscope.audio.asr.transcription.Transcription;
import com.alibaba.dashscope.audio.asr.transcription.TranscriptionParam;
import com.alibaba.dashscope.audio.asr.transcription.TranscriptionQueryParam;
import com.alibaba.dashscope.audio.asr.transcription.TranscriptionResult;
import com.alibaba.dashscope.common.TaskStatus;
import com.crawler.crawler_exercise.entiy.input.DashScopeChatInput;
import com.crawler.crawler_exercise.entiy.output.Trip.TripResponse;
import com.crawler.crawler_exercise.service.IDashScopeService;
import com.crawler.crawler_exercise.utls.tool.MysqlChatMemory;
import com.crawler.crawler_exercise.utls.tool.TimeTools;
import com.crawler.crawler_exercise.utls.tool.TripPlanTools;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
@Slf4j
public class DashScopeServiceImpl implements IDashScopeService {

    private static final String DEFAULT_PROMPT = "你是一个博学的智能聊天助手，请根据用户提问回答！";

    private final ChatClient chatClient;

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;


    public DashScopeServiceImpl(ChatClient.Builder builder, MysqlChatMemory mysqlChatMemory) {
        this.chatClient = builder.defaultSystem(DEFAULT_PROMPT)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                // 注册Advisor
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(mysqlChatMemory).build())
                .defaultOptions(
                        DashScopeChatOptions.builder()
                                .withTopP(0.7)
                                .build()
                )
                .build();
    }

    @Override
    public Flux<String> dashScopeChatByMemory(DashScopeChatInput input) {
        UserMessage user = UserMessage.builder()
                .text(input.getQuestion())
                .metadata(Map.of("type", input.getType()))
                .build();
        SystemMessage system = SystemMessage.builder()
                .text("当且仅当调用 TripPlanTools 工具时，最终只输出严格 JSON 格式" +
                        "JSON模板如下，替换成TripPlanTools返回的真实数据：" +
                        "{\"code\":0,\"message\":\"ok\",\"data\":{\"days\":[{\"day\":1,\"city\":\"城市名\",\"hotel\":[{\"name\":\"酒店1\",\"id\":\"49017\",\"rating\":5,\"photos\":[\"http://store.is.autonavi.com/showpic/dc\"],\"booking_url\":\"https://www.amap.com/place/B0L6LSA57J\"}],\"attractions\":[{\"name\":\"景点1\",\"photos\":[\"http://store.is.autonavi.com/showpic/dc\"],\"rating\":4.8,\"address\":\"天津市南开区\",\"booking_url\":\"https://www.amap.com/place/B0L6LSA57J\"}]}]}}" +
                        "不得包含多余字符/解释/Markdown 代码块；缺失字段用 null/空串占位。").build();
        return chatClient.prompt(new Prompt(List.of(user, system)))
                .tools(new TimeTools(), new TripPlanTools())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, input.getConversationId()))
                .stream()
                .content();
    }

    @Override
    public TripResponse dashScopeChatByMemoryInStruct(DashScopeChatInput input) {
        UserMessage user = UserMessage.builder()
                .text(input.getQuestion())
                .metadata(Map.of("type", input.getType()))
                .build();
        return chatClient.prompt(new Prompt(List.of(user)))
                .tools(new TimeTools(), new TripPlanTools())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, input.getConversationId()))
                .call()
                .entity(TripResponse.class); //结构化对象结果
    }

    //模型名称 龙飞
    private static String model = "cosyvoice-v1"; // 改回 v1，v2 可能未开通或不支持当前 voice
    //音色名称
    private static String voice = "longfei";

    @Autowired
    private SpeechSynthesisModel synthesisModel;

    @Override
    public String dashScopeVoidRead(String text) {
        DashScopeAudioSpeechOptions scopeSpeechSynthesisOptions = DashScopeAudioSpeechOptions.builder()
                .model(model)
                .voice(voice)
                .build();

        byte[] array = synthesisModel.call(
                new SpeechSynthesisPrompt(text,scopeSpeechSynthesisOptions)).getResult().getOutput().getAudio().array();

        System.out.println("Base64: " + Base64.getEncoder().encodeToString(array));

        return Base64.getEncoder().encodeToString(array);
    }

    @Override
    public String dashScopeVoiceToText(String musicStr) throws Exception {

        // 音频资源
//        UrlResource audioResource = new UrlResource(AUDIO_RESOURCES_URL);
//
//        // 指定参数
//        DashScopeAudioTranscriptionOptions transcriptionOptions = DashScopeAudioTranscriptionOptions.builder()
//                .withModel("paraformer-realtime-8k-v2")
//                .build();
//
//        AudioTranscriptionResponse call = audioTranscriptionModel.call(
//                new AudioTranscriptionPrompt(audioResource, transcriptionOptions)
//        );
//
//        System.out.println(call.getResult().getOutput());

        // <|Speech|>hello world,这里是阿里巴巴语音实验室。<|/Speech|><|NEUTRAL|>


        // 用户可忽略url下载文件部分，可以直接使用本地文件进行相关api调用进行识别
        String exampleWavUrl = musicStr;
        try {
            InputStream in = new URL(exampleWavUrl).openStream();
            Files.copy(in, Paths.get("asr_example.wav"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println("error: " + e);
            System.exit(1);
        }

        // 创建Recognition实例
        Recognition recognizer = new Recognition();
        // 创建RecognitionParam
        RecognitionParam param =
                RecognitionParam.builder()
                        // 若没有将API Key配置到环境变量中，需将下面这行代码注释放开，并将apiKey替换为自己的API Key
                         .apiKey(apiKey)
                        .model("paraformer-realtime-v2")
                        .format("wav")
                        .sampleRate(16000)
                        // “language_hints”只支持paraformer-v2和paraformer-realtime-v2模型
                        .parameter("language_hints", new String[]{"zh", "en"})
                        .build();

        try {
            String callResult = recognizer.call(param, new File("asr_example.wav"));
            System.out.println("识别结果：" + callResult);
            return callResult;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String dashScopeOnlineVoiceToText(String musicStr) throws Exception {
        // 创建转写请求参数
        TranscriptionParam param =
                TranscriptionParam.builder()
                        // 若没有将API Key配置到环境变量中，需将apiKey替换为自己的API Key
                        .apiKey(apiKey)
                        .model("paraformer-v2")
                        // “language_hints”只支持paraformer-v2模型
                        .parameter("language_hints", new String[]{"zh", "en"})
                        .fileUrls(
                                Arrays.asList(musicStr))
                        .build();
        try {
            Transcription transcription = new Transcription();
            // 提交转写请求
            TranscriptionResult result = transcription.asyncCall(param);
            System.out.println("RequestId: " + result.getRequestId());
            // 阻塞等待任务完成并获取结果
            result = transcription.wait(
                    TranscriptionQueryParam.FromTranscriptionParam(param, result.getTaskId()));
            // 打印结果
//            System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(result.getOutput()));
            System.out.println("单"+result.getOutput().get("results").getAsJsonArray().get(0).getAsJsonObject());
            return parsOnlineJsonToText(result.getOutput().get("results").getAsJsonArray().get(0).getAsJsonObject().get("transcription_url").getAsString());
        } catch (Exception e) {
            System.out.println("error: " + e);
        }
        return null;
    }


    @Override
    public String dashScopeRecordToText(String musicStr) {
        // 创建转写请求参数
        TranscriptionParam param =
                TranscriptionParam.builder()
                        // 若没有将API Key配置到环境变量中，需将apiKey替换为自己的API Key
                        .apiKey(apiKey)
                        .model("paraformer-v2")
                        // “language_hints”只支持paraformer-v2模型
                        .parameter("language_hints", new String[]{"zh", "en"})
                        .fileUrls(
                                Arrays.asList(
                                        "https://dashscope.oss-cn-beijing.aliyuncs.com/samples/audio/paraformer/hello_world_female2.wav",
                                        "https://dashscope.oss-cn-beijing.aliyuncs.com/samples/audio/paraformer/hello_world_male2.wav"))
                        .build();
        try {
            Transcription transcription = new Transcription();
            // 提交转写请求
            TranscriptionResult result = transcription.asyncCall(param);
            System.out.println("RequestId: " + result.getRequestId());
            // 循环获取任务执行结果，直到任务结束
            while (true) {
                result = transcription.fetch(TranscriptionQueryParam.FromTranscriptionParam(param, result.getTaskId()));
                if (result.getTaskStatus() == TaskStatus.SUCCEEDED || result.getTaskStatus() == TaskStatus.FAILED) {
                    break;
                }
                Thread.sleep(1000);
            }
            // 打印结果
            for (JsonElement results : result.getOutput().get("results").getAsJsonArray()) {
                System.out.println("录音转文字："+results.getAsJsonObject().get("transcription_url"));
                parsOnlineJsonToText(results.getAsJsonObject().get("transcription_url").getAsString());
            }
        } catch (Exception e) {
            System.out.println("error: " + e);
        }
        return "";
    }

    public String parsOnlineJsonToText(String url) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode root = mapper.readTree(new URL(url));
        JsonNode transcriptsNode = root.path("transcripts");
        String text = "";
        if (transcriptsNode.isArray() && transcriptsNode.size() > 0) {
            JsonNode first = transcriptsNode.get(0);
            text = first.path("text").asText(null); // 若不存在返回 null
            System.out.println("first text: " + text);
        } else {
            System.out.println("transcripts 不存在或为空");
        }

        // 方法2：读取为通用 Map（也可改为具体 POJO：MyClass.class）
//        Map<String, Object> map = mapper.readValue(new URL(url), new TypeReference<Map<String, Object>>() {});
//        System.out.println("=== Map pretty ===");
//        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map));

        return text;
    }


}