package com.yisi.stiku.dbtest.entity;

import com.yisi.stiku.common.bean.BaseEntity;

public class TestUser extends BaseEntity<Integer> {
    private Integer sid;

    private String nick;

    private String company;

    private Byte state;

    public Integer getSid() {
        return sid;
    }

    public void setSid(Integer sid) {
        this.sid = sid;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick == null ? null : nick.trim();
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company == null ? null : company.trim();
    }

    public Byte getState() {
        return state;
    }

    public void setState(Byte state) {
        this.state = state;
    }

    @Override
    public Integer getPK() {
        return sid;
    }
}