package com.archiveplayer.services;

import com.archiveplayer.repositories.AccountRepository;
import com.archiveplayer.dto.AuthRequestDTO;
import com.archiveplayer.dto.AuthResponseDTO;
import com.archiveplayer.entities.Account;
import com.archiveplayer.security.JWTTokenGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTTokenGenerator tokenProvider;

    public AuthService(AccountRepository accountRepository,
                       PasswordEncoder passwordEncoder,
                       JWTTokenGenerator tokenProvider) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public void registerUser(AuthRequestDTO registerRequest) {
        if (accountRepository.findByAccountName(registerRequest.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken.");
        }
        Account newAccount = new Account(registerRequest.getUsername(), passwordEncoder.encode(registerRequest.getPassword()));
        accountRepository.save(newAccount);
    }

    @Transactional
    public AuthResponseDTO loginUser(AuthRequestDTO loginRequest) {
        Optional<Account> accountOptional = accountRepository.findByAccountName(loginRequest.getUsername());

        if (accountOptional.isEmpty() || !passwordEncoder.matches(loginRequest.getPassword(), accountOptional.get().getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password.");
        }

        Account account = accountOptional.get();
        String newDeviceTokenId = UUID.randomUUID().toString();
        String jwt = tokenProvider.generateToken(account.getId(), account.getAccountName(), newDeviceTokenId);

        account.setActiveSessionToken(newDeviceTokenId);
        accountRepository.save(account);
        
        logger.info("User {} logged in. DB ActiveSessionToken set to: {}", account.getAccountName(), newDeviceTokenId);

        return new AuthResponseDTO(account.getId(), account.getAccountName(), jwt);
    }

    @Transactional
    public void logoutUser(Account account) {
        if (account != null) {
            Optional<Account> attachedAccount = accountRepository.findById(account.getId());
            if (attachedAccount.isPresent()) {
                attachedAccount.get().setActiveSessionToken(null);
                accountRepository.save(attachedAccount.get());
                logger.info("User {} logged out. DB token cleared.", account.getAccountName());
            }
        }
    }
}