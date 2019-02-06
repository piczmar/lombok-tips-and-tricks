# Demonstration of Lombok tricks
[Project Lombok](https://projectlombok.org/) is a Java library which can generate some commonly used code and facilitate keeping source code clean, e.g. by using some annotations you can generate constructors, getters, setters and other helpful code for your classes.

I am showing here a few common use cases of Lombok,  possible problems and propose solutions for them.

## Custom builders

Let's create a builder with different arguments than class fields, e.g.:

```java
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
```
NOTE: You don't need to add `private final` on fields - Lombok will generate them.

Then if you use `toBuilder` property of Lombok `@Builder` annotation you may face tricky compilation errors 
hard to spot in source class because they are actually coming from Lombok generated sources.

E.g: 

```java 
import lombok.Builder;
import lombok.Value;

@Value
class Pojo {

    String username;
    String surname;

    @Builder(toBuilder = true)
    Pojo(User user, String surname) {
        this.username = user.getEmail();
        this.surname = surname;
    }
}
```
Compilation will fail with error:
```bash
Error:java: cannot find symbol
  symbol: variable user
```
This is because Lombok generates the `toBuilder` method with class fields like that: 

```java
public Pojo.PojoBuilder toBuilder() {
   return (new Pojo.PojoBuilder()).user(this.user).surname(this.surname);
}
```
Obviously `this.user` causes the compilation error.

To fix it you could add another `@Builder` annotation building from all class fields and set `toBuilder` property on it like that:


```java 
import lombok.Builder;
import lombok.Value;

@Value
class Pojo {

    String username;
    String surname;
 
    @Builder(toBuilder = true)
    Pojo(String username, String surname) {
        this.username = username;
        this.surname = surname;
    }
    
    @Builder
    Pojo(User user, String surname) {
        this.username = user.getEmail();
        this.surname = surname;
    }
}
```
Looks like now compilation succeeds and you can use the `toBuilder()` method.
But wait.. it's still wrong. Try to build an object with the new builder method and check which values were set: 

```java
Pojo pojo = Pojo.builder()
    .user( new User("email@test.com"))
    .surname("surname")
    .build();

System.out.println(pojo.getUsername());
System.out.println(pojo.getSurname());
```

It prints:

```bash
null
surname
```

The generated `builder()` method is still using the other builder (the one with the `toBuilder` property) and not setting the `username` from passed `User` object.
Lombok generated such code:

```java
public static Pojo.PojoBuilder builder() {
    return new Pojo.PojoBuilder();
}

public Pojo.PojoBuilder toBuilder() {
    return (new Pojo.PojoBuilder()).username(this.username).surname(this.surname);
}
```

How to fix this properly?
We have to specify method and class name for the additional builder. This is the final working solution:

```java
import lombok.Builder;
import lombok.Value;

@Value
class Pojo {

    String username;
    String surname;

    @Builder
    Pojo(String username, String surname) {
        this.username = username;
        this.surname = surname;
    }

    @Builder(builderMethodName = "builderFromUser", builderClassName = "FromUserBuilder")
    Pojo(User user, String surname) {
        this.username = user.getEmail();
        this.surname = surname;
    }
}
```
## Inheritance

Let's assume we have classes `Parent` and `Child` which is extending the `Parent`.

In order to be able to extend we cannot use the `@Value` annotation as it makes the class `final`.
We can use `@Data` instead which will also generate getters, but we will have to implement a constructor.
Lombok is not able to generate constructor using inheritance information.

The implementation would look as follows:

```java
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Data
class Parent {

    private final String a;
}

@Value
class Child extends Parent {

    String b;

    Child(String a, String b) {
        super(a);
        this.b = b;
    }
}

```
On the subclass we can use the `@Value` unless we plan to extend from this class as well.

After adding the required constructor the class compiles but with a warning: 

```bash
Warning:(12, 1) java: Generating equals/hashCode implementation but without a call to superclass, even though this class does not extend java.lang.Object. If this is intentional, add '@EqualsAndHashCode(callSuper=false)' to your type.
```
So we should add the annotation on subclass: 
```java
@EqualsAndHashCode(callSuper=true)
```

## Jackson serialization and deserialization
When implementing data transfer objects for use with Jackson library it is handful to use Lombok to eliminate the getters-setters boilerplate code, e.g.: 


```java
@Value
class ValueObject {

    String login;
    int age;
}
```

Then we could deserialize JSON like that: 

```java

ObjectMapper objectMapper = new ObjectMapper();
String json = "{\"login\" : \"johnsmith\", \"age\": 77}";
ValueObject vo = objectMapper.readValue(json, ValueObject.class);
```

But wait, at runtime it fails with: 

```bash
om.fasterxml.jackson.databind.exc.InvalidDefinitionException: Cannot construct instance of `lombok.tricks.jackson.ValueObject` (no Creators, like default construct, exist): cannot deserialize from Object value (no delegate- or property-based Creator)
 at [Source: (String)"{"login" : "johnsmith", "age": 77}"; line: 1, column: 2]

```

This is because the generated class looks as follows: 

```java

final class ValueObject {
    private final String login;
    private final int age;

    public ValueObject(String login, int age) {
        this.login = login;
        this.age = age;
    }

    public String getLogin() {
        return this.login;
    }

    public int getAge() {
        return this.age;
    }

    // equals, hashCode and toString follow.. 
}

```

Jackson does not recognize that the constructor is a creator of the class, it expects default constructor or constructor annotated with: 
`@JsonCreator` e.g.: 


```java
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

...

@JsonCreator
ValueObject(@JsonProperty("login") String login,@JsonProperty("age") int age) {
    this.login = login;
    this.age = age;
}
```

or with java beans annotation: 

```java
@ConstructorProperties({"login", "age"})
ValueObject(String login, int age) {
    this.login = login;
    this.age = age;
}
```

which is slightly shorter but still easy to make a mistake and rename argument name in constructor but not in annotation.

Fortunately, Lombok can generate it as well if we only configure it properly.
To do so, we have to add a property file in project root folder named `lombok.config` with content: 

```text
lombok.anyConstructor.addConstructorProperties=true
```

Then we can be happy with clean DTO implementation like we initially wanted to have: 

```java
@Value
class ValueObject {

    String login;
    int age;
}
```

It works fine with Jackson now.