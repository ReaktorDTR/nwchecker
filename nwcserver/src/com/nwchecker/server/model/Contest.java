package com.nwchecker.server.model;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

/**
 * <h1>Contest Entity</h1>
 * Entity that represents some Contest in DB.
 * <p>
 *
 * @author Roman Zayats
 * @version 1.0
 */
@Entity
@Table(name = "contest")
public class Contest {

    public static enum Status {
        ARCHIVE, PREPARING, RELEASE, GOING
    }

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "title")
    @Pattern(regexp = "[0-9a-zA-Zа-яіїєА-ЯІЇЄ ,.'()-]{0,}")
    @NotEmpty
    @Size(max = 100)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    @NotEmpty
    private String description;

    @Column(name = "starts")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date starts;

    @Column(name = "duration")
    @DateTimeFormat(pattern = "HH:mm")
    private Date duration;

    @OneToMany(mappedBy = "contest", orphanRemoval = true,
            cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<Task> tasks;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "contest_users",
            joinColumns = {
                    @JoinColumn(name = "contest_id")},
            inverseJoinColumns = {
                    @JoinColumn(name = "user_id")})
    private List<User> users;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Column(name = "hidden")
    private boolean hidden;

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStarts() {
        return starts;
    }

    public void setStarts(Date starts) {
        this.starts = starts;
    }

    public Date getDuration() {
        return duration;
    }

    public void setDuration(Date duration) {
        this.duration = duration;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    @Override
    public boolean equals(Object c) {
        return (c != null && c instanceof Contest && ((Contest) c).getId() == this.id);
    }

}
