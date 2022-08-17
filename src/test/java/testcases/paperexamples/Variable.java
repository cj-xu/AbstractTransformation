package testcases.paperexamples;

public class Variable {

    Object h() {
        Object x = new Object();
        Object y = x;
        Object z = new Object();
        y = z;
        z = x;
        x = y;
        return x;
    }
}
