package com.github.irof.test.spring_payload_binding.jackson2;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.irof.test.spring_payload_binding.EndpointPayloadTypes;
import com.github.irof.test.spring_payload_binding.JacksonContextProvider;
import com.github.irof.test.spring_payload_binding.PayloadTestContext;
import com.github.irof.test.spring_payload_binding.PayloadTestContextImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Collection;
import java.util.List;

/**
 * Jackson2 を使用した {@link JacksonContextProvider} の実装です。
 */
public class Jackson2ContextProvider implements JacksonContextProvider {

    private static final Logger log = LoggerFactory.getLogger(Jackson2ContextProvider.class);

    /**
     * コンストラクタ
     */
    public Jackson2ContextProvider() {
    }

    @SuppressWarnings("removal")
    @Override
    public Collection<? extends PayloadTestContext> collect(RequestMappingHandlerMapping mapping, List<HttpMessageConverter<?>> messageConverters) {
        var mapper = messageConverters.stream()
                .filter(AbstractJackson2HttpMessageConverter.class::isInstance)
                .map(AbstractJackson2HttpMessageConverter.class::cast)
                .map(AbstractJackson2HttpMessageConverter::getObjectMapper)
                .findFirst()
                .orElseGet(() -> {
                    log.warn("Jackson2 HttpMessageConverter not found; falling back to plain ObjectMapper");
                    return JsonMapper.builder()
                            .disable(MapperFeature.REQUIRE_HANDLERS_FOR_JAVA8_TIMES)
                            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                            .addModule(new JavaTimeModule())
                            .findAndAddModules()
                            .build();
                });
        var adapter = new Jackson2Adapter(mapper);
        return EndpointPayloadTypes.collect(mapping, adapter).stream()
                .map(pt -> new PayloadTestContextImpl<>(pt, adapter))
                .toList();
    }
}
