package com.epam.gym.core.component;

import com.epam.gym.core.repository.UserRepository;
import com.epam.gym.core.service.UserService;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.ScenarioScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

@ScenarioScope
public class UserServiceSteps {

    @Autowired private ScenarioContext ctx;
    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @When("the password is changed")
    public void thePasswordIsChanged() {
        ctx.newPassword = "NewPass" + ctx.uid;
        userService.changePassword(ctx.trainee.getUser().getUsername(), ctx.newPassword);
    }

    @Then("the stored password is not the plain-text new value")
    public void theStoredPasswordIsNotThePlainTextNewValue() {
        String stored = userRepository.findByUsername(ctx.trainee.getUser().getUsername())
                .orElseThrow().getPassword();
        assertThat(stored).isNotEqualTo(ctx.newPassword);
    }

    @Then("the stored password matches the new value when verified with BCrypt")
    public void theStoredPasswordMatchesTheNewValueWithBCrypt() {
        String stored = userRepository.findByUsername(ctx.trainee.getUser().getUsername())
                .orElseThrow().getPassword();
        assertThat(passwordEncoder.matches(ctx.newPassword, stored)).isTrue();
    }

    @When("the password is changed for a username that does not exist")
    public void thePasswordIsChangedForAUsernameThatDoesNotExist() {
        try {
            userService.changePassword("nonexistent-" + ctx.uid, "anyPassword");
        } catch (NoSuchElementException e) {
            ctx.thrownException = e;
        }
    }
}
