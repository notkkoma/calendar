package com.example.calendar;

public class Holiday {
    private String date;  // 공휴일 날짜
    private String name;  // 공휴일 이름

    // date 필드에 대한 getter
    public String getDate() {
        return date;
    }

    // date 필드에 대한 setter
    public void setDate(String date) {
        this.date = date;
    }

    // name 필드에 대한 getter
    public String getName() {
        return name;
    }

    // name 필드에 대한 setter
    public void setName(String name) {
        this.name = name;
    }
}