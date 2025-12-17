package com.parking.fee.common;

import lombok.Data;

import java.util.List;

/**
 * 分页结果类
 *
 * @author Parking System
 */
@Data
public class PageResult<T> {

    private int current;
    private int size;
    private long total;
    private List<T> records;
    private long pages;

    public PageResult(int current, int size, long total, List<T> records) {
        this.current = current;
        this.size = size;
        this.total = total;
        this.records = records;
        this.pages = (total + size - 1) / size;
    }
}
