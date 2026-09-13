package com.commafeed.frontend.rest.resources;

import com.commafeed.backend.model.FeedEntryNote;
import com.commafeed.backend.model.User;
import com.commafeed.backend.service.FeedEntryNoteService;
import com.commafeed.security.AuthenticationContext;
import com.commafeed.security.Roles;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Path("/rest/entry/note")
@RolesAllowed(Roles.USER)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
@RequiredArgsConstructor
public class FeedEntryNoteREST {

    private final AuthenticationContext authenticationContext;
    private final FeedEntryNoteService feedEntryNoteService;

    @POST
    @Path("/save")
    public Response save(SaveNoteRequest request) {
        User user = authenticationContext.getCurrentUser();
        feedEntryNoteService.saveOrUpdateNote(
                user, request.getEntryId(), request.getComment(), request.getRating());
        return Response.ok().build();
    }

    @GET
    @Path("/get/{entryId}")
    public Response get(@PathParam("entryId") Long entryId) {
        User user = authenticationContext.getCurrentUser();
        FeedEntryNote note = feedEntryNoteService.getNote(user, entryId).orElse(null);
        return Response.ok(note).build();
    }

    @DELETE
    @Path("/delete/{entryId}")
    public Response delete(@PathParam("entryId") Long entryId) {
        User user = authenticationContext.getCurrentUser();
        feedEntryNoteService.deleteNote(user, entryId);
        return Response.ok().build();
    }

    @Data
    public static class SaveNoteRequest {

        private Long entryId;
        private String comment;
        private Integer rating;
    }
}
