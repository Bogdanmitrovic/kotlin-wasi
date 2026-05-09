@file:OptIn(ExperimentalWasmInterop::class)

import kotlin.wasm.WasmImport
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi
import kotlin.wasm.unsafe.withScopedMemoryAllocator

@WasmImport("wasi_snapshot_preview1", "fd_read")
private external fun wasiFdRead(fd: Int, iovs: Int, iovsLength: Int, bytesReadPtr: Int): Int

private fun readLine(): String? {
    val sb = StringBuilder()
    while (true) {
        val b = readByte()
        if (b == -1) return null
        if (b == '\n'.code) {
            if (sb.isNotEmpty() && sb.last() == '\r') sb.deleteAt(sb.length - 1)
            return sb.toString()
        }
        sb.append(b.toChar())
    }
}

@OptIn(UnsafeWasmMemoryApi::class)
private fun readByte(): Int {
    var result = 0
    withScopedMemoryAllocator { allocator ->
        val buffer = allocator.allocate(1)
        val iov = allocator.allocate(8)
        (iov + 0).storeInt(buffer.address.toInt())
        (iov + 4).storeInt(1)
        val bytesReadPtr = allocator.allocate(4)
        val errno = wasiFdRead(0, iov.address.toInt(), 1, bytesReadPtr.address.toInt())
        result = if (errno == 0 && bytesReadPtr.loadInt() > 0) {
            buffer.loadByte().toInt() and 0xFF
        } else -1
    }
    return result
}

fun main() {
    while (true) {
        val line = readLine() ?: break
        println("Wasm received: $line")
    }
}

@WasmExport
fun dummy() {
}