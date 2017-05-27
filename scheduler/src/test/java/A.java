
/**
 * Created by yefeng on 16/9/12.
 */
public class A {

    A(int val){
        value_ = val;
        if (val > 0) {
            b_ = new B(val - 1);
        }
    }

    public B b_;

    public int value_;


    public static void main(String[] args) {
        A a = new A(100);
        System.out.println(a.value_);
        System.out.println(a.b_.value_);
        System.out.println(a.b_.a_.value_);
        System.out.println(a.b_.a_.b_.value_);
    }


}
