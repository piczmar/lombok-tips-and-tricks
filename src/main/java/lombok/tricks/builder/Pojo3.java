package lombok.tricks.builder;

import lombok.Builder;
import lombok.Value;

@Value
//@Builder(toBuilder = true // this won't fix compilation error, need to overwrite constructor
class Pojo3 {

    String username;
    String surname;

    @Builder(toBuilder = true)
    Pojo3(String username, String surname) {
        this.username = username;
        this.surname = surname;
    }

    @Builder
    Pojo3(User user, String surname) {
        this.username = user.getEmail();
        this.surname = surname;
    }
}
