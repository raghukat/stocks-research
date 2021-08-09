//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.stocks.research.mkvapi_dummy.main;

import com.stocks.research.mkvapi.main.IonRecordListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;

public class EventQueue {
    private static final Logger log = LoggerFactory.getLogger(EventQueue.class);
    private LinkedBlockingQueue<Event> queue = new LinkedBlockingQueue();

    public EventQueue() {
    }

    public void start() {
        Thread t = new Thread(() -> {
            this.dispatch();
        });
        t.setDaemon(true);
        t.setName("DummyEventQ");
        t.start();
    }

    public void postEvent(IonRecordListener listener, Object object, String recordName) {
        try {
            Event event = new Event(listener, object, recordName);
            this.queue.put(event);
        } catch (Exception var5) {
            log.error("failed to add event for [{}]", recordName, var5);
        }

    }

    private void dispatch() {
        try {
            while(true) {
                Event event = (Event)this.queue.take();

                try {
                    log.info("UPDATE EVENT recordName=[{}]", event.recordName);
                    event.listener.onRecordUpdate(event.recordName, event.object);
                } catch (Exception var3) {
                    log.error("exception from onRecordUpdate callback [{}] [{}]", new Object[]{event.recordName, event.object, var3});
                }
            }
        } catch (InterruptedException var4) {
            log.error("InterruptedException from event queue", var4);
        }
    }

    static class Event {
        IonRecordListener listener;
        Object object;
        String recordName;
        EventType eventType;

        public Event(IonRecordListener listener, Object object, String recordName) {
            this.listener = listener;
            this.object = object;
            this.recordName = recordName;
            this.eventType = EventType.pub;
        }
    }

    static enum EventType {
        pub;

        private EventType() {
        }
    }
}
