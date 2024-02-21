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

/**
 * A queue of arguments.
 */
interface ArgumentQueue<T> {
    /**
     * Pop an argument, throwing an exception if no argument was present.
     *
     * After an invocation of `pop()`, the internal argument pointer will be
     * advanced to the next argument.
     *
     * @return the popped argument
     */
    fun pop(): T

    /**
     * Pop an argument, throwing an exception if no argument was present.
     *
     * After an invocation of `popOr()`, the internal argument pointer will be
     * advanced to the next argument.
     *
     * @param errorMessage the error to throw if the argument is not present
     * @return the popped argument
     */
    fun popOr(errorMessage: String): T

    /**
     * Pop an argument, throwing an exception if no argument was present.
     *
     * After an invocation of `popOr()`, the internal argument pointer will be
     * advanced to the next argument.
     *
     * @param errorMessage the error to throw if the argument is not present
     * @return the popped argument
     */
    fun popOr(errorMessage: () -> String): T

    /**
     * Peek at the next argument without advancing the iteration pointer.
     *
     * @return the next argument, if any is available.
     */
    fun peek(): T?

    /**
     * Get whether another argument is available to be popped.
     *
     * @return whether another argument is available
     */
    fun hasNext(): Boolean

    /**
     * Reset index to the beginning, to begin another attempt.
     */
    fun reset()
}