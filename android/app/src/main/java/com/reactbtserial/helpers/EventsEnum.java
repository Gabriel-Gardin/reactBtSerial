package com.reactbtserial.helpers;

public enum EventsEnum {
    UNBLOCK_COMMAND_EXECUTED(1),
    UNBLOCK_COMMAND_NOT_EXECUTED(2),
    IGNITION_ON(3),
    IGNITION_OFF(4),
    CONNECTION_CLOSED(5),
    CONNECTION_ERROR(6),
    COMMAND_SENT(7),
    COMMAND_NOT_SENT(8);

    private int eventsCode;

    EventsEnum(int eventsCode) {
        this.eventsCode = eventsCode;
    }

    public int getEventsCode() {
        return this.eventsCode;
    }
}
