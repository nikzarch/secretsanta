package ru.nikzarch.secretsanta.service.impl

import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import ru.nikzarch.secretsanta.domain.User
import ru.nikzarch.secretsanta.security.JwtToken
import ru.nikzarch.secretsanta.service.JwtService
import java.time.Instant
import java.time.temporal.ChronoUnit


@Service
class JwtServiceImpl(
    private val jwtEncoder: JwtEncoder,
) : JwtService {
    override fun getTokenForUser(user: User): JwtToken {
        val now = Instant.now()

        val claims = JwtClaimsSet.builder()
            .subject(requireNotNull(user.id).value.toString())
            .issuedAt(now)
            .expiresAt(now.plus(1, ChronoUnit.HOURS))
            .build()

        val header = JwsHeader
            .with(MacAlgorithm.HS256)
            .build()

        return JwtToken(
            jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
            ).tokenValue
        )
    }
}