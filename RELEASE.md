RELEASE
==========

```sh
./gradlew clean publish
```

```sh
cd spring-payload-binding-test/build/staging-deploy/
zip -r dist.zip .
```

`dist.zip` を https://central.sonatype.com/publishing からアップロード
