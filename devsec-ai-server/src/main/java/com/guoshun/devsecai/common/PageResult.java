package com.guoshun.devsecai.common;

import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {
    private Long total;
    private List<T> records;
    private Long current;
    private Long size;
    
    public static <T> PageResult<T> of(Long total, List<T> records, Long current, Long size) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(total);
        result.setRecords(records);
        result.setCurrent(current);
        result.setSize(size);
        return result;
    }
}
