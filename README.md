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

各ペイロード型に対して deserialize → serialize → 元 JSON と等価性比較というラウンドトリップ検査を行う。リクエスト/レスポンスの区別はせず、エンドポイントから収集された型集合に対して一律に検査する (同じ型が複数エンドポイントで使われる場合は1回だけ検査され、エラーメッセージに利用エンドポイント一覧が表示される)。

- フレームワーク提供のハンドラ (`BasicErrorController` 等) は自動除外。
- `Resource` / `MultipartFile` 等のフレームワーク型 (`org.springframework.*` / `jakarta.*` / `javax.*`) も自動除外。

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

**型ごとのバリエーション指定**: `variations(PayloadType)` を override し、各ペイロードに対して実行するバリエーション群を返す。型ごとに自由に組み替え可能 (NULL を受け付けない型はリストから外す、特定型だけカスタムバリエーションを追加する、等)。

```java
@SpringBootTest
class JsonBindingContractTest extends JsonBindingContractTestBase {
    @Override
    protected List<Variation> variations(PayloadType payload) {
        Class<?> raw = payload.type().getRawClass();
        // primitive を含む型は NULL variation で round-trip 不可なので除外
        if (raw == SearchResult.class || raw == TodoStats.class) {
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

## fixture ファイルの扱い

各 (型, バリエーション) について、`src/test/resources/json-binding/{FQN}/{variation}.json` の有無で動作が自動切替される。

- **ファイルあり**: そのファイルの JSON を source として読み込みラウンドトリップ検査
- **ファイルなし**: `Variation.build()` でその場生成してラウンドトリップ検査

モード指定は不要。fixture を pin したい型・バリエーションだけファイル化すればよい。Pin されていないものは毎回 build される。

fixture を新規作成・更新したい時は実行ログに pretty-print 出力された JSON を該当パスに保存する。ファイルを削除すれば次回から build に戻る。

## サンプル値の生成

`SampleJsonFactory` が `JavaType` から再帰的に `JsonNode` を構築する。

- スカラー: `String→"sample"`, 数値→`1`, `enum`→第一定数, `LocalDate→"2024-01-01"`, `UUID`/`Instant`/`URI` 等
- コレクション・配列: 要素 1 個
- Map: 1 エントリ (キー型に応じた値、例えば `Map<Priority,Long>` → `{"LOW": 1}`)
- Bean / record: `BeanDescription` の serializationConfig 由来 property を全て埋める
- 循環参照: `path` Set で検出して `NullNode`

## 実行時ログ

各テストで使用された JSON が INFO ログに pretty-print 出力される (build か file かの出自も併記)。

```
[sample] com.example.demo.todo.TodoList (built)
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

6 つの型 × バリエーション (SAMPLE / NULL のうち適用される分) で複数の動的テストが実行される。

## Maven Central 公開予定

現在は mavenLocal でのみ利用可能。Central 公開時の TODO:

- `test-tool/build.gradle.kts` 内 `TODO:` コメント箇所 (url, developer, scm) を実値に
- `signing` プラグインを追加し GPG 署名 (Central の必須要件)
- 必要なら `io.github.gradle-nexus.publish-plugin` でステージング自動化
