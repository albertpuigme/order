package net.apuig.user;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import net.apuig.user.dto.RegisterPassengerRequestDto;
import net.apuig.user.dto.UserDto;

@RestController
public class UserController
{
    private final UserService userService;

    public UserController(UserService userService)
    {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerPassenger(
        @RequestBody @Valid RegisterPassengerRequestDto request)
    {
        Long userId = userService.registerPassenger(request);
        // TODO location header do not point to any endpoint
        return ResponseEntity.created(URI.create("/users/" + userId))
            .body(new UserDto(userId, request.name()));
    }

    @GetMapping("/current_passenger")
    @PreAuthorize("hasAuthority('PASSENGER')")
    public ResponseEntity<UserDto> getCurrentPassenger(
        @AuthenticationPrincipal final User loginInfo)
    {
        Long userId = userService.getPassenger(loginInfo.getUsername()).getId();
        return ResponseEntity.ok(new UserDto(userId, loginInfo.getUsername()));
    }
}
