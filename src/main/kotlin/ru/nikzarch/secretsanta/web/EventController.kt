package ru.nikzarch.secretsanta.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import ru.nikzarch.secretsanta.domain.SecretSantaEvent
import ru.nikzarch.secretsanta.domain.vo.EventId
import ru.nikzarch.secretsanta.domain.vo.UserId
import ru.nikzarch.secretsanta.service.CreateEventRequest
import ru.nikzarch.secretsanta.service.EventService
import ru.nikzarch.secretsanta.service.GiftRecipient
import ru.nikzarch.secretsanta.service.UpdateEventRequest
import ru.nikzarch.secretsanta.service.exception.NotFoundException
import java.time.LocalDate

@Tag(name = "События")
@RestController
@RequestMapping("/api/v1/events")
class EventController(private val eventService: EventService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать событие; владелец участвует автоматически")
    fun create(@RequestBody request: CreateEventRequestDto, @AuthenticationPrincipal jwt: Jwt): EventResponseDto =
        EventResponseDto.fromDomain(
            eventService.createEvent(
                request.toServiceRequest(jwt.userId())
            )
        )

    @GetMapping("/{id}")
    fun get(@PathVariable id: Int): EventResponseDto =
        EventResponseDto.fromDomain(eventService.findById(EventId(id)) ?: throw NotFoundException("event not found"))

    @GetMapping("/owned")
    fun owned(@AuthenticationPrincipal jwt: Jwt): List<EventResponseDto> =
        eventService.findByOwnerId(jwt.userId()).map(EventResponseDto::fromDomain)

    @GetMapping("/joined")
    fun joined(@AuthenticationPrincipal jwt: Jwt): List<EventResponseDto> =
        eventService.findByParticipantId(jwt.userId()).map(EventResponseDto::fromDomain)

    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: Int,
        @RequestBody request: UpdateEventRequestDto,
        @AuthenticationPrincipal jwt: Jwt
    ): EventResponseDto =
        EventResponseDto.fromDomain(
            eventService.updateEvent(
                request.toServiceRequest(EventId(id)), jwt.userId()
            )
        )

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Int, @AuthenticationPrincipal jwt: Jwt) {
        eventService.deleteEvent(EventId(id), jwt.userId())
    }

    @PostMapping("/{id}/deactivate")
    fun deactivate(@PathVariable id: Int, @AuthenticationPrincipal jwt: Jwt): EventResponseDto =
        EventResponseDto.fromDomain(eventService.deactivateEvent(EventId(id), jwt.userId()))

    @GetMapping("/{id}/gift")
    @Operation(summary = "Мой получатель и его вишлист после жеребьёвки")
    fun gift(@PathVariable id: Int, @AuthenticationPrincipal jwt: Jwt): GiftResponseDto =
        GiftResponseDto.fromDomain(eventService.findRecipient(EventId(id), jwt.userId()))

    @PostMapping("/{id}/draw")
    @Operation(summary = "Провести жеребьёвку (только владелец)")
    fun draw(@PathVariable id: Int, @AuthenticationPrincipal jwt: Jwt): EventResponseDto =
        EventResponseDto.fromDomain(eventService.generateSantaAssignments(EventId(id), jwt.userId()))
}

data class CreateEventRequestDto(val name: String, val description: String?, val date: LocalDate) {
    fun toServiceRequest(ownerId: UserId) = CreateEventRequest(name, description, date, ownerId)
}

data class UpdateEventRequestDto(
    val name: String? = null,
    val description: String? = null,
    val date: LocalDate? = null
) {
    fun toServiceRequest(id: EventId) = UpdateEventRequest(id, name, description, date)
}

data class EventResponseDto(
    val id: Int,
    val name: String,
    val description: String?,
    val date: LocalDate,
    val ownerId: Int,
    val participantsCount: Int,
    val active: Boolean,
    val assignmentsGenerated: Boolean,
) {
    companion object {
        fun fromDomain(event: SecretSantaEvent) = EventResponseDto(
            id = requireNotNull(event.id).value,
            name = event.name,
            description = event.description,
            date = event.date,
            ownerId = event.ownerId.value,
            participantsCount = event.participants.size,
            active = event.isActive(),
            assignmentsGenerated = event.areAssignmentsGenerated(),
        )
    }
}

data class GiftResponseDto(
    val username: String,
    val items: List<WishlistItemDto>,
) {
    companion object {
        fun fromDomain(recipient: GiftRecipient) = GiftResponseDto(
            username = recipient.user.username,
            items = recipient.wishlist?.items?.map(WishlistItemDto::fromDomain) ?: emptyList(),
        )
    }
}
