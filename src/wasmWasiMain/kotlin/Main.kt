@file:OptIn(ExperimentalWasmInterop::class)

import kotlin.wasm.WasmImport
import kotlin.wasm.unsafe.Pointer
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi
import kotlin.wasm.unsafe.withScopedMemoryAllocator

@WasmImport("wasi_snapshot_preview1", "fd_read")
private external fun wasiFdRead(fd: Int, iovs: Int, iovsLength: Int, bytesReadPtr: Int): Int

@OptIn(UnsafeWasmMemoryApi::class)
fun readFromStdin(): String? = withScopedMemoryAllocator { allocator ->
    val bufSize = 1024
    val buffer = allocator.allocate(bufSize)
    val iov = allocator.allocate(8)
    val bytesReadPtr = allocator.allocate(4)

    Pointer(iov.address).storeInt(buffer.address.toInt())
    Pointer((iov.address + 4u)).storeInt(bufSize)

    val err = wasiFdRead(0, iov.address.toInt(), 1, bytesReadPtr.address.toInt())
    if (err != 0) return@withScopedMemoryAllocator null
    val bytesRead = Pointer(bytesReadPtr.address).loadInt()
    if (bytesRead == 0) return@withScopedMemoryAllocator null

    val bytes = ByteArray(bytesRead)
    for (i in 0 until bytesRead) {
        bytes[i] = Pointer((buffer.address + i.toUInt())).loadByte()
    }
    bytes.decodeToString()
}

@OptIn(UnsafeWasmMemoryApi::class)
private fun readByte(): Int {
    var result : Int = 0
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
        //val line = readFromStdin() ?: break
        //println("Wasm received: $line")
        val b = readByte()
        if (b == -1) break
        print(b.toChar())
    }
}