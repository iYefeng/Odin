/**
 * Created by yefeng on 16/9/12.
 */
public class B {

    B(int val) {
        value_ = val;
        a_ = new A(val-1);
    }

    A a_;
    int value_;

}
