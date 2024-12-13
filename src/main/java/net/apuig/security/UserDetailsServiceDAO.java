package net.apuig.security;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import net.apuig.user.User;
import net.apuig.user.UserRepository;

public class UserDetailsServiceDAO implements UserDetailsService
{
    final UserRepository userRepository;

    final PasswordEncoder passwordEncoder;

    public UserDetailsServiceDAO(UserRepository userRepository, PasswordEncoder passwordEncoder)
    {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        User user = userRepository.findByName(username);
        if (user == null)
        {
            throw new UsernameNotFoundException("not found " + username);
        }
        return new org.springframework.security.core.userdetails.User(username, user.getPassword(),
            List.of(new SimpleGrantedAuthority(user.getType().name())));
    }
}
