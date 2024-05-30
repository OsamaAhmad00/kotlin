fun box(stepId: Int): String {
    when (stepId) {
        0 -> if (qux() != 42) return "Fail"
        1 -> if (qux() != 33) return "Fail"
        else -> return "Unknown"
    }
    return "OK"
}

@kotlin.wasm.WasmExport
fun wasi_box(stepId: Int): Int {
    val result = box(stepId)
    if (result != "OK") {
        println("Expected OK but found $result on step $stepId")
        return 0
    } else {
        return 1
    }
}