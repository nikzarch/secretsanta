package ru.nikzarch.secretsanta.security

import org.jose4j.keys.HmacKey
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.web.SecurityFilterChain
import kotlin.io.encoding.Base64

@Configuration
class SecurityConfig {

    @Value("\${app.jwt.secret}")
    private lateinit var jwtSecret: String


    @Bean
    fun securityFilterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
        return httpSecurity.csrf { it.disable() }
            .sessionManagement { (it::sessionCreationPolicy)(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers("/api/v1/auth/**").permitAll()
                it.requestMatchers("/api/v1/**").authenticated()
                it.anyRequest().denyAll()
            }
            .oauth2ResourceServer { it.jwt(Customizer.withDefaults()) }
            .build()
    }

    @Bean
    fun PasswordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun jwtDecoder(): JwtDecoder {
        val secret = Base64.decode(jwtSecret)

        val key = HmacKey(secret)

        return NimbusJwtDecoder
            .withSecretKey(key)
            .macAlgorithm(MacAlgorithm.HS256)
            .build()
    }

    @Bean
    fun jwtEncoder(): JwtEncoder {
        val secret = Base64.decode(jwtSecret)

        val key = HmacKey(secret)
        return NimbusJwtEncoder
            .withSecretKey(key)
            .algorithm(MacAlgorithm.HS256)
            .build()
    }

}