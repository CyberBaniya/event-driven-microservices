package com.cyboul.eda.userservice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

@Data
@Document(collection = "users")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private String id;
    @Indexed(unique = true)
    private String email;
    private String name;
    private String password;
}
