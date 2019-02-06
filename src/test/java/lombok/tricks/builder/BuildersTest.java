package lombok.tricks.builder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BuildersTest {

    @Test
    public void shouldBuildWithAnotherConstructor() {
//        User user = new User("user@test.com");
//        Pojo2 pojo2 = Pojo2.builder()
//            .user(user)
//            .surname("surname2")
//            .build();

        Pojo3 pojo3 = Pojo3.builder()
            .user(new User("email@test.com"))
            .surname("surname")
            .build();

        Assertions.assertNotNull(pojo3.getUsername());
        Assertions.assertNotNull(pojo3.getSurname());
    }

    @Test
    public void shouldBuildWithAdditionalBuilderClass() {
        Pojo4 pojo4 = Pojo4.builderFromUser()
            .user(new User("email@test.com"))
            .surname("surname")
            .build();

        Assertions.assertNotNull(pojo4.getUsername());
        Assertions.assertNotNull(pojo4.getSurname());
    }
}
