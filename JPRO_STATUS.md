# JPro Integration Status

## Current Situation (Updated after Gradle attempts)

We've attempted multiple approaches to integrate JPro for web-based PR previews, but have run into systematic issues with both the Maven and Gradle plugin distributions.

## Problems Discovered

### Maven Plugin Issue
The JPro Maven plugin version `2024.2.0` (and any 2024.x/2025.x version) cannot be found in the JPro Maven repository at https://sandec.jfrog.io/artifactory/repo/.

**Error Message:**
```
Could not find artifact com.sandec.jpro:jpro-maven-plugin:jar:2024.2.0
in jpro (https://sandec.jfrog.io/artifactory/repo)
```

**Last publicly available version:** `2022.1.8` (likely incompatible with Java 21 and modern JavaFX)

### Gradle Plugin Issue
The JPro Gradle plugin artifact exists in the repository (`one.jpro:jpro-gradle-plugin:2024.4.1` and `2025.1.0`), but the plugin ID is not properly declared in the plugin's metadata.

**Error Message:**
```
Plugin with id 'jpro' not found.
Plugin with id 'one.jpro' not found.
```

**What we tried:**
- ✗ Plugin portal syntax: `id 'one.jpro' version '2025.1.0'`
- ✗ Buildscript classpath + `apply plugin: 'jpro'`
- ✗ Buildscript classpath + `apply plugin: 'one.jpro'`  
- ✗ Using version `2024.4.1` instead of `2025.1.0`

The artifact downloads successfully, but Gradle cannot locate the plugin implementation class or plugin ID declaration within the JAR.


## Root Cause Analysis

JPro appears to have changed their distribution strategy:
1. **Maven plugin:** Stopped public releases after 2022.1.8
2. **Gradle plugin:** Published artifacts but with incomplete/broken plugin metadata
3. **Runtime libs:** Available on Maven Central (jpro-webapi, jpro-client-javafx at 2024.4.0)

This suggests:
- JPro may require a commercial license for current build tools (2024+)
- The public Artifactory repository may be deprecated or misconfigured
- Plugin metadata generation may be broken in their build pipeline

## Current Workaround

All JPro-related build configuration has been **temporarily disabled**:
- Maven: JPro dependencies/plugin/repositories commented out in `pom.xml`
- Gradle: Created `build.gradle` but JPro plugin not working
- Docker: Falls back to basic Maven build without web serving capability

This allows the basic JavaFX application to build successfully, proving our containerization approach works.

## Next Steps to Resolve

### Option 1: Find Correct JPro Version
1. Check JPro's official documentation for available versions
2. Visit https://www.jpro.one/docs/current/2.8/MAVEN_GRADLE to find latest stable version
3. Check their GitHub releases: https://github.com/JPro-one/JPro-Platform

### Option 2: Contact JPro Team
JPro may require a license or special access for certain versions. Contact them at:
- Email: support@jpro.one
- GitHub Issues: https://github.com/JPro-one/JPro-Platform/issues

### Option 3: Use Alternative Version
Based on public information, try these versions (from most recent to oldest):
- `2024.1.0`
- `2023.3.0`
- `2023.2.0`

### Option 4: Alternative Approach
Consider alternatives to JPro:
- **JPro Open** - The open-source version (if available)
- **GraalVM Native Image** with web framework
- **JxBrowser** for embedding web content
- **Vaadin** for web UI (requires rewrite)

## Testing Once Resolved

When you find the correct JPro version:

1. **Uncomment JPro sections in pom.xml**:
   ```bash
   # Search for all commented JPro lines
   grep -n "<!-- JPro" pom.xml
   ```

2. **Update version**:
   ```xml
   <jpro.version>CORRECT_VERSION_HERE</jpro.version>
   ```

3. **Test locally**:
   ```bash
   mvn clean package
   mvn jpro:run
   ```

4. **Test Docker build**:
   ```bash
   docker build -t fxplanner-test .
   docker run -p 8080:8080 fxplanner-test
   ```

5. **Access the app**:
   Open browser to http://localhost:8080

## References

- JPro Documentation: https://www.jpro.one/docs
- JPro Maven Plugin: https://www.jpro.one/docs/current/2.8/MAVEN_GRADLE
- JPro Repository: https://sandec.jfrog.io/ui/repos/tree/General/repo
