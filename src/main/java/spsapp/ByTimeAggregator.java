package spsapp;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ByTimeAggregator<T extends Timeable> {

    private static final int BUFFER_SIZE = 4;
    private SortedMap<Long, Map<T, Long>> levelOneMap = Collections.synchronizedSortedMap(new TreeMap<>(Long::compareTo));
    //ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public Map<T, Long> put(T value){
        Map<T, Long> innerMap = levelOneMap.getOrDefault(value.getTime(), new HashMap<T, Long>());
        if(innerMap.containsKey(value)) {
            innerMap.computeIfPresent(value, (key, val) -> val + 1);
        } else {
            innerMap.put(value, 1l);
        }
        //lock.writeLock().lock();
        levelOneMap.put(value.getTime(), innerMap);
        //System.out.println("Time: " + value.getTime());
        //lock.writeLock().unlock();
        if(levelOneMap.size() < BUFFER_SIZE){
            return null;
        } else {
            //lock.readLock().lock();
            Long firstKey = levelOneMap.firstKey();
            //lock.readLock().unlock();
            //lock.readLock().lock();
            Map<T, Long> result = levelOneMap.remove(firstKey);
            //lock.readLock().unlock();
            return result;
        }
    }

    public Collection<Map<T, Long>> releaseAll(){
        return levelOneMap.values();
    }

}
