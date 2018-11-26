package com.jkojote.libraryserver.application.controllers.utils;

import org.thymeleaf.context.IContext;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class Context implements IContext {

    public Map<String, Object> objects;

    public Context(Map<String, Object> map) {
        this.objects = map;
    }

    public static final ContextBuilder builder() {
        return new ContextBuilder();
    }
    @Override
    public Locale getLocale() {
        return Locale.ENGLISH;
    }

    @Override
    public boolean containsVariable(String name) {
        return objects.containsKey(name);
    }

    @Override
    public Set<String> getVariableNames() {
        return objects.keySet();
    }

    @Override
    public Object getVariable(String name) {
        return objects.get(name);
    }

    public static final class ContextBuilder {

        private Map<String, Object> objects;

        ContextBuilder() {
            objects = new HashMap<>();
        }

        public ContextBuilder add(String key, Object value) {
            this.objects.put(key, value);
            return this;
        }

        public ContextBuilder addAll(Map<String, Object> values) {
            this.objects.putAll(values);
            return this;
        }

        public Context build() {
            return new Context(objects);
        }
    }
}
