package turing.client;

public interface ClientUserInterface {
    void register(String username, String password);
    void login(String username, String password);
    void logout();

    void create(String document, int sections);
    void share(String document, String username);
    void show(String document, int section);
    void show(String document);
    void list();

    void edit(String documnet, int section);
    void endEdit(String document, int section);

    void printHelp();
}
