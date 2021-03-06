package ch.puzzle.itc.mobiliar.business.auditview.control;

import java.util.HashMap;

/**
 * Copied from <a href="https://gist.github.com/nschlimm/2234464#file-gistfile1-java">here.</a>
 * More information in <a href="http://niklasschlimm.blogspot.ch/2012/04/threading-stories-threadlocal-in-web.html">this Blogpost</a>.
 */
public class ThreadLocalUtil {
    public static final String KEY_RESOURCE_ID = "resourceId";
    public static final String KEY_RESOURCE_TYPE_ID = "resourceTypeId";

    private final static ThreadLocal<ThreadVariables> THREAD_VARIABLES = new ThreadLocal<ThreadVariables>() {
        @Override
        protected ThreadVariables initialValue() {
            return new ThreadVariables();
        }
    };

    public static Object getThreadVariable(String name) {
        return THREAD_VARIABLES.get().get(name);
    }

    public static Object getThreadVariable(String name, InitialValue initialValue) {
        Object o = THREAD_VARIABLES.get().get(name);
        if (o == null) {
            THREAD_VARIABLES.get().put(name, initialValue.create());
            return getThreadVariable(name);
        } else {
            return o;
        }
    }

    protected static void setThreadVariable(String name, Object value) {
        THREAD_VARIABLES.get().put(name, value);
    }

    public static void destroy() {
        THREAD_VARIABLES.remove();
    }
}

class ThreadVariables extends HashMap<String, Object> {
}

abstract class InitialValue {

    public abstract Object create();

}