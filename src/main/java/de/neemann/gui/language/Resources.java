/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.gui.language;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 */
public class Resources {

    private static XStream getxStream() {
        XStream xStream = new XStream(new StaxDriver());
        xStream.alias("resources", Map.class);
        xStream.registerConverter(new MapEntryConverter());
        return xStream;
    }

    private final Map<String, String> resourceMap;

    Resources() {
        this(new HashMap<>());
    }

    private Resources(Map<String, String> map) {
        resourceMap = map;
    }

    /**
     * Reads the resources from the given stream
     *
     * @param in the input stream
     */
    public Resources(InputStream in) {
        this(loadMap(in));
    }

    private static Map<String, String> loadMap(InputStream in) {
        XStream xStream = getxStream();
        return (Map<String, String>) xStream.fromXML(in);
    }

    void save(OutputStream out) throws IOException {
        XStream xStream = getxStream();
        try (Writer w = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
            w.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
            xStream.marshal(resourceMap, new PrettyPrintWriter(w));
        }
    }

    void put(String key, String value) {
        resourceMap.put(key, value);
    }

    /**
     * Returns a entry by the given key
     *
     * @param key the key
     * @return the entry
     */
    public String get(String key) {
        return resourceMap.get(key);
    }

    /**
     * @return a set containing all keys
     */
    public Set<String> getKeys() {
        return resourceMap.keySet();
    }

    static class MapEntryConverter implements Converter {

        public boolean canConvert(Class clazz) {
            return Map.class.isAssignableFrom(clazz);
        }

        public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
            Map map = (Map) value;
            for (Object obj : map.entrySet()) {
                Map.Entry entry = (Map.Entry) obj;
                writer.startNode("string");
                writer.addAttribute("name", entry.getKey().toString());
                writer.setValue(entry.getValue().toString());
                writer.endNode();
            }
        }

        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            Map<String, String> map = new TreeMap<>();
            while (reader.hasMoreChildren()) {
                reader.moveDown();
                String key = reader.getAttribute("name");
                String value = reader.getValue();
                map.put(key, value);
                reader.moveUp();
            }
            return map;
        }
    }

}
