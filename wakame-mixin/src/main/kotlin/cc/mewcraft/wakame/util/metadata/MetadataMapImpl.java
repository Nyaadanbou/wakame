/*
 * This file is part of helper, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package cc.mewcraft.wakame.util.metadata;

import com.google.common.collect.ImmutableMap;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

@NullMarked
final class MetadataMapImpl implements MetadataMap {
    private final Map<MetadataKey<?>, Object> map = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Reused lookup result holder to avoid allocations on hot paths. Guarded by {@link #lock}.
     * */
    private final Lookup lookup = new Lookup();

    /**
     * Mutable lookup result. All fields are only valid when a lookup method returned true.
     * Guarded by {@link #lock}.
     */
    private static final class Lookup {
        @Nullable MetadataKey<?> storedKey;
        @Nullable Object value; // always unboxed
        @Nullable Iterator<Map.Entry<MetadataKey<?>, Object>> iterator;

        void clear() {
            this.storedKey = null;
            this.value = null;
            this.iterator = null;
        }
    }

    /**
     * Finds an entry matching {@code needle} by id (MetadataKey equality), while performing opportunistic cleanup
     * of expired {@link TransientValue}s.
     *
     * <p>On success, populates {@link #lookup} with the stored key, unboxed value, and the iterator positioned at
     * the found entry (so callers may remove via {@code lookup.iterator.remove()}).</p>
     */
    private boolean findEntry(MetadataKey<?> needle) {
        this.lookup.clear();

        Iterator<Map.Entry<MetadataKey<?>, Object>> it = this.map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<MetadataKey<?>, Object> kv = it.next();

            Object value = kv.getValue();
            if (value instanceof TransientValue<?> transientValue) {
                Object unboxed = transientValue.getOrNull();

                // expired
                if (unboxed == null) {
                    it.remove();
                    continue;
                }

                if (kv.getKey().equals(needle)) {
                    this.lookup.storedKey = kv.getKey();
                    this.lookup.value = unboxed;
                    this.lookup.iterator = it;
                    return true;
                }
            } else {
                if (kv.getKey().equals(needle)) {
                    this.lookup.storedKey = kv.getKey();
                    this.lookup.value = value;
                    this.lookup.iterator = it;
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Ensures the located entry key has the same type as the requested key.
     */
    private static void ensureTypeMatch(MetadataKey<?> requested, @Nullable MetadataKey<?> stored) {
        if (stored != null && !requested.getType().equals(stored.getType())) {
            throw new ClassCastException(
                "Cannot cast key with id " + requested.getId() +
                    " with type " + requested.getType().getRawType() +
                    " to existing stored type " + stored.getType().getRawType()
            );
        }
    }

    @Override
    public <T> void put(MetadataKey<T> key, T value) {
        internalPut(key, value);
    }

    @Override
    public <T> void put(MetadataKey<T> key, TransientValue<T> value) {
        internalPut(key, value);
    }

    private void internalPut(MetadataKey<?> key, Object value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");

        this.lock.lock();
        try {
            MetadataKey<?> existing = null;
            for (MetadataKey<?> k : this.map.keySet()) {
                if (k.equals(key)) {
                    existing = k;
                    break;
                }
            }

            if (existing != null && !existing.getType().equals(key.getType())) {
                throw new ClassCastException("Cannot cast key with id " + key.getId() + " with type " + key.getType().getRawType() + " to existing stored type " + existing.getType().getRawType());
            }

            this.map.put(key, value);

        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public <T> void forcePut(MetadataKey<T> key, T value) {
        internalForcePut(key, value);
    }

    @Override
    public <T> void forcePut(MetadataKey<T> key, TransientValue<T> value) {
        internalForcePut(key, value);
    }

    private void internalForcePut(MetadataKey<?> key, Object value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");

        this.lock.lock();
        try {
            this.map.put(key, value);
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public <T> boolean putIfAbsent(MetadataKey<T> key, T value) {
        return internalPutIfAbsent(key, value);
    }

    @Override
    public <T> boolean putIfAbsent(MetadataKey<T> key, TransientValue<T> value) {
        return internalPutIfAbsent(key, value);
    }

    private boolean internalPutIfAbsent(MetadataKey<?> key, Object value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");

        this.lock.lock();
        try {
            cleanup();
            return this.map.putIfAbsent(key, value) == null;
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public <T> Optional<T> get(MetadataKey<T> key) {
        Objects.requireNonNull(key, "key");

        this.lock.lock();
        try {
            if (!findEntry(key)) {
                return Optional.empty();
            }

            ensureTypeMatch(key, this.lookup.storedKey);
            return Optional.of(key.cast(Objects.requireNonNull(this.lookup.value)));
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public <T> boolean ifPresent(MetadataKey<T> key, Consumer<? super T> action) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(action, "action");
        Optional<T> opt = get(key);
        if (opt.isEmpty()) {
            return false;
        }

        action.accept(opt.get());
        return true;
    }

    @Override
    public <T> T getOrNull(MetadataKey<T> key) {
        Objects.requireNonNull(key, "key");
        return get(key).orElse(null);
    }

    @Override
    public <T> T getOrDefault(MetadataKey<T> key, T def) {
        Objects.requireNonNull(key, "key");
        return get(key).orElse(def);
    }

    @Override
    public <T> T getOrPut(MetadataKey<T> key, Supplier<? extends T> def) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(def, "def");

        this.lock.lock();
        try {
            if (!findEntry(key)) {
                T t = def.get();
                Objects.requireNonNull(t, "supplied def");

                this.map.put(key, t);
                return t;
            }

            ensureTypeMatch(key, this.lookup.storedKey);
            return key.cast(Objects.requireNonNull(this.lookup.value));
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public <T> T getOrPutExpiring(MetadataKey<T> key, Supplier<? extends TransientValue<T>> def) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(def, "def");

        this.lock.lock();
        try {
            if (!findEntry(key)) {
                TransientValue<T> t = def.get();
                Objects.requireNonNull(t, "supplied def");

                T value = t.getOrNull();
                if (value == null) {
                    throw new IllegalArgumentException("Transient value already expired: " + t);
                }

                this.map.put(key, t);
                return value;
            }

            ensureTypeMatch(key, this.lookup.storedKey);
            return key.cast(Objects.requireNonNull(this.lookup.value));
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public boolean has(MetadataKey<?> key) {
        Objects.requireNonNull(key, "key");

        this.lock.lock();
        try {
            return findEntry(key) && Objects.requireNonNull(this.lookup.storedKey).getType().equals(key.getType());
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public <T> @Nullable T remove(MetadataKey<T> key) {
        Objects.requireNonNull(key, "key");

        this.lock.lock();
        try {
            if (!findEntry(key)) {
                return null;
            }

            ensureTypeMatch(key, this.lookup.storedKey);
            Objects.requireNonNull(this.lookup.iterator).remove();
            return key.cast(Objects.requireNonNull(this.lookup.value));
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public void clear() {
        this.lock.lock();
        try {
            this.map.clear();
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public ImmutableMap<MetadataKey<?>, Object> asMap() {
        this.lock.lock();
        try {
            return ImmutableMap.copyOf(this.map);
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        this.lock.lock();
        try {
            cleanup();
            return this.map.isEmpty();
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public void cleanup() {
        this.lock.lock();
        try {
            this.map.values().removeIf(o -> o instanceof TransientValue<?> tv && tv.shouldExpire());
        } finally {
            this.lock.unlock();
        }
    }

}
