package com.github.irof.test.spring_payload_binding.jackson3;

import com.github.irof.test.spring_payload_binding.jackson.EndpointPayloadTypes;
import com.github.irof.test.spring_payload_binding.jackson.JacksonContextProvider;
import com.github.irof.test.spring_payload_binding.PayloadTestContext;
import com.github.irof.test.spring_payload_binding.jackson.PayloadTestContextImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.AbstractJacksonHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.Collection;
import java.util.List;

/**
 * Jackson3 を使用した {@link JacksonContextProvider} の実装です。
 */
@SuppressWarnings("unchecked")
public class Jackson3ContextProvider implements JacksonContextProvider {

    private static final Logger log = LoggerFactory.getLogger(Jackson3ContextProvider.class);

    /**
     * コンストラクタ
     */
    public Jackson3ContextProvider() {
    }

    @Override
    public Collection<? extends PayloadTestContext> collect(RequestMappingHandlerMapping mapping, List<HttpMessageConverter<?>> messageConverters) {
        var mapper = messageConverters.stream()
                .filter(AbstractJacksonHttpMessageConverter.class::isInstance)
                .map(c -> (AbstractJacksonHttpMessageConverter<ObjectMapper>) c)
                .map(AbstractJacksonHttpMessageConverter::getMapper)
                .findFirst()
                .orElseGet(() -> {
                    log.warn("Jackson3 HttpMessageConverter not found; falling back to plain JsonMapper");
                    return JsonMapper.builder().build();
                });
        var adapter = new Jackson3Adapter(mapper);
        return EndpointPayloadTypes.collect(mapping, adapter).stream()
                .map(pt -> new PayloadTestContextImpl<>(pt, adapter))
                .toList();
    }
}
