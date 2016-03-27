package com.monochord.chat.common.util;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.concurrent.ConcurrentHashMap;

public class Marshalling {

    private Marshalling() {}


    private static final ConcurrentHashMap<Class<?>, JAXBContext> contexts = new ConcurrentHashMap<>();


    public static String marshal(Object obj) throws JAXBException {
        StringWriter writer = new StringWriter();
        getContext(obj.getClass()).createMarshaller().marshal(obj, writer);
        return writer.toString();
    }


    public static <T> T unmarshal(String xml, Class<T> type) throws JAXBException {
        Unmarshaller u = getContext(type).createUnmarshaller();
        u.setEventHandler(validationEvent -> false); // fail early on validation errors
        return type.cast(u.unmarshal(new StringReader(xml)));
    }


    private static JAXBContext getContext(Class<?> clazz) {
        return contexts.computeIfAbsent(clazz, cl -> {
            try {
                return JAXBContext.newInstance(cl);
            }
            catch (JAXBException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
