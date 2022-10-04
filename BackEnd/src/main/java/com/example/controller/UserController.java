package com.example.controller;


import com.example.auth.MyUserDetails;
import com.example.auth.MyUserDetailsService;
import com.example.config.AuthenticationRequest;
import com.example.config.AuthenticationResponse;
import com.example.dto.*;
import com.example.model.*;
import com.example.services.UserService;
import com.example.util.JwtUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.ui.Model;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.security.PermitAll;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@RestController
public class UserController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private MyUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtTokenUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private MessageSource messages;

//    @Autowired
//    private PasswordDto passwordDto;

//    @GetMapping("/")
//    public String getHello(@RequestHeader(name="Authorization") String token){
//        return "HEllo World!" + jwtTokenUtil.extractUsername(token.substring(7));
//    }

//    @GetMapping("/users")
//    public List<UserDTO> getAllUsers(){
//        return userService.getAllUsers();
//    }
////
////    @GetMapping("/users/{id}")
////    public User getUser(@PathVariable int id){
////        return userService.getUser(id);
////    }

//Getting the balance of the user in Home Page
    @GetMapping("/balance")
    public int getBalance(Authentication authentication){
        MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();
        return userService.getBalance(myUserDetails.getUsername());
    }
    //Single User registered or created
    @PostMapping(value = "/signup")
    public String addUser(@RequestBody UserDTO userDto){
        return userService.addUser(userDto);
    }


//    @PostMapping(value = "/signups")
//    public void addUsers(@RequestBody List<UserDTO> usersdto){
//        userService.addUsers(usersdto);
//    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws  Exception {
        System.out.println(authenticationRequest.getEmail() + " " + authenticationRequest.getPassword());
        try{authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authenticationRequest.getEmail(), authenticationRequest.getPassword())
        );}
        catch (BadCredentialsException e){
            throw new Exception("Incorrect username or password", e);
        }
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getEmail());
        System.out.println(userDetails);
        final String jwt = jwtTokenUtil.generateToken(userDetails);
        return ResponseEntity.ok(new AuthenticationResponse(jwt, userDetails.getUsername(), userDetails.getAuthorities()));
    }
     //Add Money
    @PutMapping("/addMoney")
    public String addMoney(Authentication authentication, @RequestBody AddMoneyDTO addMoneyDTO){
        int amount = addMoneyDTO.getAmount();
        MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal(); //after authrntication user details which returns, are called as Principles
        userService.addMoney(myUserDetails.getId(), amount);
        return "Money Added";
    }
    //AddBill Payments
    @PostMapping("/recurring_payment")
    public String setupNewRecurringPayment(Authentication authentication, @RequestBody RecurringPaymentsDTO recurringPaymentsDTO){
        MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();
        return userService.setupNewRecurringPayment(myUserDetails.getId(), recurringPaymentsDTO);
    }
     //Hint: First page or Home page
    @GetMapping("/recurring_payment")
    public List<RecurringPaymentsQDO> showRecurringPayments(Authentication authentication){
        MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();
        return userService.showRecurringPayments(myUserDetails.getId());
    }
//Hint: this job is done through Batch Service for monthly
//    @PutMapping("/payments")
//    public String transferMonthlyPayments(Authentication authentication){
//        MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();
//        return userService.transferMonthlyPayments(myUserDetails.getId());
//    }


//View Statement:> All transaction will appear here
    @PostMapping("/statement")
    public List<Transactions> getStatement(Authentication authentication, @RequestBody StatementDTO statementDTO){
        MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();
//        System.out.println(statementDTO.getFrom() + " " + statementDTO.getTo());
        return userService.getStatement(myUserDetails.getId(), statementDTO.getFrom(), statementDTO.getTo());
    }











    //    @PutMapping("/cancel")
//    public String removeRecurringPayment(Authentication authentication,@RequestBody CancelReccurringDTO cancelReccurringDTO){
//        MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();
//        return userService.removeRecurringPayment(myUserDetails.getId(), cancelReccurringDTO.getId());
//    }

//    @PostMapping("/user/reset-password")
//    public String resetPassword(HttpServletRequest request,
//                                @RequestBody EmailDTO emailDTO) throws Exception{
//        System.out.println(emailDTO.getEmail());
//        User user = userService.findUserByEmail(emailDTO.getEmail());
//        if (user == null) {
//            throw new UsernameNotFoundException("User not found");
//        }
//        String token = UUID.randomUUID().toString();
//        userService.createPasswordResetTokenForUser(user, token);
//        String url = "http://localhost:3000" + "/change-password?token=" + token;
//        sendEmail(user.getEmail(), url);
//        return "success ";
//    }

//    @PostMapping("/user/valid-token")
//    public String isValidToken(@RequestBody String token){
//        System.out.println(token.substring(10, 46));
//        return userService.validatePasswordResetToken(token.substring(10, 46));
//    }


//    @PostMapping("/user/save-password")
//    public String savePassword(@RequestBody PasswordDto passwordDto){
//public void sendEmail(String recipientEmail, String link)
////        throws MessagingException, UnsupportedEncodingException {
//    MimeMessage message = mailSender.createMimeMessage();
//    MimeMessageHelper helper = new MimeMessageHelper(message);
//
//    helper.setFrom("foodfoodalways@gmail.com", "Shopme Support");
//    helper.setTo(recipientEmail);
//
//    String subject = "Here's the link to reset your password";
//
//    String content = "<p>Hello,</p>"
//            + "<p>You have requested to reset your password.</p>"
//            + "<p>Click the link below to change your password:</p>"
//            + "<p><a href=\"" + link + "\">Change my password</a></p>"
//            + "<br>"
//            + "<p>Ignore this email if you do remember your password, "
//            + "or you have not made the request.</p>";
//
//    helper.setSubject(subject);
//
//    helper.setText(content, true);
//
//    mailSender.send(message);
//}
////        return userService.savePassword(passwordDto.getToken(), passwordDto.getPassword());
//    }

//    @PostMapping("/user/savePassword")
//    public GenericResponse savePassword(final Locale locale, @Valid PasswordDto passwordDto) {
//
//        String result = userService.validatePasswordResetToken(passwordDto.getToken());
//
//        if(result != "success") {
//            return new GenericResponse(messages.getMessage(
//                    "auth.message." + result, null, locale));
//        }
//
//        Optional user = userService.getUserByPasswordResetToken(passwordDto.getToken());
//        if(user.isPresent()) {
//            userService.changeUserPassword(user.get(), passwordDto.getNewPassword());
//            return new GenericResponse(messages.getMessage(
//                    "message.resetPasswordSuc", null, locale));
//        } else {
//            return new GenericResponse(messages.getMessage(
//                    "auth.message.invalid", null, locale));
//        }
//    }

//    @PutMapping("/pay/{description}")
//    public String setupNewRecurringPayment(Authentication authentication, @PathVariable String description){
//        MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();
//        return userService.isRecurring(myUserDetails.getId(), description);
//    }

//    @PutMapping("/users/{id}/pay")
//    public String setupNewRecurringPayment(@PathVariable int id){
//        return userService.setupNewRecurringPayment(id);
//    }
//

}
