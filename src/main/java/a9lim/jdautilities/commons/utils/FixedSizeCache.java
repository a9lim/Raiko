// Copyright 2023 Aidan Lim (a9lim) <aidanlim192@gmail.com>
// Copyright 2016-2018 John Grosh (jagrosh) <john.a.grosh@gmail.com> & Kaidan Gustave (TheMonitorLizard).
//
// This file is part of Raiko.
//
// Raiko is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as
// published by the Free Software Foundation, either version 3 of the
// License, or (at your option) any later version.
//
// Raiko is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with Raiko. If not, see <http://www.gnu.org/licenses/>.

package a9lim.jdautilities.commons.utils;

import java.util.HashMap;
import java.util.Map;

public class FixedSizeCache<K, V> {
    private final Map<K, V> map;
    private final K[] keys;
    private int currIndex;

    @SuppressWarnings("unchecked")
    public FixedSizeCache(int size) {
        map = new HashMap<>();
        if (size < 1)
            throw new IllegalArgumentException("Cache size must be at least 1!");
        keys = (K[]) new Object[size];
    }

    public void add(K key, V value) {
        if (keys[currIndex] != null)
            map.remove(keys[currIndex]);
        map.put(key, value);
        keys[currIndex] = key;
        currIndex = (currIndex + 1) % keys.length;
    }

    public boolean contains(K key) {
        return map.containsKey(key);
    }

    public V get(K key) {
        return map.get(key);
    }
}
