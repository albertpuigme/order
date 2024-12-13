package net.apuig.user;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.apuig.StoreApplication;
import net.apuig.user.dto.RegisterPassengerRequestDto;

@SpringBootTest(classes = StoreApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private UserRepository userRepository;

    @BeforeAll
    public void setup()
    {
        // already running EntitySetup. Attendant users
    }

    @Test
    public void getCurrentPassengerNeedsAuth() throws Exception
    {
        this.mockMvc.perform(get("/current_passenger")//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isUnauthorized());

        this.mockMvc.perform(get("/current_passenger")//
            .with(httpBasic("foo", "bar"))//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isUnauthorized());
    }

    @Test
    public void getCurrentPassengerNotForAttendants() throws Exception
    {
        this.mockMvc.perform(get("/current_passenger")//
            .with(httpBasic("att1", "Att1Passowrd!"))//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    public void registerPassenger() throws Exception
    {
        this.mockMvc.perform(post("/register")//
            .content(json.writeValueAsBytes(//
                new RegisterPassengerRequestDto("Alice", "Temporal1!")))
            .contentType(MediaType.APPLICATION_JSON)//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isCreated());

        this.mockMvc.perform(get("/current_passenger")//
            .with(httpBasic("Alice", "Temporal1!"))//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isOk())//
            .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    @Transactional
    public void registerPassengerAlreadyExists() throws Exception
    {
        userRepository.save(new User("Alice", "pswd", UserType.ATTENDANT));

        this.mockMvc.perform(post("/register")//
            .content(json.writeValueAsBytes(//
                new RegisterPassengerRequestDto("Alice", "Temporal1!")))
            .contentType(MediaType.APPLICATION_JSON)//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isConflict())//
            .andExpect(jsonPath("$.code").value("PassengerAlreadyExistsException"));
    }
}
