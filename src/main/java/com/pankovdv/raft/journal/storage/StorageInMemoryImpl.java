package com.pankovdv.raft.journal.storage;

import com.pankovdv.raft.journal.operation.Entry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Component
@Slf4j
@RequiredArgsConstructor
class StorageInMemoryImpl implements Storage {

    private final Map<Integer, String> map = new ConcurrentHashMap<>();


    @Override
    public String get(Integer key) {
        return map.get(key);
    }

    @Override
    public void insert(Integer key,
                       String val) {
        map.put(key, val);
    }

    @Override
    public void update(Integer key,
                       String val) {
        map.put(key, val);
    }

    @Override
    public void delete(Integer key) {
        map.remove(key);
    }

    @Override
    public List<Entry> all() {
        return map.entrySet().stream().
                map(entry -> new Entry(entry.getKey(), entry.getValue())).
                collect(Collectors.toList());
    }
}
