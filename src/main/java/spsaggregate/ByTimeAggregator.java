package spsaggregate;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

public class ByTimeAggregator<T extends Timeable> {

    private SortedMap<Long, Map<T, Long>> levelOneMap;
    private final int bufferSize;

    public ByTimeAggregator(int bufferSize) {
        this.bufferSize = bufferSize;
        levelOneMap = Collections.synchronizedSortedMap(new TreeMap<>(Long::compareTo));
    }

    public Optional<Map<T, Long>> put(T value){
        Map<T, Long> innerMap = levelOneMap.getOrDefault(value.getTime(), new HashMap<T, Long>());
        if(innerMap.containsKey(value)) {
            innerMap.computeIfPresent(value, (key, val) -> val + 1);
        } else {
            innerMap.put(value, 1l);
        }
        levelOneMap.put(value.getTime(), innerMap);
        if(levelOneMap.size() < bufferSize){
            return Optional.empty();
        } else {
            Long firstKey = levelOneMap.firstKey();
            Map<T, Long> result = levelOneMap.remove(firstKey);
            return Optional.ofNullable(result);
        }
    }

    public Collection<Map<T, Long>> releaseAll(){
        return levelOneMap.values();
    }
}
