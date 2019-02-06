package lombok.tricks.inheritance;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Data
class Parent {

    private final String a;
}

@Value
@EqualsAndHashCode(callSuper = true)
class Child extends Parent {

    String b;

    Child(String a, String b) {
        super(a);
        this.b = b;
    }
}

@Value
@EqualsAndHashCode(callSuper = true)
class Child2 extends Parent {

    String b;

    @Builder
    Child2(String a, String b) {
        super(a);
        this.b = b;
    }
}
