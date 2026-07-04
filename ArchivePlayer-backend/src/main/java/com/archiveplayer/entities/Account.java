package com.archiveplayer.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Account")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_name", unique = true, nullable = false)
    private String accountName;

    @Column(name = "password", nullable = false)
    private String password;

    //TODO: Remove this once redis cache is implemented
    @Column(name = "session_token")
    private String activeSessionToken;

    @Column(name = "private_account", nullable = false)
    private boolean privateAccount = false;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "account_follows",
            joinColumns = @JoinColumn(name = "follower_id"),
            inverseJoinColumns = @JoinColumn(name = "following_id")
    )
    private Set<Account> following = new HashSet<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "following", fetch = FetchType.LAZY)
    private Set<Account> followers = new HashSet<>();

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "account_blocks",
            joinColumns = @JoinColumn(name = "blocker_id"),
            inverseJoinColumns = @JoinColumn(name = "blocked_id")
    )
    private Set<Account> blockedUsers = new HashSet<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "blockedUsers", fetch = FetchType.LAZY)
    private Set<Account> blockers = new HashSet<>();

    public Account() {}

    public Account(String accountName, String password) {
        this.accountName = accountName;
        this.password = password;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public String getPassword() { return this.password; }
    public void setPassword(String password) { this.password = password; }

    public String getActiveSessionToken() { return activeSessionToken; }
    public void setActiveSessionToken(String activeSessionToken) { this.activeSessionToken = activeSessionToken; }

    public boolean isPrivateAccount() { return privateAccount; }
    public void setPrivateAccount(boolean privateAccount) { this.privateAccount = privateAccount; }

    public Set<Account> getFollowers() { return followers; }
    public void setFollowers(Set<Account> followers) { this.followers = followers; }

    public Set<Account> getFollowing() { return following; }
    public void setFollowing(Set<Account> following) { this.following = following; }

    public Set<Account> getBlockedUsers() { return blockedUsers; }
    public void setBlockedUsers(Set<Account> blockedUsers) { this.blockedUsers = blockedUsers; }

    public Set<Account> getBlockers() { return blockers; }
    public void setBlockers(Set<Account> blockers) { this.blockers = blockers; }

    public void addFollowing(Account accountToFollow) { this.following.add(accountToFollow); }
    public void removeFollowing(Account accountToUnfollow) { this.following.remove(accountToUnfollow); }
    public void addFollower(Account follower) { this.followers.add(follower); }
    public void removeFollower(Account follower) { this.followers.remove(follower); }
    public void addBlock(Account accountToBlock) { this.blockedUsers.add(accountToBlock); }
    public void removeBlock(Account accountToUnblock) { this.blockedUsers.remove(accountToUnblock); }
}