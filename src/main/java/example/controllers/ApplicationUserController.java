package example.controllers;

import example.models.ApplicationUser;
import example.models.Post;
import example.repos.PostRepository;
import example.repos.ApplicationUserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Controller
public class ApplicationUserController {
    @Autowired
    ApplicationUserRepository applicationUserRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private HttpServletRequest request;

    @GetMapping("/")
    public RedirectView getHomePage(Model m, Principal p) {
        if (p != null) {
            String username = p.getName();
//            ApplicationUser user = applicationUserRepository.findByUsername(username);
            m.addAttribute("username", username);
            return new RedirectView("/feed");
        }
        return new RedirectView("/index");
    }

    @GetMapping("/login")
    public String getLoginPage(Principal p) {
        if (p != null) {
            return "redirect:/";
        }
        return "login.html";
    }

    @GetMapping("/signup")
    public String getSignUpPage() {
        return "signup.html";
    }

    @PostMapping("/signup")
    public RedirectView createUser(String username, String password, String firstName, String lastName, LocalDate dateOfBirth, String bio) {
        ApplicationUser newUser = new ApplicationUser(username, passwordEncoder.encode(password), firstName, lastName, dateOfBirth, bio);
        applicationUserRepository.save(newUser);
        authWithHttpServletRequest(username, password);
        return new RedirectView("/");
    }

    public void authWithHttpServletRequest(String username, String password) {
        try {
            request.login(username, password);
        } catch (ServletException e) {
            System.out.println("Error while logging in!");
            e.printStackTrace();
        }
    }

    @GetMapping("/test")
    public String getTestPage(Model m, Principal p) {
        if(p != null) {
            String username = p.getName();
            ApplicationUser user = applicationUserRepository.findByUsername(username);

            if(user != null) {
                m.addAttribute("username", user.getUsername());
            }
        }

        return "test.html";
    }

    @GetMapping("/myprofile")
    public String getMyProfile(Model m, Principal p) {
        if(p != null) {
            ApplicationUser user = applicationUserRepository.findByUsername(p.getName());
            m.addAttribute("user", user);
            m.addAttribute("username", user.getUsername());
            return "myprofile";
        }
        return "login";
    }

    @PutMapping("/myprofile")
    public RedirectView editProfile(Principal p, String username, String firstName, String lastName, LocalDate dateOfBirth, String bio, Long id, RedirectAttributes redir) {
        ApplicationUser user = applicationUserRepository.findById(id).orElseThrow();
        if(p != null) {
            user.setUsername(username);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setDateOfBirth(dateOfBirth);
            user.setBio(bio);
            applicationUserRepository.save(user);


            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, user.getPassword(),
                    user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            redir.addFlashAttribute("errorMessage", "You are not permitted to edit this profile!");
        }

        return new RedirectView("/myprofile");
    }


    @GetMapping("/users")
    public String getAllUsers(Model m, Principal p, RedirectAttributes redir) {
        if(p != null) {
            ApplicationUser currentUser = applicationUserRepository.findByUsername(p.getName());
            List<ApplicationUser> users = applicationUserRepository.findAllByOrderByUsernameAsc();
            m.addAttribute("currentUser", currentUser);
            m.addAttribute("users", users);
            return "users";
        } else {
            redir.addFlashAttribute("errorMessage", "You must be logged in to view users!");
            return "redirect:/login";
        }
    }

    @PostMapping("/createPost")
    public RedirectView createPost(Principal p, String body,long id, RedirectAttributes redir) {
        ApplicationUser user = applicationUserRepository.findById(id).orElseThrow();
        if(p != null) { //not strictly needed if WebSecurityConfig is set up properly
            Date date = new Date();
            Post post = new Post(body, date);
            user.addPost(post);
            postRepository.save(post);
            applicationUserRepository.save(user);
        } else {
            redir.addFlashAttribute("errorMessage", "You are not permitted to add posts to this profile!");
        }
        return new RedirectView("/myprofile");
    }


    @GetMapping("/user/{id}")
    public String getUserInfoPage(Model m, Principal p, @PathVariable long id, RedirectAttributes redir) {
        if(p != null) {
            ApplicationUser user = applicationUserRepository.findById(id).orElseThrow();
            m.addAttribute("user", user);
            return "profile";
        } else {
            redir.addFlashAttribute("errorMessage", "You must be logged in to view this profile!");
            return "redirect:/login";

         }
        }

    @PutMapping("/follow/{id}")
    public RedirectView followUser(Principal p, @PathVariable Long id, RedirectAttributes redir){
        ApplicationUser currentUser = applicationUserRepository.findByUsername(p.getName());
        if (!(currentUser.getId() == id)) {
            ApplicationUser userToFollow = applicationUserRepository.findById(id).orElseThrow(() -> new RuntimeException("Error reading " +
                   "user from the database with ID of: " + id));
            currentUser.follow(userToFollow);
            applicationUserRepository.save(currentUser);
        } else {
           redir.addFlashAttribute("errorMessage", "You cannot follow yourself.");
       }
      return new RedirectView("/users");
   }
    }
