package com.commafeed.frontend.rest.resources;

import com.commafeed.TestConstants;
import com.commafeed.backend.service.GroqLlmService;
import com.commafeed.integration.BaseIT;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
@TestSecurity(user = "admin", roles = "user")
class FeedEntryAlternativeRESTTest extends BaseIT {

    @InjectMock GroqLlmService llmService;

    @BeforeEach
    void setup() {
        RestAssured.authentication = RestAssured.DEFAULT_AUTH;
        initialSetup(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD);
        RestAssured.authentication =
                RestAssured.preemptive()
                        .basic(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD);
        subscribeAndWaitForEntries(getFeedUrl());
        Mockito.when(llmService.generate(Mockito.anyString(), Mockito.anyString()))
                .thenReturn("Mocked Title");
    }

    @Test
    void generateAlternative() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"target\":\"title\",\"prompt\":\"Shorten\"}")
                .when()
                .post("/rest/entry/2/generate-alternative")
                .then()
                .statusCode(200)
                .body("alternative", Matchers.equalTo("Mocked Title"));
    }

    @Test
    void generateAlternativeForMissingEntry() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"target\":\"title\",\"prompt\":\"Shorten\"}")
                .when()
                .post("/rest/entry/99999/generate-alternative")
                .then()
                .statusCode(404);
    }
}
