package com.jkojote.libraryserver.application.controllers.utils;

import org.thymeleaf.context.IContext;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class Context implements IContext {

    public Map<String, Object> objects;

    public Context(Map<String, Object> map) {
        this.objects = map;
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
}
