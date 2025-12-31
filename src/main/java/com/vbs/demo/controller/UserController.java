package com.vbs.demo.controller;

import com.vbs.demo.dto.DisplayDto;
import com.vbs.demo.dto.LoginDto;
import com.vbs.demo.dto.UpdateDto;
import com.vbs.demo.models.History;
import com.vbs.demo.models.User;
import com.vbs.demo.repositories.HistoryRepo;
import com.vbs.demo.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    UserRepo userRepo;
    @Autowired
    HistoryRepo historyRepo;

    //SignUp-UI
    @PostMapping("/register")
    public String register(@RequestBody User user) {
        History history = new History();
        history.setDescription("User " + user.getUsername() + " self created");

        historyRepo.save(history);
        userRepo.save(user);

        return "Sign-Up Successful";
    }

    //LogIn-UI
    @PostMapping("/login")
    public String login(@RequestBody LoginDto u) {
        User user = userRepo.findByUsername(u.getUsername());

        if(user == null) {
            return "User not found";
        }

        if(!u.getPassword().equals(user.getPassword())) {
            return "Password Mismatch";
        }

        if(!u.getRole().equals(user.getRole())) {
            return "Role Mismatch";
        }

        return String.valueOf(user.getId());
    }

    //Dashboard-UI
    @GetMapping("/get-details/{id}")
    public DisplayDto display(@PathVariable int id) {
        //id is primary key hence we need to resolve any exceptions
        User user = userRepo.findById(id).orElseThrow(()->new RuntimeException("User not found"));

        DisplayDto displayDto = new DisplayDto();
        displayDto.setUsername(user.getUsername());
        displayDto.setBalance(user.getBalance());

        return displayDto;
    }

    //Dashboard-Update-Details-UI
    @PostMapping("/update")
    public String update(@RequestBody UpdateDto obj) {
        User user = userRepo.findById(obj.getId()).orElseThrow(()->new RuntimeException("User not found"));
        History history = new History();

        if(obj.getKey().equalsIgnoreCase("name")){
            if(obj.getValue().equals(user.getName())){
                return "Same Username";
            }

            history.setDescription("User " + user.getUsername() + " Changed Name from " + user.getName() + " to " + obj.getValue());

            user.setName(obj.getValue());
        }
        else if(obj.getKey().equalsIgnoreCase("password")) {
            if(obj.getValue().equals(user.getPassword())){
                return "Same Password";
            }

            history.setDescription("User " + user.getUsername() + " Changed Password");

            user.setPassword(obj.getValue());
        }
        else if(obj.getKey().equalsIgnoreCase("email")) {
            if(obj.getValue().equals(user.getEmail())){
                return "Same Email";
            }

            if(userRepo.findByEmail(obj.getValue()) != null) {
                return "Email already exists";
            }

            history.setDescription("user " + user.getUsername() + " Changed Email from " + user.getEmail() + " to " + obj.getValue());

            user.setEmail(obj.getValue());
        }
        else {
            return "Invalid field";
        }

        historyRepo.save(history);
        userRepo.save(user);

        return "Updated Successfully";
    }

    //Admin-Add-User-UI
    @PostMapping("/add/{adminId}")
    public String add(@RequestBody User user,@PathVariable int adminId) {

        History history = new History();
        history.setDescription("Admin " + adminId + " added user " + user.getUsername() + " Successfully");

        historyRepo.save(history);
        userRepo.save(user);

        return "Added " + user.getName() + " Successfully";
    }

    //Admin-All-Users-Sort-Order-UI
    @GetMapping("/users")
    public List<User> getAllUsers(@RequestParam String sortBy, @RequestParam String order) {
        Sort sort;

        if(order.equalsIgnoreCase("desc")) {
            sort = Sort.by(sortBy).descending();
        }
        else {
            sort = Sort.by(sortBy).ascending();
        }

        return userRepo.findAllByRole("customer",sort);
    }

    //Admin-All-Users-Search-UI
    @GetMapping("/users/{searchTerm}")
    public List<User> searchUsers(@PathVariable String searchTerm) {

        return userRepo.findByUsernameContainingIgnoreCaseAndRole(searchTerm,"customer");
    }

    //Admin-All-Users-Delete-UI
    @DeleteMapping("/delete-user/{userId}/admin/{adminId}")
    public String delete(@PathVariable int userId, @PathVariable int adminId) {
        User user = userRepo.findById(userId).orElseThrow(()-> new RuntimeException("User not found"));
        History history = new History();

        if(userId == adminId){
            return "Admin cannot delete themselves";
        }
        if(user.getBalance() > 0) {
            return "Balance should be zero";
        }

        history.setDescription("Admin " + adminId + " deleted user " + user.getUsername() + " Successfully");

        historyRepo.save(history);
        userRepo.delete(user);

        return "Deleted " + user.getUsername() + " Successfully";
    }

}