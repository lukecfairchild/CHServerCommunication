/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.extensions.chsc;

import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.events.AbstractEvent;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import com.laytonsmith.core.events.Prefilters;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;
import java.util.Map;

/**
 *
 * @author import
 */
public class Events {

    private static void fireEvent(DaemonManager daemon, final BindableEvent event, final String name) {
        if (daemon == null) {
            return;
        }
        
        StaticLayer.GetConvertor().runOnMainThreadLater(daemon, new Runnable() {
            public void run() {
                EventUtils.TriggerListener(Driver.EXTENSION, name, event);
            }
        });
    }

    public static void fireReceived(DaemonManager daemon, String subscriber, String channel, String publisher, String message) {
        if (daemon == null) {
            return;
        }
        
        RecvEvent event = new RecvEvent(subscriber, channel, publisher, message);
        fireEvent(daemon, event, "comm_received");
    }

    private static class RecvEvent implements BindableEvent {

        private final String subscriber;
        private final String channel;
        private final String publisher;
        private final String message;

        public RecvEvent(String subscriber, String channel, String publisher, String message) {
            this.subscriber = subscriber;
            this.channel = channel;
            this.publisher = publisher;
            this.message = message;
        }

        public Object _GetObject() {
            return this;
        }

        public String getChannel() {
            return channel;
        }

        public String getPublisher() {
            return publisher;
        }
        
        public String getSubscriber() {
            return subscriber;
        }

        public String getMessage() {
            return message;
        }
    }

    @api
    public static class srvcom_received extends AbstractEvent {
        public String getName() {
            return "comm_received";
        }

        public String docs() {
            return "{channel: <string match> | publisherid: <string match> | subscriberid: <string match>} "
                    + "Fired when a message is received by a SUB socket. "
                    + "{channel: The channel this message was directed to | "
                    + "publisherid: The name of the publisher who sent this message | "
                    + "subscriberid: The name of the subscriber who received this message | "
                    + "message: The message itself} "
                    + "{channel|publisherid|subscriberid}";
        }

        public boolean matches(Map<String, Construct> prefilter, BindableEvent event)
                throws PrefilterNonMatchException {
            if (event instanceof RecvEvent) {
                RecvEvent e = (RecvEvent)event;
                
                Prefilters.match(prefilter, "channel", e.getChannel(), Prefilters.PrefilterType.STRING_MATCH);
                Prefilters.match(prefilter, "publisherid", e.getPublisher(), Prefilters.PrefilterType.STRING_MATCH);
                Prefilters.match(prefilter, "subscriberid", e.getSubscriber(), Prefilters.PrefilterType.STRING_MATCH);

                return true;
            }
            
            return false;
        }

        public BindableEvent convert(CArray manualObject, Target t) {
            return null;
        }

        public Map<String, Construct> evaluate(BindableEvent event)
                throws EventException {
            if (event instanceof RecvEvent) {
                RecvEvent e = (RecvEvent) event;

                Map<String, Construct> map = evaluate_helper(event);

                map.put("channel", new CString(e.getChannel(), Target.UNKNOWN));
                map.put("publisherid", new CString(e.getPublisher(), Target.UNKNOWN));
                map.put("subscriberid", new CString(e.getSubscriber(), Target.UNKNOWN));
                map.put("message", new CString(e.getMessage(), Target.UNKNOWN));

                return map;
            } else {
                throw new EventException("Cannot convert e to RecvEvent");
            }
        }

        public Driver driver() {
            return Driver.EXTENSION;
        }

        public boolean modifyEvent(String key, Construct value,
                BindableEvent event) {
            return false;
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
    }
}
