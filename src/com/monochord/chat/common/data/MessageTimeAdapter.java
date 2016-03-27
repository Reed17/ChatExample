package com.monochord.chat.common.data;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

final class MessageTimeAdapter extends XmlAdapter<String, LocalTime> {

    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public LocalTime unmarshal(String timeStr) throws Exception {
        return LocalTime.parse(timeStr, TIME_FORMAT);
    }

    @Override
    public String marshal(LocalTime time) throws Exception {
        return time.format(TIME_FORMAT);
    }
}
