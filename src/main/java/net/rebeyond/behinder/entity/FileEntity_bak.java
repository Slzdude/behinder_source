package net.rebeyond.behinder.entity;

import java.sql.Timestamp;

public class FileEntity_bak {
    private Timestamp createTime;
    private Timestamp lastAccessTime;
    private Timestamp lastModifyTime;
    private String name;
    private String permission;
    private long size;
    private int type;

    public String getName() {
        return this.name;
    }

    public void setName(String name2) {
        this.name = name2;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type2) {
        this.type = type2;
    }

    public long getSize() {
        return this.size;
    }

    public void setSize(long size2) {
        this.size = size2;
    }

    public String getPermission() {
        return this.permission;
    }

    public void setPermission(String permission2) {
        this.permission = permission2;
    }

    public Timestamp getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Timestamp createTime2) {
        this.createTime = createTime2;
    }

    public Timestamp getLastModifyTime() {
        return this.lastModifyTime;
    }

    public void setLastModifyTime(Timestamp lastModifyTime2) {
        this.lastModifyTime = lastModifyTime2;
    }

    public Timestamp getLastAccessTime() {
        return this.lastAccessTime;
    }

    public void setLastAccessTime(Timestamp lastAccessTime2) {
        this.lastAccessTime = lastAccessTime2;
    }
}
