package com.commafeed.frontend.rest.resources;

import com.commafeed.TestConstants;
import com.commafeed.integration.BaseIT;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestSecurity(user = "admin", roles = "user")
class FeedEntryNoteRESTTest extends BaseIT {

    @BeforeEach
    void setup() {
        RestAssured.authentication = RestAssured.DEFAULT_AUTH;
        initialSetup(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD);
        RestAssured.authentication =
                RestAssured.preemptive()
                        .basic(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD);
        subscribeAndWaitForEntries(getFeedUrl());
    }

    @Test
    void saveNote() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"entryId\":2,\"comment\":\"Test note\",\"rating\":5}")
                .when()
                .post("/rest/entry/note")
                .then()
                .statusCode(200);
    }

    @Test
    void listNotes() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"entryId\":2,\"comment\":\"Test note\",\"rating\":5}")
                .post("/rest/entry/note")
                .then()
                .statusCode(200);

        RestAssured.given()
                .get("/rest/entry/note/list")
                .then()
                .statusCode(200)
                .body("comment", Matchers.hasItem("Test note"))
                .body("rating", Matchers.hasItem(5));
    }
}
