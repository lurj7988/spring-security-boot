package com.original.security.dto;

import java.util.List;

/**
 * 分页结果封装类。
 *
 * @param <T> 数据类型
 * @author bmad
 * @since 0.1.0
 */
public class PageResult<T> {

    /**
     * 当前页，从 1 开始
     */
    private int page;

    /**
     * 每页数量
     */
    private int size;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 当前页数据列表
     */
    private List<T> list;

    public PageResult() {
    }

    public PageResult(int page, int size, long total, List<T> list) {
        this.page = page;
        this.size = size;
        this.total = total;
        this.list = list;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
