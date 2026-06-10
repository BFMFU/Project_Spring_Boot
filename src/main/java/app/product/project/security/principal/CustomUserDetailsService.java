package app.product.project.security.principal;
import app.product.project.model.entity.Role;
import app.product.project.model.entity.Users;
import app.product.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Users users = userRepository.findByUsername(username).orElseThrow(() -> new NoSuchElementException("Không tồn tại user: " + username));

		return  CustomUserDetails.builder()
				        .userId(users.getUserId())
				        .username(users.getUsername())
				        .password(users.getPassword())
				        .fullName(users.getFullName())
				        .email(users.getEmail())
				        .isActive(users.getIsActive())
				        .authorities(mapRoleToAuthority(users.getRoles()))
				        .build();
	}

	private List<? extends GrantedAuthority> mapRoleToAuthority(List<Role> roles) {
		return roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r.getRoleName())).toList();
	}
}