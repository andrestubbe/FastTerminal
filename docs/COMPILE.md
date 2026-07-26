# Building from Source

## Prerequisites

- JDK 17+
- Maven 3.9+
- **Windows:** Visual Studio 2019+ or Build Tools

## Build

### Windows

```bash
compile.bat
mvn clean package
```

The build script compiles the native library and packages it with the JAR.

## Run Examples

```bash
run-demo.bat
```

## Installation

### JitPack (Recommended)

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastTerminal</artifactId>
        <version>0.1.8</version>
    </dependency>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastCore</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

### Gradle (JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:FastTerminal:0.1.8'
    implementation 'com.github.andrestubbe:FastCore:0.1.0'
}
```

## Download Pre-built JAR

See [Releases Page](https://github.com/andrestubbe/FastTerminal/releases)

## Troubleshooting

### JNI UnsatisfiedLinkError

If you get `UnsatisfiedLinkError`, the native library was not found:

1. Ensure `compile.bat` was run successfully
2. Check that the DLL exists in the `build/` folder
3. Ensure you have VS C++ Redistributable installed
