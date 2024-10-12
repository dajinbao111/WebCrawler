package com.github.dajinbao.apiserver.common.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class Page<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int INIT_VALUE = -1;

    /**
     * 当前页数据。
     */
    private List<T> records = Collections.emptyList();

    /**
     * 当前页码。
     */
    private long pageNo = 1;

    /**
     * 每页数据数量。
     */
    private long pageSize = 20;

    /**
     * 总页数。
     */
    private long totalPage = INIT_VALUE;

    /**
     * 总数据数量。
     */
    private long totalCount = INIT_VALUE;

    /**
     * 是否优化分页查询 COUNT 语句。
     */
    private boolean optimizeCountQuery = true;

    /**
     * 创建分页对象。
     *
     * @param pageNo   当前页码
     * @param pageSize 每页数据数量
     * @param <T>      数据类型
     * @return 分页对象
     */
    public static <T> Page<T> of(Number pageNo, Number pageSize) {
        return new Page<>(pageNo, pageSize);
    }

    /**
     * 创建分页对象。
     *
     * @param pageNo   当前页码
     * @param pageSize 每页数据数量
     * @param totalCount 总数据数量
     * @param <T>      数据类型
     * @return 分页对象
     */
    public static <T> Page<T> of(Number pageNo, Number pageSize, Number totalCount) {
        return new Page<>(pageNo, pageSize, totalCount);
    }

    /**
     * 创建分页对象。
     */
    public Page() {
    }

    /**
     * 创建分页对象。
     *
     * @param pageNo   当前页码
     * @param pageSize 每页数据数量
     */
    public Page(Number pageNo, Number pageSize) {
        this.setPageNo(pageNo.longValue());
        this.setPageSize(pageSize.longValue());
    }

    /**
     * 创建分页对象。
     *
     * @param pageNo   当前页码
     * @param pageSize 每页数据数量
     * @param totalCount 总数居数量
     */
    public Page(Number pageNo, Number pageSize, Number totalCount) {
        this.setPageNo(pageNo.longValue());
        this.setPageSize(pageSize.longValue());
        this.setTotalCount(totalCount.longValue());
    }

    /**
     * 创建分页对象。
     *
     * @param records  当前页数据
     * @param pageNo   当前页码
     * @param pageSize 每页数据数量
     * @param totalCount 总数居数量
     */
    public Page(List<T> records, Number pageNo, Number pageSize, Number totalCount) {
        this.setRecords(records);
        this.setPageNo(pageNo.longValue());
        this.setPageSize(pageSize.longValue());
        this.setTotalCount(totalCount.longValue());
    }

    /**
     * 获取当前页的数据。
     *
     * @return 当前页的数据
     */
    public List<T> getRecords() {
        return records;
    }

    /**
     * 设置当前页的数据。
     *
     * @param records 当前页的数据
     */
    public void setRecords(List<T> records) {
        if (records == null) {
            records = Collections.emptyList();
        }
        this.records = records;
    }

    /**
     * 获取当前页码。
     *
     * @return 页码
     */
    public long getPageNo() {
        return pageNo;
    }

    /**
     * 设置当前页码。
     *
     * @param pageNo 页码
     */
    public void setPageNo(long pageNo) {
        if (pageNo < 1) {
            throw new IllegalArgumentException("pageNo must greater than or equal 1，current value is: " + pageNo);
        }
        this.pageNo = pageNo;
    }

    /**
     * 获取当前每页数据数量。
     *
     * @return 每页数据数量
     */
    public long getPageSize() {
        return pageSize;
    }

    /**
     * 设置当前每页数据数量。
     *
     * @param pageSize 每页数据数量
     */
    public void setPageSize(long pageSize) {
        if (pageSize < 0) {
            throw new IllegalArgumentException("pageSize must greater than or equal 0，current value is: " + pageSize);
        }
        this.pageSize = pageSize;
        this.calcTotalPage();
    }

    /**
     * 获取数据总数。
     *
     * @return 数据总数
     */
    public long getTotalPage() {
        return totalPage;
    }

    /**
     * 设置总页数。
     *
     * @param totalPage 总页数
     */
    public void setTotalPage(long totalPage) {
        this.totalPage = totalPage;
    }

    /**
     * 获取数据总数。
     *
     * @return 数据总数
     */
    public long getTotalCount() {
        return totalCount;
    }

    /**
     * 设置数据总数。
     *
     * @param totalCount 数据总数
     */
    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
        this.calcTotalPage();
    }

    /**
     * 计算总页码。
     */
    private void calcTotalPage() {
        if (pageSize < 0 || totalCount < 0) {
            totalPage = INIT_VALUE;
        } else {
            totalPage = totalCount % pageSize == 0 ? (totalCount / pageSize) : (totalCount / pageSize + 1);
        }
    }

    /**
     * 当前页是否有记录（有内容）。
     *
     * @return {@code true} 有内容，{@code false} 没有内容
     */
    public boolean hasRecords() {
        return getTotalCount() > 0 && getPageNo() <= getTotalPage();
    }

    /**
     * 是否存在下一页。
     *
     * @return {@code true} 存在下一页，{@code false} 不存在下一页
     */
    public boolean hasNext() {
        return getTotalPage() != 0 && getPageNo() < getTotalPage();
    }

    /**
     * 是否存在上一页。
     *
     * @return {@code true} 存在上一页，{@code false} 不存在上一页
     */
    public boolean hasPrevious() {
        return getPageNo() > 1;
    }

    /**
     * 获取当前分页偏移量。
     *
     * @return 偏移量
     */
    public long offset() {
        return getPageSize() * (getPageNo() - 1);
    }

}