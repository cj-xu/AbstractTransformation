package testcases.paperexamples;

public class Parameters {

    Object f() {
        Object arg = new Object();
        Object obj = id(arg);
        obj = id2(obj);
        return obj;
    }

    Object id(Object par) {
        Object obj = par;
        return obj;
    }

    Object id2(Object par) {
        Object obj = id(par);
        return obj;
    }
}
