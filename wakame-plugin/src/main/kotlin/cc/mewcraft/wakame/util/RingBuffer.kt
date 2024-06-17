package cc.mewcraft.wakame.util

/**
 * A ring buffer is a fixed-size buffer with a read pointer and a write pointer.
 */
class RingBuffer<T>(
    private val capacity: Int,
) {
    private val buffer: Array<Any?> = arrayOfNulls<Any?>(capacity)
    private var writeIndex = 0
    private var readIndex = 0
    private var size = 0

    fun isEmpty(): Boolean {
        return size == 0
    }

    fun isFull(): Boolean {
        return size == capacity
    }

    fun write(element: T) {
        buffer[writeIndex] = element
        writeIndex = (writeIndex + 1) % capacity
        if (size < capacity) {
            size++
        } else {
            readIndex = (readIndex + 1) % capacity // Rewrite the oldest data
        }
    }

    fun read(): T? {
        if (isEmpty()) {
            return null
        }
        @Suppress("UNCHECKED_CAST")
        val element = buffer[readIndex] as T?
        buffer[readIndex] = null // 清除读取的数据
        readIndex = (readIndex + 1) % capacity
        size--
        return element
    }

    fun peek(): T? {
        if (isEmpty()) {
            return null
        }
        @Suppress("UNCHECKED_CAST")
        return buffer[readIndex] as T?
    }

    fun getSize(): Int {
        return size
    }

    fun getCapacity(): Int {
        return capacity
    }

    fun readAll(): List<T> {
        val result = mutableListOf<T>()
        for (i in 0..size - 1) {
            val index = (readIndex + i) % capacity
            @Suppress("UNCHECKED_CAST")
            result.add(buffer[index] as T)
        }
        return result
    }

    fun clear() {
        writeIndex = 0
        readIndex = 0
        size = 0
    }
}
