package com.crawler.crawler_exercise;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.crawler.crawler_exercise.config.AlibabaConfig;
import com.crawler.crawler_exercise.config.BytedanceConfig;
import com.volcengine.tos.TOSV2;
import com.volcengine.tos.TOSV2ClientBuilder;
import com.volcengine.tos.TosClientException;
import com.volcengine.tos.TosServerException;
import com.volcengine.tos.model.object.PutObjectInput;
import com.volcengine.tos.model.object.PutObjectOutput;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

@SpringBootTest
@Slf4j
public class OSSUploadTest {

    @Autowired
    AlibabaConfig alibabaConfig;

    @Autowired
    BytedanceConfig bytedanceConfig;

    // 火山引擎
    @Test
    public void bytedanceOss() {
        String endpoint = "tos-cn-beijing.volces.com";
        String region = "cn-beijing";
        String accessKey = System.getenv(bytedanceConfig.getAccessKey());
        String secretKey = System.getenv(bytedanceConfig.getSecretKey());

        String bucketName = bytedanceConfig.getBucketName();
        String objectKey = "test/hello_world_female2.wav";

        TOSV2 tos = new TOSV2ClientBuilder().build(region, endpoint, accessKey, secretKey);

        try {
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

    //阿里云OSS
    @Test
    public void alibabaOSSTest() throws IOException {
        OSS ossClient = createOssClient();

        // base64版本
//        String base64 = "";
//        if (base64 == null) {
//            throw new IllegalArgumentException("base64 is null");
//        }
//        // 移除 data URI 前缀（如 "data:audio/wav;base64,"）并去除空白
//        int comma = base64.indexOf(',');
//        String payload = (comma >= 0) ? base64.substring(comma + 1) : base64;
//        payload = payload.replaceAll("\\s+", "");
//        byte[] bytes = Base64.getDecoder().decode(payload);
//        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
//        uploadToOss(ossClient,byteArrayInputStream,bytes.length,"bucketName","testvoice/voice"+new Date());

        //上传文件版
        File file = new File("/Downloads/test.mp3");
        long contentLength = file.length(); // 获取文件大小（字节）
        InputStream inputStream = new FileInputStream(file);
        String url = uploadToOss(ossClient, inputStream, contentLength, alibabaConfig.getBucketName(), "testvoice/voice" + new Date() + ".mp3");
        System.out.println("上传成功，URL: " + url);
    }

    private String uploadToOss(OSS ossClient, InputStream inputStream, long contentLength, String bucketName, String objectName) throws IOException {
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

    private OSS createOssClient() {
        // 从配置中心或环境变量获取更安全，这里为示例简化
        String accessKeyId = alibabaConfig.getAccessKeyId();
        String accessKeySecret = alibabaConfig.getAccessKeySecret();
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
}
