package com.crawler.crawler_exercise;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.TemplateType;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.baomidou.mybatisplus.generator.fill.Column;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MyBatis-Plus code generator entry.
 *
 * Environment variables:
 * - MPG_DB_URL (required)
 * - MPG_DB_USERNAME (required)
 * - MPG_DB_PASSWORD (required)
 * - MPG_DB_SCHEMA (optional)
 * - MPG_TABLES (optional, comma-separated; falls back to program args)
 * - MPG_TABLE_PREFIX (optional, comma-separated)
 * - MPG_AUTHOR (optional, default: eamon)
 * - MPG_PARENT_PACKAGE (optional, default: com.crawler.crawler_exercise)
 * - MPG_OUTPUT_DIR (optional, default: ${user.dir}/src/main/java)
 * - MPG_XML_DIR (optional, default: ${user.dir}/src/main/resources/mapper)
 * - MPG_FILE_OVERRIDE (optional, true/false, default: false)
 */
public class MybatisPlusCodeGenerator {

    public static void main(String[] args) {
        String projectRoot = System.getProperty("user.dir");
        Map<String, String> datasourceFromYml = loadDatasourceFromYml(
                Paths.get(projectRoot, "src", "main", "resources", "application.yml")
        );

        String dbUrl = firstNonBlank(env("MPG_DB_URL"), datasourceFromYml.get("url"));
        String dbUsername = firstNonBlank(env("MPG_DB_USERNAME"), datasourceFromYml.get("username"));
        String dbPassword = firstNonBlank(env("MPG_DB_PASSWORD"), datasourceFromYml.get("password"));
        String dbDriver = firstNonBlank(env("MPG_DB_DRIVER"), datasourceFromYml.get("driver-class-name"));

        requireNonBlank(dbUrl, "缺少数据库地址。请设置 MPG_DB_URL，或在 application.yml 中配置 spring.datasource.url");
        requireNonBlank(dbUsername, "缺少数据库用户名。请设置 MPG_DB_USERNAME，或在 application.yml 中配置 spring.datasource.username");
        requireNonBlank(dbPassword, "缺少数据库密码。请设置 MPG_DB_PASSWORD，或在 application.yml 中配置 spring.datasource.password");
        dbDriver = defaultIfBlank(dbDriver, "com.mysql.cj.jdbc.Driver");

        List<String> includeTablesInput = parseList(env("MPG_TABLES"));
        if (includeTablesInput.isEmpty() && args != null && args.length > 0) {
            includeTablesInput = Arrays.stream(args).map(String::trim).filter(s -> !s.isEmpty()).toList();
        }
        if (includeTablesInput.isEmpty()) {
            throw new IllegalArgumentException("未指定要生成的表。请设置 MPG_TABLES，或通过程序参数传入表名。");
        }
        final List<String> includeTables = includeTablesInput;

        List<String> tablePrefixes = parseList(env("MPG_TABLE_PREFIX"));
        String outputDir = defaultIfBlank(env("MPG_OUTPUT_DIR"),
                Paths.get(projectRoot, "src", "main", "java").toString());
        String xmlDir = defaultIfBlank(env("MPG_XML_DIR"),
                Paths.get(projectRoot, "src", "main", "resources", "mapper").toString());
        String parentPackage = defaultIfBlank(env("MPG_PARENT_PACKAGE"), "com.crawler.crawler_exercise");
        String author = defaultIfBlank(env("MPG_AUTHOR"), "eamon");
        boolean fileOverride = Boolean.parseBoolean(defaultIfBlank(env("MPG_FILE_OVERRIDE"), "false"));
        String schema = env("MPG_DB_SCHEMA");

        FastAutoGenerator generator = FastAutoGenerator.create(
                new DataSourceConfig.Builder(dbUrl, dbUsername, dbPassword)
                        .driverClassName(dbDriver)
        );

        if (!isBlank(schema)) {
            generator.dataSourceConfig(builder -> builder.schema(schema));
        }

        generator
                .globalConfig(builder -> {
                    builder.author(author)
                            .disableOpenDir()
                            .dateType(DateType.TIME_PACK)
                            .commentDate("yyyy-MM-dd")
                            .outputDir(outputDir);
                })
                .packageConfig(builder -> builder
                        .parent(parentPackage)
                        // Keep package naming aligned with existing project structure.
                        .entity("entiy")
                        .mapper("mapper")
                        .service("service")
                        .serviceImpl("service.impl")
                        .pathInfo(Map.of(OutputFile.xml, xmlDir))
                )
                .strategyConfig(builder -> {
                    builder.addInclude(includeTables);
                    if (!tablePrefixes.isEmpty()) {
                        builder.addTablePrefix(tablePrefixes);
                    }
                    builder.entityBuilder()
                            .enableLombok()
                            .enableTableFieldAnnotation()
                            .naming(NamingStrategy.underline_to_camel)
                            .columnNaming(NamingStrategy.underline_to_camel)
                            .addTableFills(
                                    new Column("create_time", FieldFill.INSERT),
                                    new Column("update_time", FieldFill.INSERT_UPDATE)
                            );
                    if (fileOverride) {
                        builder.entityBuilder().enableFileOverride();
                    }

                    builder.mapperBuilder()
                            .enableMapperAnnotation()
                            .formatMapperFileName("%sMapper")
                            .formatXmlFileName("%sMapper");
                    if (fileOverride) {
                        builder.mapperBuilder().enableFileOverride();
                    }

                    builder.serviceBuilder()
                            .formatServiceFileName("I%sService")
                            .formatServiceImplFileName("%sServiceImpl");
                    if (fileOverride) {
                        builder.serviceBuilder().enableFileOverride();
                    }
                })
                .templateConfig(builder -> builder.disable(TemplateType.CONTROLLER))
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();

        System.out.println("MyBatis-Plus 代码生成完成，表：" + String.join(", ", includeTables));
    }

    private static String env(String key) {
        return System.getenv(key);
    }

    private static String requireEnv(String key) {
        String value = env(key);
        if (isBlank(value)) {
            throw new IllegalArgumentException("缺少必填环境变量：" + key);
        }
        return value.trim();
    }

    private static Map<String, String> loadDatasourceFromYml(Path ymlPath) {
        Map<String, String> datasource = new HashMap<>();
        if (!Files.exists(ymlPath)) {
            return datasource;
        }
        Yaml yaml = new Yaml();
        try (InputStream inputStream = Files.newInputStream(ymlPath)) {
            Object rootObj = yaml.load(inputStream);
            if (!(rootObj instanceof Map<?, ?> rootMap)) {
                return datasource;
            }
            Object springObj = rootMap.get("spring");
            if (!(springObj instanceof Map<?, ?> springMap)) {
                return datasource;
            }
            Object dsObj = springMap.get("datasource");
            if (!(dsObj instanceof Map<?, ?> dsMap)) {
                return datasource;
            }
            putIfString(dsMap, datasource, "url");
            putIfString(dsMap, datasource, "username");
            putIfString(dsMap, datasource, "password");
            putIfString(dsMap, datasource, "driver-class-name");
            return datasource;
        } catch (IOException e) {
            throw new IllegalStateException("读取 application.yml 失败：" + ymlPath, e);
        }
    }

    private static void putIfString(Map<?, ?> source, Map<String, String> target, String key) {
        Object value = source.get(key);
        if (value instanceof String str && !isBlank(str)) {
            target.put(key, str.trim());
        }
    }

    private static String firstNonBlank(String primary, String fallback) {
        return isBlank(primary) ? fallback : primary.trim();
    }

    private static void requireNonBlank(String value, String message) {
        if (isBlank(value)) {
            throw new IllegalArgumentException(message);
        }
    }

    private static List<String> parseList(String csv) {
        if (isBlank(csv)) {
            return List.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private static String defaultIfBlank(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
