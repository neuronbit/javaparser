# 本地

1. 修改代码
2. 运行PrepareSourceCode
3. cd到第二步生成的文件夹
4. 运行 `mvnw clean install -DskipTests=true`

# maven仓库

## Settings.xml

```xml
<servers>
    <server>
        <id>ossrh</id>
        <username>{replace_it}</username>
        <password>{replace_it}</password>
    </server>
</servers>
<profile>
<id>ossrh</id>
<activation>
    <activeByDefault>true</activeByDefault>
</activation>
<properties>
    <gpg.executable>{replace_it}</gpg.executable>
    <gpg.passphrase>{replace_it}</gpg.passphrase>
</properties>
</profile>
```

## command
指定 profile 
```bash
mvnw clean deploy -DskipTests=true -Prun-release
```
