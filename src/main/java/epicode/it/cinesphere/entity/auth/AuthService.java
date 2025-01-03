package epicode.it.cinesphere.entity.auth;

import epicode.it.cinesphere.entity.user.IGetUserResponse;
import epicode.it.cinesphere.entity.user.User;
import epicode.it.cinesphere.entity.user.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final epicode.it.cinesphere.security.JwtTokenProvider jwtTokenProvider;

    public IGetUserResponse register(RegisterRequest request) {

        if (userRepo.existsByUsername(request.getUsername()) || userRepo.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("User already exists");
        }

        User u = new User();
        if (request.getFirstName() != null)
            u.setFirstName(request.getFirstName());

        if (request.getLastName() != null)
            u.setLastName(request.getLastName());

        if (request.getUsername() != null)
            u.setUsername(request.getUsername());

        if (request.getEmail() != null)
            u.setEmail(request.getEmail());

        if (request.getPassword() != null)
            u.setPassword(passwordEncoder.encode(request.getPassword()));

        u = userRepo.save(u);

        return userRepo.findByIdGetUserResponse(u.getId());

    }

    public LoginResponse login(LoginRequest request) throws IllegalAccessException {
        if (userRepo.existsByEmail(request.getEmail()) || userRepo.existsByUsername(request.getUsername())) {
            String emailOrUsername = request.getEmail() != null ? request.getEmail() : request.getUsername();
            User u = userRepo.findFirstByEmailOrUsername(emailOrUsername);
            if (u != null && passwordEncoder.matches(request.getPassword(), u.getPassword())) {
                // Genera il token JWT
                String token = jwtTokenProvider.generateToken(u.getUsername());

                // Recupera l'oggetto GetUserResponse
                IGetUserResponse userResponse = userRepo.findByIdGetUserResponse(u.getId());

                // Restituisci una risposta che include il token e i dati utente
                return new LoginResponse(token, userResponse);
            } else {
                throw new IllegalAccessException("Invalid credentials");
            }
        }
        throw new IllegalAccessException("User not found");
    }

}
