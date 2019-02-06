package lombok.tricks.jackson;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import lombok.Value;

@Value
@Valid
class ValueObject3 {

    @NotBlank
    String name;
    @NotBlank
    String surname;
    int age;
}
