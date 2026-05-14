package com.github.irof.test.spring_payload_binding.jackson;

import com.github.irof.test.spring_payload_binding.TypeConfigurer;

import java.io.File;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Jackson バージョン固有の API を抽象化するアダプターインタフェースです。
 *
 * @param <T> JavaType（JacksonのJava型表現）
 * @param <N> JsonNode（Jacksonのノード型）
 */
public interface JacksonAdapter<T, N> {

    // --- 型クエリ ---

    /**
     * Optional や HttpEntity など参照型かどうかを返します。
     *
     * @param type 判定対象の型
     * @return 参照型であれば true
     */
    boolean isReferenceType(T type);

    /**
     * 参照型（Optional 等）が包む内側の型を返します。
     *
     * @param type 参照型
     * @return 内側の型
     */
    T referencedType(T type);

    /**
     * コレクション・配列かどうかを返します。
     *
     * @param type 判定対象の型
     * @return コレクション・配列であれば true
     */
    boolean isCollectionLike(T type);

    /**
     * コレクション・マップの要素（値）型を返します。
     *
     * @param type コレクションまたはマップの型
     * @return 要素型
     */
    T contentType(T type);

    /**
     * マップ型かどうかを返します。
     *
     * @param type 判定対象の型
     * @return マップ型であれば true
     */
    boolean isMapLike(T type);

    /**
     * マップのキー型を返します。
     *
     * @param type マップの型
     * @return キー型
     */
    T keyType(T type);

    /**
     * 型の raw クラスを返します。
     *
     * @param type 対象の型
     * @return raw クラス
     */
    Class<?> rawClass(T type);

    /**
     * コレクション・配列・マップを含む広義のコンテナ型かどうかを返します。
     *
     * @param type 判定対象の型
     * @return コンテナ型であれば true
     */
    boolean isContainerType(T type);

    /**
     * Java の {@link Type} から T を構築します。
     *
     * @param type 変換元の Java 型
     * @return 構築した型
     */
    T constructType(Type type);

    /**
     * Nth 型パラメータを返します。存在しない場合は不明型を返します。Optional/HttpEntity のアンラップに使用します。
     *
     * @param type  対象の型
     * @param index 型パラメータのインデックス（0 始まり）
     * @return Nth 型パラメータ、存在しない場合は不明型
     */
    T containedTypeOrUnknown(T type, int index);

    /**
     * 型の正規文字列表現（例: {@code java.util.List<java.lang.String>}）を返します。
     *
     * @param type 対象の型
     * @return 正規文字列表現
     */
    String toCanonical(T type);

    // --- イントロスペクション ---

    /**
     * {@code @JsonValue} アノテーションが付いたアクセサーの型を返します。存在しない場合は空を返します。
     *
     * @param type 検索対象の型
     * @return {@code @JsonValue} アクセサーの型。存在しない場合は空の Optional
     */
    Optional<T> findJsonValueAnnotationType(T type);

    /**
     * シリアライズ対象のプロパティ一覧を返します。
     *
     * @param type 対象の型
     * @return プロパティ定義のリスト
     */
    List<PropertyDef<T>> findProperties(T type);

    /**
     * シリアライズ対象プロパティの名前と型のペアです。
     *
     * @param <P>  プロパティの型表現
     * @param name プロパティ名
     * @param type プロパティの型
     */
    record PropertyDef<P>(String name, P type) {
    }

    // --- ノード生成 ---

    /**
     * null ノードを返します。
     *
     * @return null ノード
     */
    N nullNode();

    /**
     * 文字列ノードを返します。
     *
     * @param value 文字列値
     * @return 文字列ノード
     */
    N stringNode(String value);

    /**
     * int ノードを返します。
     *
     * @param value int 値
     * @return int ノード
     */
    N intNode(int value);

    /**
     * long ノードを返します。
     *
     * @param value long 値
     * @return long ノード
     */
    N longNode(long value);

    /**
     * 真偽値ノードを返します。
     *
     * @param value 真偽値
     * @return 真偽値ノード
     */
    N boolNode(boolean value);

    /**
     * float ノードを返します。
     *
     * @param value float 値
     * @return float ノード
     */
    N floatNode(float value);

    /**
     * double ノードを返します。
     *
     * @param value double 値
     * @return double ノード
     */
    N doubleNode(double value);

    /**
     * フィールドマップからオブジェクトノードを生成します。
     *
     * @param fields フィールド名とノードのマップ
     * @return オブジェクトノード
     */
    N objectNode(Map<String, N> fields);

    /**
     * 要素リストから配列ノードを生成します。
     *
     * @param elements 要素のリスト
     * @return 配列ノード
     */
    N arrayNode(List<N> elements);

    /**
     * Map キーなどのテキスト表現を返します。
     *
     * @param node テキスト化するノード
     * @return テキスト表現
     */
    String nodeToText(N node);

    /**
     * {@link TypeConfigurer} の値（String/Integer/Long/Boolean）を N に変換します。
     *
     * @param value 変換する値（String/Integer/Long/Boolean のいずれか）
     * @return 変換後のノード
     */
    N primitiveToNode(Object value);

    // --- JSON 読み書き ---

    /**
     * オブジェクトを JSON 文字列にシリアライズします。
     *
     * @param value シリアライズするオブジェクト
     * @return JSON 文字列
     * @throws Exception シリアライズ失敗時
     */
    String writeValueAsString(Object value) throws Exception;

    /**
     * JSON 文字列を N にパースします。
     *
     * @param json JSON 文字列
     * @return パース結果のノード
     * @throws Exception パース失敗時
     */
    N readTree(String json) throws Exception;

    /**
     * JSON ファイルを N にパースします。
     *
     * @param file JSON ファイル
     * @return パース結果のノード
     * @throws Exception パース失敗時
     */
    N readTree(File file) throws Exception;

    /**
     * JSON 文字列を型 T に従ってデシリアライズします。
     *
     * @param json JSON 文字列
     * @param type デシリアライズ先の型
     * @return デシリアライズ結果のオブジェクト
     * @throws Exception デシリアライズ失敗時
     */
    Object readValue(String json, T type) throws Exception;

    /**
     * N をインデント付き JSON としてファイルに書き出します。
     *
     * @param file  書き出し先のファイル
     * @param value 書き出すノード
     * @throws Exception 書き出し失敗時
     */
    void writePrettyValue(File file, N value) throws Exception;

    /**
     * N をインデント付き JSON 文字列に変換します（ログ用）。
     *
     * @param node 変換するノード
     * @return インデント付き JSON 文字列
     */
    String toPrettyString(N node);
}
