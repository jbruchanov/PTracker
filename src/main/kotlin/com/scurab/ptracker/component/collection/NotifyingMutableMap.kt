package com.scurab.ptracker.component.collection

class NotifyingMutableMap<K, V>(
    private val map: MutableMap<K, V>,
    onChange: (K) -> Unit
) : MutableMap<K, V> {
    private var triggerChanges: Boolean = true

    private val onChange: (K) -> Unit = {
        if (triggerChanges) {
            onChange(it)
        }
    }

    override val size: Int get() = map.size
    override fun containsKey(key: K): Boolean = map.containsKey(key)
    override fun containsValue(value: V): Boolean = map.containsValue(value)
    override fun get(key: K): V? = map[key]
    override fun isEmpty(): Boolean = map.isEmpty()
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>> get() = map.entries
    override val keys: MutableSet<K> get() = map.keys
    override val values: MutableCollection<V> get() = map.values
    override fun clear() {
        val preKeys = keys
        map.clear()
        preKeys.forEach(onChange)
    }

    override fun put(key: K, value: V): V? {
        return map.put(key, value).also { onChange(key) }
    }

    override fun putAll(from: Map<out K, V>) {
        map.putAll(from)
        from.keys.forEach(onChange)
    }

    override fun remove(key: K): V? {
        return map.remove(key).also { onChange(key) }
    }

    fun withSilentChanges(block: () -> Unit) {
        triggerChanges = false
        block()
        triggerChanges = true
    }
}
