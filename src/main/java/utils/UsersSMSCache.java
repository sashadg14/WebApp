package utils;

import tcp_ip.channels.AbstractSocket;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alex on 15.02.2018.
 */
public class UsersSMSCache {
    private Map<AbstractSocket,String> smsCache=new HashMap<>();

    public String removeCachedSMS(AbstractSocket channel){
        return smsCache.remove(channel);
    }

    public void addSMSinCache(AbstractSocket channel,String sms){
        if(smsCache.containsKey(channel)) {
            String old = smsCache.get(channel);
            smsCache.replace(channel,smsCache.get(channel),old+"\n"+sms);
        } else smsCache.put(channel,sms);
    }
}
