package com.crawler.crawler_exercise.utils.redisMQ.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 注册表模式的定义与处理器聚合器
 */
@Component
public class RegistryStreamRegistry {

    private static final Logger log = LoggerFactory.getLogger(RegistryStreamRegistry.class);

    private final Map<String, RegistryStreamDefinition> definitionsByStreamKey;
    private final List<RegistryStreamHandler> handlers;

    public RegistryStreamRegistry(List<RegistryStreamDefinition> definitions,
                                  List<RegistryStreamHandler> handlers) {
        // 按 streamKey 快速索引定义，方便绑定处理器
        this.definitionsByStreamKey = definitions.stream()
                .collect(Collectors.toMap(
                        RegistryStreamDefinition::getStreamKey,
                        definition -> definition,
                        (left, right) -> right,
                        LinkedHashMap::new
                ));
        this.handlers = handlers;
    }

    public List<RegistryStreamBinding> getBindings() {
        List<RegistryStreamBinding> bindings = new ArrayList<>();
        for (RegistryStreamHandler handler : handlers) {
            RegistryStreamDefinition definition = definitionsByStreamKey.get(handler.streamKey());
            if (definition == null) {
                // handler 没有对应的配置，直接跳过
                log.warn("No stream definition found for handler streamKey={}", handler.streamKey());
                continue;
            }
            bindings.add(new RegistryStreamBinding(definition, handler));
        }
        return bindings;
    }
}
