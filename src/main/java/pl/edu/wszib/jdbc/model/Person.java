package pl.edu.wszib.jdbc.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Person {
    private int id;
    private String name;
    private String surname;
    private String password;
}
