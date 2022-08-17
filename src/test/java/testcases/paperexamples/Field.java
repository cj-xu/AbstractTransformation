package testcases.paperexamples;

public class Field {

    class M {
    }

    class N {
        M f;
    }

    /**
     * Due to the weak update for fields, the field <created at .(Field.java:18)>.f
     * has type {<created at .(Field.java:19)>, <created at .(Field.java:21)>}, and thus
     * so does the variable x.
     */
    M h() {
       N y = new N();
       y.f = new M();
       M x = y.f;
       y.f = new M();
       return x;
    }
}
