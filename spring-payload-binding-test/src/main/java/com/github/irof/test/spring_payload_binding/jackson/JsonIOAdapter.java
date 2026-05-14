package com.github.irof.test.spring_payload_binding.jackson;

import java.io.File;

/**
 * Jackson の JSON 読み書き操作を抽象化します。
 *
 * @param <T> JavaType（JacksonのJava型表現）
 * @param <N> JsonNode（Jacksonのノード型）
 */
public interface JsonIOAdapter<T, N> {

    /**
     * オブジェクトを JSON 文字列にシリアライズします。
     */
    String writeValueAsString(Object value) throws Exception;

    /**
     * JSON 文字列を N にパースします。
     */
    N readTree(String json) throws Exception;

    /**
     * JSON ファイルを N にパースします。
     */
    N readTree(File file) throws Exception;

    /**
     * JSON 文字列を型 T に従ってデシリアライズします。
     */
    Object readValue(String json, T type) throws Exception;

    /**
     * N をインデント付き JSON としてファイルに書き出します。
     */
    void writePrettyValue(File file, N value) throws Exception;

    /**
     * N をインデント付き JSON 文字列に変換します。
     */
    String toPrettyString(N node);
}
