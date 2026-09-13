package com.commafeed.frontend.rest.resources;

import com.commafeed.backend.service.FeedEntryAlternativeService;
import com.commafeed.backend.service.LlmServiceException;
import com.commafeed.frontend.model.AlternativeTextResponse;
import com.commafeed.frontend.model.request.GenerateAlternativeRequest;
import com.commafeed.security.AuthenticationContext;
import com.commafeed.security.Roles;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/entry/{id}/generate-alternative")
@RolesAllowed(Roles.USER)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
@RequiredArgsConstructor
public class FeedEntryAlternativeREST {

    private final AuthenticationContext authenticationContext;
    private final FeedEntryAlternativeService feedEntryAlternativeService;

    @POST
    public Response generate(@PathParam("id") Long entryId, GenerateAlternativeRequest request) {
        if (request == null
                || request.getTarget() == null
                || request.getTarget().isBlank()
                || request.getPrompt() == null
                || request.getPrompt().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("target and prompt are required"))
                    .build();
        }

        try {
            AlternativeTextResponse response =
                    feedEntryAlternativeService
                            .generate(
                                    authenticationContext.getCurrentUser(),
                                    entryId,
                                    request.getTarget(),
                                    request.getPrompt())
                            .orElse(null);
            if (response == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(response).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        } catch (LlmServiceException e) {
            log.warn("LLM alternative generation failed: {}", e.getMessage());
            Response.Status status =
                    e.kind() == LlmServiceException.Kind.UNAVAILABLE
                            ? Response.Status.SERVICE_UNAVAILABLE
                            : Response.Status.BAD_GATEWAY;
            return Response.status(status)
                    .entity(new ErrorResponse("Alternative generation is currently unavailable"))
                    .build();
        }
    }

    public record ErrorResponse(String message) {}
}
