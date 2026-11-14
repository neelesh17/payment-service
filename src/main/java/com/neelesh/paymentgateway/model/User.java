package com.neelesh.paymentgateway.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(nullable = false, unique = true)
    String username;
    String name;
    @Column(nullable = false, unique = true)
    String email;
    String phone;
    @Column(nullable = false)
    String paymentMethodToken;
    @Column(length = 2048)
    String webhookUrl;
}
