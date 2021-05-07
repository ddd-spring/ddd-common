package io.dddspring.common.notification;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestFastJson {
    private static final Logger logger = Logger.getLogger(TestFastJson.class.getName());
    @Test
    public void testfastjson(){

        String rs="{\"event\":{\"eventVersion\":1,\"id\":1609235914290,\"name\":\"name1609235914290\",\"occurredOn\":\"1609235914421\"},\"notificationId\":5,\"occurredOn\":\"1609235914421\",\"typeName\":\"com.weailove.common.event.TestableDomainEvent\",\"version\":1}";

        Type type = new TypeToken<Notification<TestableDomainEvent>>() {}.getType();

        Notification<TestableDomainEvent> notification = NotificationSerializer.instance().deserialize(rs, type);
        Gson gson=new Gson();
        logger.info(gson.toJson(notification));


        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);

//        String msg= JSON.toJSONString(notification, SerializerFeature.WriteClassName);
        String msg= JSON.toJSONString(notification, SerializerFeature.WriteClassName);
        logger.info("fastjson::recived-msg::"+msg);

        Notification<TestableDomainEvent> notification2= JSON.parseObject(rs,new TypeReference<Notification<TestableDomainEvent>>(){});

        logger.info("fastjson::notification2::notification2::"+JSON.toJSONString(notification2, SerializerFeature.WriteClassName));
        logger.info("fastjson::notification2::notification2::event::"+JSON.toJSONString(notification2.getEvent(), SerializerFeature.WriteClassName));


        assertEquals("com.weailove.common.event.TestableDomainEvent",   notification2.getTypeName());
        assertEquals("com.weailove.common.event.TestableDomainEvent",   notification2.typeName());

//        assertEquals(1609235914290L,   notification2.getEvent().getId());
//        assertEquals(1,   notification2.getEvent().getEventVersion());
//        assertEquals(1609235914421L,   notification2.getEvent().getOccurredOn());
//        assertEquals("name1609235914290",   notification2.event().name());
//        assertEquals("name1609235914290",   notification2.getEvent().getName());
    }

}
