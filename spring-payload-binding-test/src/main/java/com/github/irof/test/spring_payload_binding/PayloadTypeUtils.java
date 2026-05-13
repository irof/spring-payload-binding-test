package com.github.irof.test.spring_payload_binding;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

/**
 * ペイロード型収集ロジックで使用する、Jackson バージョン非依存のユーティリティクラスです。
 * jackson2/jackson3 両パッケージから共有されます。
 */
public final class PayloadTypeUtils {

    private PayloadTypeUtils() {
    }

    /**
     * ペイロード型収集の対象外にすべきフレームワーク提供の型かどうかを判定します。
     * java.*、jakarta.*、javax.*、org.springframework.* はアプリケーション独自の型ではないため除外します。
     *
     * @param raw 判定対象のクラス
     * @return 除外すべきフレームワーク型であれば true
     */
    public static boolean isFrameworkType(Class<?> raw) {
        String name = raw.getName();
        return name.startsWith("java.")
                || name.startsWith("jakarta.")
                || name.startsWith("javax.")
                || name.startsWith("org.springframework.");
    }

    /**
     * JSON で単一値として表現されるスカラー型かどうかを判定します。
     * スカラー型はオブジェクトとして展開せず、そのまま除外します。
     *
     * @param raw 判定対象のクラス
     * @return スカラー型であれば true
     */
    public static boolean isScalar(Class<?> raw) {
        return raw.isPrimitive()
                || raw.isEnum()
                || raw == String.class
                || raw == Boolean.class
                || raw == Character.class
                || Number.class.isAssignableFrom(raw)
                || raw == Object.class;
    }

    /**
     * Spring フレームワーク内部のエンドポイントかどうかを判定します。
     * アプリケーションのコントローラーのみを対象とするため、フレームワーク内部ハンドラーは除外します。
     *
     * @param handler 判定対象のハンドラーメソッド
     * @return フレームワーク内部のハンドラーであれば true
     */
    public static boolean isFrameworkHandler(HandlerMethod handler) {
        return handler.getBeanType().getPackageName().startsWith("org.springframework.");
    }

    /**
     * エンドポイントを人が読める形式に変換します。テスト失敗時のエラーメッセージで使用されます。
     *
     * @param info    リクエストマッピング情報
     * @param handler ハンドラーメソッド
     * @return "{HTTPメソッド} {パス} ({クラス}#{メソッド})" 形式の文字列
     */
    public static String describeEndpoint(RequestMappingInfo info, HandlerMethod handler) {
        RequestMethodsRequestCondition methods = info.getMethodsCondition();
        String httpMethod = methods.getMethods().isEmpty() ? "ANY" : methods.getMethods().iterator().next().name();
        String path = info.getPathPatternsCondition() != null
                ? info.getPathPatternsCondition().getPatterns().stream().findFirst().map(Object::toString).orElse("?")
                : "?";
        String handlerLabel = handler.getMethod().getDeclaringClass().getSimpleName()
                + "#" + handler.getMethod().getName();
        return httpMethod + " " + path + " (" + handlerLabel + ")";
    }
}
