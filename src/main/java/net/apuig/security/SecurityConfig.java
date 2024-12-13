package net.apuig.security;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import org.springframework.aop.Advisor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.method.AuthorizationManagerBeforeMethodInterceptor;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import net.apuig.user.UserRepository;

@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = false)
@Configuration(proxyBeanMethods = false)
public class SecurityConfig
{
    @Bean
    SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception
    {
        // TODO pending production config
        http.csrf(CsrfConfigurer::disable).cors(CorsConfigurer::disable);

        http.authorizeHttpRequests(//
            (authorize) -> authorize
                // can list categories and products without authentication
                .requestMatchers(HttpMethod.GET, "/categories", "/categories/*",
                    "/categories/*/products", "/products")
                .anonymous()//
                .requestMatchers(HttpMethod.POST, "/register").anonymous()
                //
                .anyRequest().authenticated())//
            .httpBasic(Customizer.withDefaults())//
            .sessionManagement(session -> session.sessionCreationPolicy(STATELESS));

        return http.build();
    }

    @Bean
    UserDetailsServiceDAO appUserDetailsServiceDAO(UserRepository userRepository,
        PasswordEncoder passwordEncoder)
    {
        return new UserDetailsServiceDAO(userRepository, passwordEncoder);
    }

    @Bean
    PasswordEncoder passwordEncoder(final @Value("${passwordencoder.rounds:11}") Integer rounds)
    {
        return new BCryptPasswordEncoder(rounds);
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    static Advisor preAuthorizeMethodInterceptor()
    {
        return AuthorizationManagerBeforeMethodInterceptor.preAuthorize();
    }
}
