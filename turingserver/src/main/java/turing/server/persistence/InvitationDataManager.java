package turing.server.persistence;

import turing.model.invitation.Invitation;
import turing.util.stream.StreamUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class InvitationDataManager implements DataManager<Invitation> {
    //TODO: make root (e.g. "invitations") as a private constant

    private File getInvitationsFile() throws IOException {
        return getFile("./model/invitation/invitations");
    }

    @Override
    public Optional<Invitation> get(UUID id) {
        return getAll().stream().filter(inv -> inv.id.equals(id)).findFirst();
    }

    @Override
    public List<Invitation> getAll() {
        try {
            return StreamUtils.deserializeEntities(getInvitationsFile(), "invitations", Invitation.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @Override
    public Optional<UUID> create(Invitation entity) {
        if (entity == null || entity.id == null) {
            return Optional.empty();
        }

        List<Invitation> invitations = getAll();
        // If there is already an invitation with the same name user-document association
        if (invitations.stream().anyMatch(inv -> inv.userName.equals(entity.userName)
                && inv.documentName.equals(entity.documentName))) {
            return Optional.empty();
        }

        invitations.add(entity);
        try {
            StreamUtils.serializeEntities(invitations, "invitations", new FileOutputStream(getInvitationsFile()));
            return Optional.of(entity.id);
        }
        catch (IOException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean update(Invitation entity) {
        return false;
    }

    @Override
    public boolean delete(Invitation entity) {
        return false;
    }
}
