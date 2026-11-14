package ru.nikzarch.secretsanta.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.*
import org.springframework.security.web.SecurityFilterChain
import java.util.*
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Configuration
class SecurityConfig(
    @Value("\${app.jwt.secret}") private val jwtSecret: String,
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain = http
        .csrf { it.disable() }
        .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        .authorizeHttpRequests {
            it.requestMatchers(
                "/api/v1/auth/**",
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/v3/api-docs.yaml"
            ).permitAll()
            it.requestMatchers("/admin-api/v1/**").permitAll()
            it.requestMatchers("/api/v1/**").authenticated()
            it.anyRequest().denyAll()
        }
        .oauth2ResourceServer { it.jwt(Customizer.withDefaults()) }
        .build()

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun jwtKey(): SecretKey {
        val secret = Base64.getDecoder().decode(jwtSecret)
        require(secret.size >= 32) { "app.jwt.secret must decode to at least 32 bytes" }
        return SecretKeySpec(secret, "HmacSHA256")
    }

    @Bean
    fun jwtDecoder(key: SecretKey): JwtDecoder = NimbusJwtDecoder
        .withSecretKey(key)
        .macAlgorithm(MacAlgorithm.HS256)
        .build()
        .apply {
            val audienceValidator = JwtClaimValidator<List<String>>("aud") { it.contains("secretsanta-api") }
            setJwtValidator(
                DelegatingOAuth2TokenValidator(
                    JwtValidators.createDefaultWithIssuer("secretsanta"),
                    audienceValidator,
                )
            )
        }

    @Bean
    fun jwtEncoder(key: SecretKey): JwtEncoder = NimbusJwtEncoder
        .withSecretKey(key)
        .algorithm(MacAlgorithm.HS256)
        .build()
}
