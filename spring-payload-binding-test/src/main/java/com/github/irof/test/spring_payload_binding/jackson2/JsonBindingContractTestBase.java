package com.github.irof.test.spring_payload_binding.jackson2;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.irof.test.spring_payload_binding.PayloadTestContext;
import com.github.irof.test.spring_payload_binding.Variation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Collection;
import java.util.List;

/**
 * Jackson2 を使用した JSON バインディングコントラストテストの基底クラスです。
 * エンドポイントのペイロード型を Jackson2 の型システムで収集し、テストを生成します。
 */
public abstract class JsonBindingContractTestBase extends com.github.irof.test.spring_payload_binding.JsonBindingContractTestBase {

    private static final Logger log = LoggerFactory.getLogger(JsonBindingContractTestBase.class);

    private volatile ObjectMapper objectMapper;

    @Autowired(required = false)
    private ObjectMapper autowiredObjectMapper;

    @Autowired
    private RequestMappingHandlerAdapter handlerAdapter;

    /**
     * 使用する ObjectMapper を取得します。
     * RequestMappingHandlerAdapter に登録されている MappingJackson2HttpMessageConverter から取得します。
     *
     * @return ObjectMapper
     */
    protected ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            if (autowiredObjectMapper != null) {
                objectMapper = autowiredObjectMapper;
            } else {
                objectMapper = handlerAdapter.getMessageConverters().stream()
                        .filter(AbstractJackson2HttpMessageConverter.class::isInstance)
                        .map(AbstractJackson2HttpMessageConverter.class::cast)
                        .map(AbstractJackson2HttpMessageConverter::getObjectMapper)
                        .findFirst()
                        .orElseGet(() -> {
                            log.warn("Jackson HttpMessageConverter not found; falling back to plain ObjectMapper");
                            return JsonMapper.builder()
                                    .disable(MapperFeature.REQUIRE_HANDLERS_FOR_JAVA8_TIMES)
                                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                                    .addModule(new JavaTimeModule())
                                    .findAndAddModules()
                                    .build();
                        });
            }
        }
        return objectMapper;
    }

    /**
     * 各ペイロードに対して実行するバリエーション群を返します。
     * デフォルトは全ペイロードで SAMPLE, NULL, EMPTY です。
     *
     * @param ctx ペイロードのテストコンテキスト
     * @return バリエーションのリスト
     */
    @Override
    protected List<Variation> variations(PayloadTestContext ctx) {
        return List.of(Jackson2Variation.SAMPLE, Jackson2Variation.NULL, Jackson2Variation.EMPTY);
    }

    @Override
    protected Collection<Jackson2PayloadTestContext> collectPayloadContexts(RequestMappingHandlerMapping handlerMapping) {
        ObjectMapper mapper = getObjectMapper();
        return EndpointPayloadTypes.collect(handlerMapping, mapper).stream()
                .map(pt -> new Jackson2PayloadTestContext(pt, mapper))
                .toList();
    }
}
