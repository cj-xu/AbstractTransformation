package testcases.paperexamples;

public class Field {

    class M {
    }

    class N {
        M f;
    }

    // return type: <created at .(Field.java:15)> , <created at .(Field.java:17)>
    M h() {
       N y = new N();
       y.f = new M();
       M x = y.f;
       y.f = new M();
       return x;
    }
}
