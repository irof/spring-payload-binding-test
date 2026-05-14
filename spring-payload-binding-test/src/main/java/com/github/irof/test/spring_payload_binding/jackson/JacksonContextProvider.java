package com.github.irof.test.spring_payload_binding.jackson;

import com.github.irof.test.spring_payload_binding.PayloadTestContext;
import com.github.irof.test.spring_payload_binding.jackson2.Jackson2ContextProvider;
import com.github.irof.test.spring_payload_binding.jackson3.Jackson3ContextProvider;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Collection;
import java.util.List;

/**
 * Jackson バージョンごとのペイロードテストコンテキスト収集戦略です。
 * {@link com.github.irof.test.spring_payload_binding.JsonBindingContractTestBase} がクラスパスに応じて適切な実装を自動選択します。
 */
public interface JacksonContextProvider {

    /**
     * 使用するJacksonを判別します。
     *
     * Jacksonは2/3混在可能ですが、3が存在する場合は3を決め打ちで使用します。
     */
    static JacksonContextProvider detectProvider() {
        try {
            Class.forName("tools.jackson.databind.ObjectMapper");
            return new Jackson3ContextProvider();
        } catch (ClassNotFoundException e) {
            return new Jackson2ContextProvider();
        }
    }

    /**
     * エンドポイントのペイロード型を収集してテストコンテキストを返します。
     *
     * @param mapping           HandlerMapping
     * @param messageConverters Mapperを保持しているConverterのコレクション
     * @return テストコンテキストのコレクション
     */
    Collection<? extends PayloadTestContext> collect(RequestMappingHandlerMapping mapping, List<HttpMessageConverter<?>> messageConverters);
}
