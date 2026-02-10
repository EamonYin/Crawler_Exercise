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

import java.nio.file.Paths;
import java.util.Arrays;
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
        String dbUrl = requireEnv("MPG_DB_URL");
        String dbUsername = requireEnv("MPG_DB_USERNAME");
        String dbPassword = requireEnv("MPG_DB_PASSWORD");

        List<String> includeTablesInput = parseList(env("MPG_TABLES"));
        if (includeTablesInput.isEmpty() && args != null && args.length > 0) {
            includeTablesInput = Arrays.stream(args).map(String::trim).filter(s -> !s.isEmpty()).toList();
        }
        if (includeTablesInput.isEmpty()) {
            throw new IllegalArgumentException("No tables specified. Set MPG_TABLES or pass table names as args.");
        }
        final List<String> includeTables = includeTablesInput;

        List<String> tablePrefixes = parseList(env("MPG_TABLE_PREFIX"));
        String projectRoot = System.getProperty("user.dir");
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
                        .driverClassName("com.mysql.cj.jdbc.Driver")
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

        System.out.println("MyBatis-Plus code generation completed. Tables: " + String.join(", ", includeTables));
    }

    private static String env(String key) {
        return System.getenv(key);
    }

    private static String requireEnv(String key) {
        String value = env(key);
        if (isBlank(value)) {
            throw new IllegalArgumentException("Missing required environment variable: " + key);
        }
        return value.trim();
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
