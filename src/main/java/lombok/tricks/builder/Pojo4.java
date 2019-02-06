package lombok.tricks.builder;

import lombok.Builder;
import lombok.Value;

@Value
class Pojo4 {

    String username;
    String surname;

    @Builder(toBuilder = true)
    Pojo4(String username, String surname) {
        this.username = username;
        this.surname = surname;
    }

    @Builder(builderMethodName = "builderFromUser", builderClassName = "FromUserBuilder")
    Pojo4(User user, String surname) {
        this.username = user.getEmail();
        this.surname = surname;
    }
}
