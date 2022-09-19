package testcases.paperexamples;

public class StaticInvocation {

    Object foo() {
        Object o = new Object();
        Object o1 = id(o);
        return o1;
    }

    static Object id(Object obj) {
        return obj;
    }
}
