package com.github.irof.test.spring_payload_binding;

import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Collection;
import java.util.List;

/**
 * Jackson バージョンごとのペイロードテストコンテキスト収集戦略です。
 * {@link JsonBindingContractTestBase} がクラスパスに応じて適切な実装を自動選択します。
 */
public interface JacksonContextProvider {

    /**
     * エンドポイントのペイロード型を収集してテストコンテキストを返します。
     *
     * @param mapping HandlerMapping
     * @param messageConverters Mapperを保持しているConverterのコレクション
     * @return テストコンテキストのコレクション
     */
    Collection<? extends PayloadTestContext> collect(RequestMappingHandlerMapping mapping, List<HttpMessageConverter<?>> messageConverters);
}
