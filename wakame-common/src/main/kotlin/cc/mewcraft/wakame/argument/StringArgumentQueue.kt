/*
 * This file is part of adventure, licensed under the MIT License.
 *
 * Copyright (c) 2017-2023 KyoriPowered
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package cc.mewcraft.wakame.argument

class StringArgumentQueue(
    private val args: List<String>,
) : ArgumentQueue<String> {
    private var ptr = 0

    override fun pop(): String {
        if (!this.hasNext()) {
            throw IllegalArgumentException("Missing argument!")
        }
        return args[ptr++]
    }

    override fun popOr(errorMessage: String): String {
        if (!this.hasNext()) {
            throw IllegalArgumentException(errorMessage)
        }
        return args[ptr++]
    }

    override fun popOr(errorMessage: () -> String): String {
        if (!this.hasNext()) {
            throw IllegalArgumentException(errorMessage())
        }
        return args[ptr++]
    }

    override fun peek(): String? {
        return if (this.hasNext()) args[ptr] else null
    }

    override fun hasNext(): Boolean {
        return this.ptr < args.size
    }

    override fun reset() {
        this.ptr = 0
    }

    override fun toString(): String {
        return args.toString()
    }
}