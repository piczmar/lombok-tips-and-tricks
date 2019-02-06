package lombok.tricks.jackson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

class JacksonMappingTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void beforeEach() {
        objectMapper = new ObjectMapper();
    }

    @Test
    public void shouldDeserialize() throws IOException {
        String json = "{\"login\" : \"johnsmith\", \"age\": 77}";

        ValueObject1 vo = objectMapper.readValue(json, ValueObject1.class);

        assertEquals("johnsmith", vo.getLogin());
        assertEquals(77, vo.getAge());
    }

    @Test
    public void shouldDeserializeFromUnknownFields() throws IOException {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // name is not specified in ValueObject1
        String json = "{\"name\" : \"johnsmith\", \"age\": 77}";

        ValueObject1 vo = objectMapper.readValue(json, ValueObject1.class);

        assertNull(vo.getLogin());
        assertEquals(77, vo.getAge());
    }

    @Test
    public void shouldValidate() throws IOException {
        // name is not specified in ValueObject1
        String json = "{\"name\" : \"johnsmith\", \"age\": 77}";

        ValueObject3 vo = objectMapper.readValue(json, ValueObject3.class);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<ValueObject3>> errors = validator.validate(vo);

        assertFalse(errors.isEmpty());
        ConstraintViolation<ValueObject3> error = errors.iterator().next();
        assertEquals("surname", error.getPropertyPath().toString());
        assertEquals("must not be blank", error.getMessage());
    }

    @Test
    public void shouldSerialize() throws IOException {
        ValueObject2 vo = new ValueObject2("John", null, 77);

        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        objectMapper.writeValue(bas, vo);

        String jsonString = new String(bas.toByteArray());

        assertEquals("{\"name\":\"John\",\"surname\":null,\"age\":77}", jsonString);
    }
}
