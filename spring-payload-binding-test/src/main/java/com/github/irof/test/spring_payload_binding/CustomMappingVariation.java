package com.github.irof.test.spring_payload_binding;

import java.util.Map;
import java.util.function.Supplier;

/**
 * 型別カスタム値を持つバリエーションです。
 * {@link EngineVariation#customMapping} で生成されます。
 *
 * @param name         バリエーション名（fixture ファイル名に使用）
 * @param base         エンジンの動作（sample/null/empty）を決めるベースバリエーション
 * @param customValues 型をキーとする値サプライヤのマップ
 */
public record CustomMappingVariation(String name, EngineVariation base, Map<Class<?>, Supplier<?>> customValues)
        implements EngineVariation {
}
