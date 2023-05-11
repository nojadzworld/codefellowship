package example.models;

import jakarta.persistence.*;
import java.util.Date;

@Entity
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    @Column(columnDefinition = "text")
    String body;
    Date createdAt;

    @ManyToOne
    @JoinColumn(name = "applicationUser_id")
    ApplicationUser author;

    public Post() {
        //empty
    }

    public Post(String postContent, Date createdAt) {
        this.body = postContent;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String postContent) {
        this.body = postContent;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public ApplicationUser getApplicationUser() {
        return author;
    }

    public void setApplicationUser(ApplicationUser applicationUser) {
        this.author = applicationUser;
    }

}
