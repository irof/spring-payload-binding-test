package com.github.irof.test.spring_payload_binding;

import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Collection;

/**
 * Jackson バージョンごとのペイロードテストコンテキスト収集戦略です。
 * {@link JsonBindingContractTestBase} がクラスパスに応じて適切な実装を自動選択します。
 */
public interface JacksonContextProvider {

    /**
     * エンドポイントのペイロード型を収集してテストコンテキストを返します。
     *
     * @param mapping HandlerMapping
     * @param adapter HandlerAdapter (ObjectMapper の取得に使用)
     * @return テストコンテキストのコレクション
     */
    Collection<? extends PayloadTestContext> collect(RequestMappingHandlerMapping mapping, RequestMappingHandlerAdapter adapter);
}
