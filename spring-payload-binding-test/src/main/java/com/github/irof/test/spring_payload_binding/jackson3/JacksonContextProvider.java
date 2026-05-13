package com.github.irof.test.spring_payload_binding.jackson3;

import com.github.irof.test.spring_payload_binding.PayloadTestContext;
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
 * Jackson3 を使用した {@link com.github.irof.test.spring_payload_binding.JacksonContextProvider} の実装です。
 */
@SuppressWarnings("unchecked")
public class JacksonContextProvider implements com.github.irof.test.spring_payload_binding.JacksonContextProvider {

    private static final Logger log = LoggerFactory.getLogger(JacksonContextProvider.class);

    @Override
    public Collection<? extends PayloadTestContext> collect(RequestMappingHandlerMapping mapping, List<HttpMessageConverter<?>> messageConverters) {
        ObjectMapper mapper = messageConverters.stream()
                .filter(AbstractJacksonHttpMessageConverter.class::isInstance)
                .map(c -> (AbstractJacksonHttpMessageConverter<ObjectMapper>) c)
                .map(AbstractJacksonHttpMessageConverter::getMapper)
                .findFirst()
                .orElseGet(() -> {
                    log.warn("Jackson3 HttpMessageConverter not found; falling back to plain JsonMapper");
                    return JsonMapper.builder().build();
                });
        return EndpointPayloadTypes.collect(mapping, mapper).stream()
                .map(pt -> new Jackson3PayloadTestContext(pt, mapper))
                .toList();
    }
}
