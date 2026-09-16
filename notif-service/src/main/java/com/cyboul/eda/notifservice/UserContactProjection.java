package com.cyboul.eda.notifservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "user_projections")
@NoArgsConstructor
@AllArgsConstructor
public class UserContactProjection {
    @Id
    private String userId;
    private String email;
    private String name;
}
