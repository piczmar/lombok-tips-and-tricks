package lombok.tricks.builder;

import lombok.Builder;
import lombok.Value;

@Value
class Pojo {

    String username;
    String surname;

    @Builder
    Pojo(User user, String surname) {
        this.username = user.getEmail();
        this.surname = surname;
    }
}
