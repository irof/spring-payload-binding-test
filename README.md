# spring-boot-demo

Spring Boot の TODO サンプルアプリ (`todo-app`) と、エンドポイントの JSON バインディングを検証する汎用テストツール (`test-tool`) のマルチモジュール構成。

## モジュール

| モジュール | 役割 |
|---|---|
| `todo-app` | Spring Boot Web アプリ。`TodoList` / 検索 / 統計 / コメントの 3 系統エンドポイントを提供 |
| `test-tool` | `@RestController` の `@RequestBody` 引数と戻り値を自動収集して JSON バインディングを検査する汎用ツール。`com.github.irof:json-binding-contract-test` として publish |

## test-tool の使い方

### 1. mavenLocal にインストール

```
./gradlew :test-tool:publishToMavenLocal
```

`~/.m2/repository/com/github/irof/json-binding-contract-test/0.0.1-SNAPSHOT/` に jar / sources / javadoc / pom が生成される。

### 2. 利用側の依存追加

Gradle (`build.gradle.kts`):

```kotlin
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    testImplementation("com.github.irof:json-binding-contract-test:0.0.1-SNAPSHOT")
}
```

Maven (`pom.xml`):

```xml
<dependencies>
    <dependency>
        <groupId>com.github.irof</groupId>
        <artifactId>json-binding-contract-test</artifactId>
        <version>0.0.1-SNAPSHOT</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

Maven は `~/.m2/repository` を既定で検索するため mavenLocal の宣言は不要。

### 3. テストクラスを作成

`JsonBindingContractTestBase` を継承した空のサブクラスを書くだけ。Spring コンテキスト構成は利用側の責務なので `@SpringBootTest` 等は利用側に書く。

```java
@SpringBootTest
class JsonBindingContractTest extends JsonBindingContractTestBase {
}
```

これで `@RestController` の `@RequestBody` 引数の型と戻り値型が自動収集され、それぞれに対して JSON バインディングが検証される。

## 検査内容

エンドポイント毎に **方向別 (REQUEST / RESPONSE)** で検査する。

- **REQUEST**: サンプル JSON を deserialize し、`BeanDescription` の accessor 経由で property レベル再帰的に「null になっていないか」を検査。値の等価性は問わないため、カスタムデシリアライザによる正規化は許容。
- **RESPONSE**: deserialize → serialize → 元 JSON と等価性比較で、シリアライザ出力が期待値と一致するか検証。
- フレームワーク提供のハンドラ (`BasicErrorController` 等) は自動除外。

## 値のバリエーション

各 `(type, direction)` の組に対して、複数のバリエーションが**並行で**実行される（モード切り替えではなく一つのテスト実行で両方検査）。

**ビルトインバリエーション:**

| バリエーション | 生成される JSON | 目的 |
|---|---|---|
| `Variation.SAMPLE` | 全フィールドにサンプル値（"sample", `1`, enum 第一定数 等） | 通常経路のバインディング検査 |
| `Variation.NULL` | 全フィールド `null` の object (`@JsonValue` 型は top-level `null`) | null 受容性の検査 |

**カスタムバリエーション**: `Variation` インタフェースを実装すれば任意のバリエーション (境界値、最小値のみ、特定エラーケース等) を追加できる:

```java
class MinimumValuesVariation implements Variation {
    public String name() { return "minimum"; }
    public JsonNode build(JavaType type, ObjectMapper mapper) {
        // ... カスタムロジック
    }
}
```

**型ごとのバリエーション指定**: `variations(PayloadType)` を override し、各ペイロードに対して実行するバリエーション群を返す。型・方向に応じて自由に組み替え可能 (NULL を受け付けない型はリストから外す、特定型だけカスタムバリエーションを追加する、等)。

```java
@SpringBootTest
class JsonBindingContractTest extends JsonBindingContractTestBase {
    @Override
    protected List<Variation> variations(PayloadType payload) {
        Class<?> raw = payload.type().getRawClass();
        // primitive を含む型は NULL response で round-trip 不可なので除外
        if (payload.direction() == Direction.RESPONSE && (raw == SearchResult.class || raw == TodoStats.class)) {
            return List.of(Variation.SAMPLE);
        }
        // 特定の型だけカスタムバリエーションを足すこともできる
        if (raw == TodoList.class) {
            return List.of(Variation.SAMPLE, Variation.NULL, new MinimumValuesVariation());
        }
        return super.variations(payload);
    }
}
```

## 3 つの実行モード

| モード | 動作 |
|---|---|
| `SAMPLE` (default) | サンプル値を埋めた JSON を生成しメモリ上で検査 |
| `WRITE` | サンプル JSON を `src/test/resources/json-binding/{request,response}/{FQN}/{variation}.json` に書き出し（型毎のディレクトリにバリエーション別ファイル） |
| `VERIFY` | 上記ファイルを読み込み、内容との突き合わせで検査。fixture が無ければ失敗 |

### モードの指定

優先順位: システムプロパティ > サブクラスの `defaultMode()` > `SAMPLE`

```
# システムプロパティで一時上書き
./gradlew test -Djson.binding.mode=WRITE
```

```java
// サブクラスでハードコード既定値
@SpringBootTest
class JsonBindingContractTest extends JsonBindingContractTestBase {
    @Override
    protected Mode defaultMode() {
        return Mode.VERIFY;
    }
}
```

典型的なワークフロー: **VERIFY を既定にして CI で常時検査、fixture 更新時のみ `-Djson.binding.mode=WRITE` で上書き再生成**。

## サンプル値の生成

`SampleJsonFactory` が `JavaType` から再帰的に `JsonNode` を構築する。

- スカラー: `String→"sample"`, 数値→`1`, `enum`→第一定数, `LocalDate→"2024-01-01"`, `UUID`/`Instant`/`URI` 等
- コレクション・配列: 要素 1 個
- Map: 1 エントリ (キー型に応じた値、例えば `Map<Priority,Long>` → `{"LOW": 1}`)
- Bean / record: `BeanDescription` の serializationConfig 由来 property を全て埋める
- 循環参照: `path` Set で検出して `NullNode`

## 実行時ログ

各テストで使用された JSON が INFO ログに pretty-print 出力される。SAMPLE モードでも実際の検査対象を目視確認できる。

```
[SAMPLE][RESPONSE] com.example.demo.todo.TodoList
{
  "id" : "sample",
  "title" : "sample",
  ...
}
```

## 検査対象の絞り込み

- ルート (= `@RequestBody` 引数 / 戻り値) のみを検査対象とし、Bean プロパティを通じた推移的な型は個別検査しない。ルートのサンプル生成時点で内部型は値が埋まりラウンドトリップされるため、別途検査の必要はない。
- `Collection<T>` / `Optional<T>` / `ResponseEntity<T>` 等のコンテナはアンラップして `T` を検査対象とする。
- `java.*` のスカラー / プリミティブ / enum は検査スキップ。

## 利用例（todo-app）

`todo-app` 自身が test-tool の利用例になっている。`JsonBindingContractTest` は `defaultMode() = VERIFY` 固定で、コミット済み fixture (`todo-app/src/test/resources/json-binding/`) と毎回突き合わせる構成。

```
./gradlew :todo-app:test
```

5 つの型 × 方向で計 7 件の動的テストが実行される。

## Maven Central 公開予定

現在は mavenLocal でのみ利用可能。Central 公開時の TODO:

- `test-tool/build.gradle.kts` 内 `TODO:` コメント箇所 (url, developer, scm) を実値に
- `signing` プラグインを追加し GPG 署名 (Central の必須要件)
- 必要なら `io.github.gradle-nexus.publish-plugin` でステージング自動化
