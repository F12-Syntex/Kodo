package com.kodo.codewars;

import java.util.Optional;

import com.kodo.bot.CodewarsApi;
import com.kodo.bot.Settings;
import com.kodo.database.users.UserStorage;
import com.kodo.database.users.scheme.User;
import com.kodo.handler.Dependencies;

/*
 * this is a facade class for dealing with codewars related functions
 */
public class CodeWars {

    private final Dependencies dependencies;
    private final CodewarsApi api;

    public CodeWars(Dependencies dependencies){
        this.dependencies = dependencies;
        this.api = new CodewarsApi(this);
    }

    public void registerUser(String username){
        if(this.getUserStorage().isRegistered(username)) throw new IllegalArgumentException("User is already registered");

        User data = this.api.getProfileData(username);

        if(data.getClan() == null || !data.getClan().equals(Settings.CLAN_NAME)) throw new IllegalArgumentException("User is not in the clan");

        this.getUserStorage().registerUser(username, data);
    }

    public Optional<User> getUserProfile(String username){
        if(!this.getUserStorage().isRegistered(username)) return Optional.empty();
        return Optional.of(this.getUserStorage().getUser(username).getProfile().getUser());
    }

    public UserStorage getUserStorage() {
        return this.dependencies.getStorage().getUserStorage();
    }

    public Dependencies getDependencies() {
        return dependencies;
    }

    public CodewarsApi getApi() {
        return api;
    }
}
