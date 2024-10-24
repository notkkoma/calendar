package com.example.calendar;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "response", strict = false)
public class HolidayResponse {
    @Element(name = "header")
    private Header header;

    @Element(name = "body")
    private Body body;

    public Header getHeader() {
        return header;
    }

    public Body getBody() {
        return body;
    }

    @Root(name = "header", strict = false)
    public static class Header {
        @Element(name = "resultCode")
        private String resultCode;

        @Element(name = "resultMsg")
        private String resultMsg;

        public String getResultCode() {
            return resultCode;
        }

        public String getResultMsg() {
            return resultMsg;
        }
    }

    @Root(name = "body", strict = false)
    public static class Body {
        @Element(name = "items") // items 요소를 포함
        private Items items; // Items 클래스 정의

        public Items getItems() {
            return items;
        }

        @Root(name = "items", strict = false)
        public static class Items {
            @ElementList(name = "item", inline = true) // item 리스트
            private List<Item> item;

            public List<Item> getItem() {
                return item;
            }
        }
    }

    @Root(name = "item", strict = false)
    public static class Item {
        @Element(name = "isHoliday")
        private String isHoliday;

        @Element(name = "locdate")
        private String locdate;

        public String getIsHoliday() {
            return isHoliday;
        }

        public String getLocdate() {
            return locdate;
        }
    }
}