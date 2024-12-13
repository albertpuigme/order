package net.apuig.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.apuig.user.dto.RegisterPassengerRequestDto;

@Service
@Transactional(readOnly = true)
public class UserService
{
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder)
    {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Long registerPassenger(RegisterPassengerRequestDto request)
    {
        // TODO enforce password policy
        if (userRepository.findByName(request.name()) != null)
        {
            throw new PassengerAlreadyExistsException();
        }
        return userRepository.save(new User(request.name(),
            passwordEncoder.encode(request.password()), UserType.PASSENGER)).getId();
    }

    public User getPassenger(String name)
    {
        User user = userRepository.findByName(name);
        if (user == null || user.getType() != UserType.PASSENGER)
        {
            throw new PassengerNotFoundException();
        }
        return user;
    }
}
