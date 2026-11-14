package ru.nikzarch.secretsanta.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import ru.nikzarch.secretsanta.domain.Participation
import ru.nikzarch.secretsanta.domain.vo.EventId
import ru.nikzarch.secretsanta.domain.vo.UserId
import ru.nikzarch.secretsanta.service.EventService
import ru.nikzarch.secretsanta.service.exception.NotFoundException
import java.time.Instant

@Tag(name = "Участие")
@RestController
@RequestMapping("/api/v1/events/{eventId}")
class ParticipationController(private val eventService: EventService) {

    @PostMapping("/participation")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Подать заявку на участие")
    fun request(@PathVariable eventId: Int, @AuthenticationPrincipal jwt: Jwt): ParticipationResponseDto =
        ParticipationResponseDto.fromDomain(eventService.requestParticipation(EventId(eventId), jwt.userId()))

    @GetMapping("/participation")
    @Operation(summary = "Статус моей заявки")
    fun myParticipation(@PathVariable eventId: Int, @AuthenticationPrincipal jwt: Jwt): ParticipationResponseDto =
        ParticipationResponseDto.fromDomain(
            eventService.findParticipation(EventId(eventId), jwt.userId())
                ?: throw NotFoundException("participation not found")
        )

    @DeleteMapping("/participation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun leave(@PathVariable eventId: Int, @AuthenticationPrincipal jwt: Jwt) {
        eventService.leaveEvent(EventId(eventId), jwt.userId())
    }

    @GetMapping("/participations")
    @Operation(summary = "Все заявки (только владелец)")
    fun all(@PathVariable eventId: Int, @AuthenticationPrincipal jwt: Jwt): List<ParticipationResponseDto> =
        eventService.findParticipations(EventId(eventId), jwt.userId()).map(ParticipationResponseDto::fromDomain)

    @PostMapping("/participations/{userId}/accept")
    fun accept(
        @PathVariable eventId: Int,
        @PathVariable userId: Int,
        @AuthenticationPrincipal jwt: Jwt
    ): ParticipationResponseDto =
        ParticipationResponseDto.fromDomain(
            eventService.acceptParticipation(
                EventId(eventId),
                UserId(userId),
                jwt.userId()
            )
        )

    @PostMapping("/participations/{userId}/reject")
    fun reject(
        @PathVariable eventId: Int,
        @PathVariable userId: Int,
        @AuthenticationPrincipal jwt: Jwt
    ): ParticipationResponseDto =
        ParticipationResponseDto.fromDomain(
            eventService.rejectParticipation(
                EventId(eventId),
                UserId(userId),
                jwt.userId()
            )
        )
}

data class ParticipationResponseDto(
    val userId: Int,
    val status: String,
    val requestedAt: Instant,
    val decidedAt: Instant?,
) {
    companion object {
        fun fromDomain(participation: Participation) = ParticipationResponseDto(
            userId = participation.participantId.value,
            status = participation.status.name,
            requestedAt = participation.requestedAt,
            decidedAt = participation.decidedAt,
        )
    }
}
