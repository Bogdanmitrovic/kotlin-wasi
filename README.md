# kotlin-wasi

Simple kotlin CLI program, writes back input with Wasm received: prepended. 
To run on macOS/linux:
```
./gradlew runWasm
```
To run on Windows:
```
.\gradlew runWasm
```

Wasmtime is downloaded by a gradle task.