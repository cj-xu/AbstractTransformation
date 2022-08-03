package testcases.paperexamples;

public class RunningExample {

    D f() {
        C c = new C();
        c.f = new D();
        D d = new D();
        return foo(d , c);
    }

    D foo(D x, C y) {
        x = y.f;
        y = new C();
        y.f = x;
        return y.f;
    }

    class C {
        D f;
    }

    class D {

    }
}
