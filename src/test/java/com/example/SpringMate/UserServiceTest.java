package com.example.SpringMate;

import com.example.SpringMate.Repositoy.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    UserRepository userRepository;


    @BeforeEach
//    @AfterAll
    void setUp(){}

//    @Disabled
    @ParameterizedTest
    @ValueSource(strings = {
            "abc@gmail.com"
    })
    void testUser(String email) {
        Assertions.assertNotNull(userRepository.findByEmail(email));
    }

    @ParameterizedTest
    @CsvSource({
            "1,2,4",
            "1,3,4"
    })
    void parameterizedTest(int a, int b, int expected){
        Assertions.assertEquals(expected,a+b,"Failed::"+expected);
    }
}
