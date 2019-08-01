package turing.server.persistence;

import turing.model.invitation.Invitation;
import turing.model.user.User;
import turing.util.stream.StreamUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class InvitationDataManager extends DataManager<UUID, Invitation> {

    public InvitationDataManager(String path) {
        super(path, "invitations", Invitation.class);
    }

    public List<Invitation> getByUser(User user) {
        return getAll().stream().filter(inv -> inv.userName.equals(user.name)).collect(Collectors.toList());
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
