package io.dddspring.common.event;


import java.lang.reflect.Type;

/**
 * emil:410758717@qq.com
 * Created by toger on 2020/12/28.
 */
public interface DomainEventHandle {

    public Type type();

    public void onEvent(Object event);

}