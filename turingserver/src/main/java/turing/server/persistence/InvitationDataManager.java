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

public class InvitationDataManager extends DataManager<UUID, Invitation> {

    public InvitationDataManager(String path) {
        super(path, "invitations", Invitation.class);
    }

    @Override
    protected boolean contains(List<Invitation> entities, Invitation invitation) {
        return entities.stream().anyMatch(inv ->
                inv.userName.equals(invitation.userName)
                        && inv.documentName.equals(invitation.documentName));
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
