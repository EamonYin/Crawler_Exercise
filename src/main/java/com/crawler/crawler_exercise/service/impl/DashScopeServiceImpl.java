package com.crawler.crawler_exercise.service.impl;

import cn.hutool.core.io.FileUtil;
import com.alibaba.cloud.ai.dashscope.audio.DashScopeAudioSpeechOptions;
import com.alibaba.cloud.ai.dashscope.audio.DashScopeAudioTranscriptionOptions;
import com.alibaba.cloud.ai.dashscope.audio.synthesis.*;
import com.alibaba.cloud.ai.dashscope.audio.transcription.AudioTranscriptionModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.dashscope.audio.asr.recognition.Recognition;
import com.alibaba.dashscope.audio.asr.recognition.RecognitionParam;
import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.crawler.crawler_exercise.entiy.input.DashScopeChatInput;
import com.crawler.crawler_exercise.entiy.output.Trip.TripResponse;
import com.crawler.crawler_exercise.service.IDashScopeService;
import com.crawler.crawler_exercise.utls.tool.MysqlChatMemory;
import com.crawler.crawler_exercise.utls.tool.TimeTools;
import com.crawler.crawler_exercise.utls.tool.TripPlanTools;
import com.volcengine.tos.TOSV2;
import com.volcengine.tos.TOSV2ClientBuilder;
import com.volcengine.tos.TosClientException;
import com.volcengine.tos.TosServerException;
import com.volcengine.tos.model.object.PutObjectInput;
import com.volcengine.tos.model.object.PutObjectOutput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
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
    public Flux<String> DashScopeChatByMemory(DashScopeChatInput input) {
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
    public TripResponse DashScopeChatByMemoryInStruct(DashScopeChatInput input) {
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
    public void DashScopeVoidRead(String text) {
        DashScopeAudioSpeechOptions scopeSpeechSynthesisOptions = DashScopeAudioSpeechOptions.builder()
                .model(model)
                .voice(voice)
                .build();

        byte[] array = synthesisModel.call(
                new SpeechSynthesisPrompt(text,scopeSpeechSynthesisOptions)).getResult().getOutput().getAudio().array();

        System.out.println("Base64: " + Base64.getEncoder().encodeToString(array));

    }

    public String DashScopeVoidToText(String musicStr) throws Exception {

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

    private static String uploadToOss(OSS ossClient, InputStream inputStream, long contentLength, String bucketName, String objectName) throws IOException {
        // 配置文件元数据
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(contentLength);
        metadata.setContentType("mp3");
        metadata.setCacheControl("max-age=31536000"); // 缓存1年

        // 构建上传请求
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName,
                inputStream, metadata);

        // 执行上传
        ossClient.putObject(putObjectRequest);

        // 返回访问URL
        return "https://" + bucketName + ".oss-cn-beijing.aliyuncs.com/" + objectName;
    }

    private static OSS createOssClient() {
        // 从配置中心或环境变量获取更安全，这里为示例简化
        String accessKeyId = "";
        String accessKeySecret = "";
        String endpoint = "https://oss-cn-beijing.aliyuncs.com";
        String region = "cn-beijing";

        CredentialsProvider credentialsProvider = new DefaultCredentialProvider(accessKeyId, accessKeySecret);
        ClientBuilderConfiguration clientConfig = new ClientBuilderConfiguration();
        clientConfig.setSignatureVersion(SignVersion.V4);

        return OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientConfig)
                .region(region)
                .build();
    }


    // 火山引擎
    public static void main(String[] args) {
        String endpoint = "tos-cn-beijing.volces.com";
        String region = "cn-beijing";
        String accessKey = System.getenv("");
        String secretKey = System.getenv("==");

        String bucketName = "";
        String objectKey = "test/hello_world_female2.wav";

        TOSV2 tos = new TOSV2ClientBuilder().build(region, endpoint, accessKey, secretKey);

        try{
//            String data = "1234567890abcdefghijklmnopqrstuvwxyz~!@#$%^&*()_+<>?,./   :'1234567890abcdefghijklmnopqrstuvwxyz~!@#$%^&*()_+<>?,./   :'";
//            ByteArrayInputStream stream = new ByteArrayInputStream(data.getBytes());
            File file = new File("/Downloads/hello_world_female2.wav");
            InputStream inputStream = new FileInputStream(file);
            PutObjectInput putObjectInput = new PutObjectInput().setBucket(bucketName).setKey(objectKey).setContent(inputStream);
            PutObjectOutput output = tos.putObject(putObjectInput);
            System.out.println("putObject succeed, object's etag is " + output.getEtag());
            System.out.println("putObject succeed, object's crc64 is " + output.getHashCrc64ecma());
        } catch (TosClientException e) {
            // 操作失败，捕获客户端异常，一般情况是请求参数错误，此时请求并未发送
            System.out.println("putObject failed");
            System.out.println("Message: " + e.getMessage());
            if (e.getCause() != null) {
                e.getCause().printStackTrace();
            }
        } catch (TosServerException e) {
            // 操作失败，捕获服务端异常，可以获取到从服务端返回的详细错误信息
            System.out.println("putObject failed");
            System.out.println("StatusCode: " + e.getStatusCode());
            System.out.println("Code: " + e.getCode());
            System.out.println("Message: " + e.getMessage());
            System.out.println("RequestID: " + e.getRequestID());
        } catch (Throwable t) {
            // 作为兜底捕获其他异常，一般不会执行到这里
            System.out.println("putObject failed");
            System.out.println("unexpected exception, message: " + t.getMessage());
        }
    }

    //    public static void main(String[] args) throws IOException {
//        OSS ossClient = createOssClient();
//
//        // base64版本
////        String base64 = "";
////        if (base64 == null) {
////            throw new IllegalArgumentException("base64 is null");
////        }
////        // 移除 data URI 前缀（如 "data:audio/wav;base64,"）并去除空白
////        int comma = base64.indexOf(',');
////        String payload = (comma >= 0) ? base64.substring(comma + 1) : base64;
////        payload = payload.replaceAll("\\s+", "");
////        byte[] bytes = Base64.getDecoder().decode(payload);
////        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
////        uploadToOss(ossClient,byteArrayInputStream,bytes.length,"bucketName","testvoice/voice"+new Date());
//
//        //上传文件版
//        File file = new File("/Downloads/test.mp3");
//        long contentLength = file.length(); // 获取文件大小（字节）
//        InputStream inputStream = new FileInputStream(file);
//        String url = uploadToOss(ossClient, inputStream, contentLength, "bucketName", "testvoice/voice"+new Date()+".mp3");
//        System.out.println("上传成功，URL: " + url);
//    }


}