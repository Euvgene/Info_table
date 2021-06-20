package com.evgenii.info_table.bean;

import javax.ejb.Singleton;
import javax.faces.push.Push;
import javax.faces.push.PushContext;
import javax.inject.Inject;

/**
 * PushBean class to send message on page.
 *
 * @author Boznyakov Evgenii
 */
@Singleton
public class PushBean {

    /**
     * Creates an instance of this class using constructor-based dependency injection.
     */
    @Inject
    @Push(channel = "websocket")
    private PushContext pushContext;

    /**
     * Send message to statistic page
     */
    public void sendMessage(String message) {
        pushContext.send(message);
    }

}