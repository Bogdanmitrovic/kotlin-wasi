fun main() {
    while (true) {
        val line = readln()
        println("Wasm received: $line")
    }
}

@WasmExport
fun dummy() {}