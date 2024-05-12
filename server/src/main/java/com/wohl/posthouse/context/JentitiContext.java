package com.wohl.posthouse.context;

import org.jentiti.context.EntityContext;

public class JentitiContext {
    private static EntityContext context;
    public static EntityContext init() {
        context = new EntityContext();
        return context;
    }
    public static EntityContext ctx() {
        return context;
    }
}
