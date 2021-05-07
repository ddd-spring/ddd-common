package io.dddspring.common.notification;

import io.dddspring.common.event.DomainEventHandle;

import java.util.HashMap;

public class DomainNotificationEvent implements NotificationDomain {

    protected int recivenum;
    protected int successednum;
    protected int failednum;
    public DomainNotificationEvent(){
        reset();
    }
    public int reciveCount(){return recivenum;}
    public int sucessedCount(){return successednum;}
    public int failedCount(){return failednum;}
    public void reset(){
        recivenum=0;
        successednum=0;
        failednum=0;
    }
    protected HashMap<String, DomainEventHandle> eventlist=new HashMap<String, DomainEventHandle>();

    @Override
    public void registerEvent(Class eventclass,DomainEventHandle eventHandle){

        System.out.println("eventclass.getCanonicalName"+eventclass.getCanonicalName());
        System.out.println("eventclass.getSimpleName"+eventclass.getSimpleName());
        System.out.println("eventclass.getName"+eventclass.getName());

        this.eventlist.put(eventclass.getName(),eventHandle);
    }
    @Override
    public void onNoticee(String rs) {
        try {
                System.out.println("消息rs::"+rs);
                NotificationReader reader = new NotificationReader(rs);
                DomainEventHandle eventHandle=eventlist.get(reader.typeName());

                if(eventlist.containsKey(reader.typeName())){
                    eventHandle.onEvent(NotificationSerializer.instance().deserialize(rs, eventHandle.type()));
                    successednum++;
                }

                System.out.println("消息reader.typeName::"+reader.typeName());

        } catch (Exception e) {
            e.printStackTrace();
            failednum++;

        }finally {
            recivenum ++;
        }


    }
}