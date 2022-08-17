package testcases.paperexamples;

public class RunningExample {

    void f() {
        C c = new C();
        c.f = new D();
        D d = new D();
        foo(d, c);
    }

    void foo(D x, C y) {
        x = y.f;
        y = new C();
        y.f = x;
    }

    class C {
        D f;
    }

    class D {

    }
}
