package com.evgenii.info_table.bean;

import javax.ejb.Singleton;
import javax.faces.push.Push;
import javax.faces.push.PushContext;
import javax.inject.Inject;

@Singleton
public class PushBean {

    @Inject
    @Push(channel = "websocket")
    private PushContext pushContext;

    public void sendMessage(String message) {
        pushContext.send(message);
    }

}