package com.example.admin.giftbox2;

public  class EventManager {

    public interface Event {
        public void onSomethingHappened(boolean msg);
    }
    private static Event mEvent;
    public static  void setEventListener(Event nm){
        mEvent = nm;
    }
    public static void raiseEvent(boolean msg){
        mEvent.onSomethingHappened(msg);
    }
}
