package com.github.irof.test.spring_payload_binding.jackson3;

import com.github.irof.test.spring_payload_binding.PayloadTestContext;
import com.github.irof.test.spring_payload_binding.Variation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.AbstractJacksonHttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.Collection;
import java.util.List;

/**
 * Jackson3 を使用した JSON バインディングコントラストテストの基底クラスです。
 * エンドポイントのペイロード型を Jackson3 の型システムで収集し、テストを生成します。
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
     * RequestMappingHandlerAdapter に登録されている AbstractJacksonHttpMessageConverter から取得します。
     *
     * @return ObjectMapper
     */
    @SuppressWarnings("unchecked")
    protected ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            if (autowiredObjectMapper != null) {
                objectMapper = autowiredObjectMapper;
            } else {
                objectMapper = handlerAdapter.getMessageConverters().stream()
                        .filter(AbstractJacksonHttpMessageConverter.class::isInstance)
                        .map(c -> (AbstractJacksonHttpMessageConverter<ObjectMapper>) c)
                        .map(AbstractJacksonHttpMessageConverter::getMapper)
                        .findFirst()
                        .orElseGet(() -> {
                            log.warn("Jackson HttpMessageConverter not found; falling back to plain JsonMapper");
                            return JsonMapper.builder().build();
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
        return List.of(Jackson3Variation.SAMPLE, Jackson3Variation.NULL, Jackson3Variation.EMPTY);
    }

    @Override
    protected Collection<Jackson3PayloadTestContext> collectPayloadContexts(RequestMappingHandlerMapping handlerMapping) {
        ObjectMapper mapper = getObjectMapper();
        return EndpointPayloadTypes.collect(handlerMapping, mapper).stream()
                .map(pt -> new Jackson3PayloadTestContext(pt, mapper))
                .toList();
    }
}
