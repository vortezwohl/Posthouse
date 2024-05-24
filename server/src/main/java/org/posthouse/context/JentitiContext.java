package org.posthouse.context;

import org.jentiti.context.EntityContext;

/*
 * @Author 吴子豪
 */
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
