# Build & Deploy Guide

## 🔨 Build Library

### 1. Build và Install vào Local Maven Repository

```bash
mvn clean install
```

Sau khi build thành công, library sẽ được install vào:
```
~/.m2/repository/com/theblood/minio/0.0.1-SNAPSHOT/
```

### 2. Skip Tests (nếu cần build nhanh)

```bash
mvn clean install -DskipTests
```

### 3. Build với Tests

```bash
mvn clean test
mvn clean install
```

## 📦 Artifacts được tạo

Sau khi build, bạn sẽ có:

1. **minio-0.0.1-SNAPSHOT.jar** - Library chính
2. **minio-0.0.1-SNAPSHOT-sources.jar** - Source code (để debug)
3. **pom.xml** - Metadata và dependencies

## 🚀 Sử dụng trong Dự án Khác

### Bước 1: Thêm Dependency

Thêm vào `pom.xml` của dự án:

```xml
<dependency>
    <groupId>com.theblood</groupId>
    <artifactId>minio</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### Bước 2: Cấu hình

Tạo `application.properties`:

```properties
minio.endpoint=http://localhost:9000
minio.access-key=minioadmin
minio.secret-key=minioadmin
minio.bucket=my-bucket
```

### Bước 3: Sử dụng

```java
@Service
@RequiredArgsConstructor
public class FileService {
    
    private final MinioService minioService;
    
    public String uploadFile(MultipartFile file) {
        return minioService.upload(file, "uploads");
    }
}
```

## 🏢 Deploy lên Maven Repository (Optional)

### Deploy lên Nexus/Artifactory

Thêm vào `pom.xml`:

```xml
<distributionManagement>
    <repository>
        <id>releases</id>
        <url>http://your-nexus-server/repository/maven-releases/</url>
    </repository>
    <snapshotRepository>
        <id>snapshots</id>
        <url>http://your-nexus-server/repository/maven-snapshots/</url>
    </snapshotRepository>
</distributionManagement>
```

Thêm credentials vào `~/.m2/settings.xml`:

```xml
<settings>
    <servers>
        <server>
            <id>releases</id>
            <username>your-username</username>
            <password>your-password</password>
        </server>
        <server>
            <id>snapshots</id>
            <username>your-username</username>
            <password>your-password</password>
        </server>
    </servers>
</settings>
```

Deploy:

```bash
mvn clean deploy
```

### Deploy lên GitHub Packages

Thêm vào `pom.xml`:

```xml
<distributionManagement>
    <repository>
        <id>github</id>
        <name>GitHub Packages</name>
        <url>https://maven.pkg.github.com/YOUR_USERNAME/YOUR_REPO</url>
    </repository>
</distributionManagement>
```

Deploy:

```bash
mvn clean deploy
```

## 🔄 Update Version

### Cập nhật version trong pom.xml

```xml
<version>0.0.2-SNAPSHOT</version>
```

Hoặc dùng Maven Versions Plugin:

```bash
# Set version mới
mvn versions:set -DnewVersion=0.0.2-SNAPSHOT

# Commit version
mvn versions:commit

# Hoặc rollback nếu sai
mvn versions:revert
```

### Release Version

```bash
# Chuyển từ SNAPSHOT sang release
mvn versions:set -DnewVersion=1.0.0
mvn clean install
mvn deploy

# Tạo tag git
git tag v1.0.0
git push origin v1.0.0

# Bump lên version tiếp theo
mvn versions:set -DnewVersion=1.0.1-SNAPSHOT
```

## 🧪 Verify Build

### Kiểm tra JAR đã được tạo

```bash
# List files trong target
ls -la target/

# Kiểm tra nội dung JAR
jar tf target/minio-0.0.1-SNAPSHOT.jar

# Kiểm tra trong local repo
ls -la ~/.m2/repository/com/theblood/minio/0.0.1-SNAPSHOT/
```

### Kiểm tra Dependencies

```bash
# Xem dependency tree
mvn dependency:tree

# Analyze dependencies
mvn dependency:analyze
```

## 🐛 Troubleshooting

### Build Failed - Compilation Error

```bash
# Clean và rebuild
mvn clean compile

# Với debug info
mvn clean compile -X
```

### Test Failed

```bash
# Chạy test với debug
mvn test -X

# Chạy specific test
mvn test -Dtest=MinioApplicationTests

# Skip tests
mvn install -DskipTests
```

### Dependency Conflict

```bash
# Xem dependency tree
mvn dependency:tree

# Resolve conflicts
mvn dependency:resolve

# Force update
mvn clean install -U
```

### Cannot Find Library trong Dự án Khác

1. Kiểm tra library đã được install:
   ```bash
   ls ~/.m2/repository/com/theblood/minio/
   ```

2. Force update trong dự án sử dụng:
   ```bash
   mvn clean install -U
   ```

3. Xóa cache và rebuild:
   ```bash
   rm -rf ~/.m2/repository/com/theblood/minio/
   mvn clean install
   ```

## 📋 Checklist trước khi Release

- [ ] All tests pass: `mvn test`
- [ ] Code compiles: `mvn clean compile`
- [ ] No dependency issues: `mvn dependency:analyze`
- [ ] Documentation updated
- [ ] Version bumped
- [ ] CHANGELOG updated
- [ ] Git tag created

## 🔧 CI/CD Integration

### GitHub Actions Example

```yaml
name: Build and Deploy

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v2
    
    - name: Set up JDK 17
      uses: actions/setup-java@v2
      with:
        java-version: '17'
        distribution: 'temurin'
        
    - name: Build with Maven
      run: mvn clean install
      
    - name: Run tests
      run: mvn test
      
    - name: Deploy to GitHub Packages
      if: github.ref == 'refs/heads/main'
      run: mvn deploy
      env:
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

## 📝 Notes

- Library này là **SNAPSHOT version** - dùng cho development
- Để production, nên release stable version (không có -SNAPSHOT)
- Auto-configuration sẽ tự động load khi import vào dự án Spring Boot
- Không cần thêm `@ComponentScan` cho package `com.theblood.minio`
