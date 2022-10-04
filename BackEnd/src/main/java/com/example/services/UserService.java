package com.example.services;


import com.example.dto.RecurringPaymentsDTO;
import com.example.dto.RecurringPaymentsQDO;
import com.example.dto.UserDTO;
import com.example.model.*;
//import com.example.repository.PasswordResetTokenRepository;
import com.example.pdf.UploadFile;
import com.example.repository.PasswordResetTokenRepository;
import com.example.repository.RecurringPaymentsRepository;
import com.example.repository.TransactionsRepository;
import com.example.repository.UserRepository;
import com.example.util.JwtUtil;
import org.hibernate.JDBCException;
import org.hibernate.PropertyValueException;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService{

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtTokenUtil;

    @Autowired
    private RecurringPaymentsRepository recurringPaymentsRepository;

    @Autowired
    private TransactionsRepository transactionsRepository;

    @Autowired
    EntityManager em;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;

    public List<UserDTO> getAllUsers(){
        List<UserDTO> users = new ArrayList<>();
        userRepository.findAll().forEach((x)-> {
            UserDTO userdto = modelMapper.map(x, UserDTO.class);
            users.add(userdto);}
        );
        return users;
    }


    public String addUser(UserDTO userdTo){
        User user = modelMapper.map(userdTo, User.class);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        try{
            userRepository.save(user);
            return "success";
        }
        catch(PropertyValueException e){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Null Value not accepted");
        }
        catch (DataIntegrityViolationException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
        }
        catch(Exception e){
            return e.toString();
        }
    }

    @Transactional
    public void addUsers(List<UserDTO> al){
        al.stream()
                .forEach((userDTO)-> {
                    User user = modelMapper.map(userDTO, User.class);
                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                    userRepository.save(user);
                });
    }

    public User getUser(int userId){
        return userRepository.findById(userId).get();
    }
    @Transactional
    public void updateUser(User user){
        userRepository.save(user);
    }
    @Transactional
    public String addMoney(int id, int amount){
        User user = userRepository.findById(id).get();
        int openingBal = user.getBalance();
        user.setBalance(user.getBalance() + amount);
        int closingBal = user.getBalance();
        Transactions transaction = new Transactions(user, "CR", "Money Added", amount, openingBal, closingBal, null);
        transactionsRepository.save(transaction);
        userRepository.save(user);
        return "Money Added";
    }

    public String setupNewRecurringPayment(int id, RecurringPaymentsDTO recurringPaymentsDTO){
        RecurringPayments recurringPayment = modelMapper.map(recurringPaymentsDTO, RecurringPayments.class);
        User user = userRepository.findById(id).get();
        recurringPayment.setUser(user);
        recurringPaymentsRepository.save(recurringPayment);
        try{
            return "success";
        }
        catch(Exception e){
            return e.toString();
        }
//        Query query = em.createNativeQuery("select * from recurring_payments where user_id=?", RecurringPayments.class);
//        query.setParameter(1, user);
//        List<RecurringPayments> recurringPaymentsList = (List<RecurringPayments>) query.getResultList();
//        return recurringPaymentsList;
    }

    public List<RecurringPaymentsQDO> showRecurringPayments(int id) {
        User user = userRepository.findById(id).get();
        Query query = em.createNativeQuery("select * from recurring_payments where user_id=? and active=1", RecurringPayments.class);
        query.setParameter(1, user);
        List<RecurringPaymentsQDO> recurringPaymentsList = (List<RecurringPaymentsQDO>) query.getResultList();
        return recurringPaymentsList;
    }


    @Transactional
    public String transferMonthlyPayments(int id){
        User user = userRepository.findById(id).get();
        Query query = em.createNativeQuery("select * from recurring_payments where (user_id=? AND start_date<=?) AND (active=1 AND no_of_times>0)", RecurringPayments.class);
        query.setParameter(1, user);
        query.setParameter(2, Calendar.getInstance().getTime());           // new Date()
        List<RecurringPayments> recurringPaymentsList = query.getResultList();
        int flag=0;
        if(recurringPaymentsList.size()==0){
            return "No Pending Payments";
        }
        recurringPaymentsList.stream().forEach(
                (recurringPayment) -> {
                    int openingBal = user.getBalance();     //inital total balance before trans.
                    if (openingBal >= recurringPayment.getAmount()) {
                        recurringPayment.setNoOfTimes(recurringPayment.getNoOfTimes() - 1);
                        if (recurringPayment.getNoOfTimes() == 0) {
                            recurringPayment.setActive(false);
                        }
                        user.setBalance(user.getBalance() - recurringPayment.getAmount());
                        int closingBal = user.getBalance();     //new opening balance or balance after trans
                        Transactions transaction = new Transactions(user, "DB", recurringPayment.getDescription(), recurringPayment.getAmount(), openingBal, closingBal, recurringPayment);
//                        user.addRecurring(recurringPayment);
                        userRepository.save(user);   //balance
                        recurringPaymentsRepository.save(recurringPayment); //no of times , active
                        transactionsRepository.save(transaction);    // opening & Closing balance, Cur Trans Date
                    }
                }
        );
        return "Paid";
    }

    public String removeRecurringPayment(int id, int recc){
        User user = userRepository.findById(id).get();
        Query query = em.createNativeQuery("select * from recurring_payments where user_id=? AND recc_id=?", RecurringPayments.class);
        query.setParameter(1, user);
        query.setParameter(2, recc);
        List<RecurringPayments> recurringPayments = (List<RecurringPayments>) query.getResultList();
        if(recurringPayments.size()==0){
            return "No Recurring Payment Found";
        }
        else{
            RecurringPayments recurringPayment = recurringPayments.get(0);
            recurringPayment.setActive(false);
            recurringPaymentsRepository.save(recurringPayment);
            return "Removed";
        }
    }

    public List<Transactions> getStatement(int id, Date from, Date to){
        User user = userRepository.findById(id).get();
        Query query = em.createNativeQuery("select * from transactions where transaction_date BETWEEN ? AND ? AND user_id=?", Transactions.class);
        query.setParameter(1, from);
        query.setParameter(2, to);
        query.setParameter(3, user);
        List<Transactions> transactionsList = query.getResultList();
        return transactionsList;
    }

    public int getBalance(String email){
        User user = userRepository.findByEmail(email).get();
        return user.getBalance();
    }

    public User findUserByEmail(String email){
        return userRepository.findByEmail(email).get();
    }

    public void createPasswordResetTokenForUser(User user, String token) {
        PasswordResetToken myToken = new PasswordResetToken(token, user);
        passwordResetTokenRepository.save(myToken);
    }

    public String validatePasswordResetToken(String token) {
        final PasswordResetToken passToken = passwordResetTokenRepository.findByToken(token).get();

        return !isTokenFound(passToken) ? "invalidToken"
                : isTokenExpired(passToken) ? "expired"
                : "success";
    }

    private boolean isTokenFound(PasswordResetToken passToken) {
        return passToken != null;
    }

    private boolean isTokenExpired(PasswordResetToken passToken) {
        final Calendar cal = Calendar.getInstance();
        return passToken.getExpiryDate().before(cal.getTime());
    }
    public void save(int userId, MultipartFile file){
        try{

            User user = userRepository.findById(userId).get();
            List<RecurringPaymentsDTO> bills= UploadFile.convertExcelToList(user,file.getInputStream());
            List<RecurringPayments> list_of_bills=new ArrayList<>();
            bills.forEach((dto)->{
                RecurringPayments bill=modelMapper.map(dto,RecurringPayments.class);
                bill.setUser(user);
                list_of_bills.add(bill);
            });
            recurringPaymentsRepository.saveAll(list_of_bills);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public String savePassword(String token, String password){
        try {
            System.out.println(token + "\n" + password);
            PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token).get();
            User user = passwordResetToken.getUser();
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
            return "success";
        }
        catch (Exception e){
            return e.toString();
        }
    }

//    public void changeUserPassword(User user, String password) {
//        user.setPassword(passwordEncoder.encode(password));
//        repository.save(user);
//    }


//    public String isRecurring(int id, String description){
//        int electricityBill = 500;
//        int waterBill = 300;
//        int gasBill = 1000;
//        int amount=0;
//        switch(description){
//            case "electricity" : amount = electricityBill; break;
//            case "water" : amount = waterBill; break;
//            case "gas" : amount = gasBill;
//        }
//        User user = userRepository.getById(id);
//        if(user.getBalance()<amount){
//            return "Balance is insufficient";
//        }
//        else{
//            Query query = em.createNativeQuery("select * from recurring_payments where user_id=? AND description=? order by recc_id DESC LIMIT 1",RecurringPayments.class);
//            query.setParameter(1, user);
//            query.setParameter(2, description);
//            List<RecurringPayments> list = query.getResultList();
//            if(list.isEmpty()){
//                return setupNewRecurringPayment(user, description, amount);
//            }
//            RecurringPayments recurringPayments = list.get(0);
//            if(recurringPayments.isActive()){
//                int noOfTimes = recurringPayments.getNoOfTimes();
//                recurringPayments.setNoOfTimes(++noOfTimes);
//                recurringPaymentsRepository.save(recurringPayments);
//                return transferMonthlyPayments(user, description, amount, recurringPayments);
//            }
//            else{
//                return setupNewRecurringPayment(user, description, amount);
//            }
//        }
//    }


//    public String setupNewRecurringPayment(User user, String description, int amount){
//        RecurringPayments recurringPayments = new RecurringPayments(user, description, amount, 1);
//        recurringPaymentsRepository.save(recurringPayments);
//        return transferMonthlyPayments(user, description, amount, recurringPayments);
//    }

//    @Transactional
//    public String transferMonthlyPayments(User user, String description, int amount, RecurringPayments recc){
//        int openingBal = user.getBalance();
//        user.setBalance(user.getBalance()-amount);
//        int closingBal = user.getBalance();
//        Transactions transaction = new Transactions(user, "DB", description, amount, openingBal, closingBal, recc);
//        transactionsRepository.save(transaction);
//        userRepository.save(user);
//        return "Paid";
//    }
//public String removeRecurringPayment(int id, String description) {
//        User user = userRepository.findById(id).get();
//        Query query = em.createNativeQuery("select * from recurring_payments where user_id=? AND description=? ORDER BY recc_id LIMIT 1", RecurringPayments.class);
//        query.setParameter(1, user);
//        query.setParameter(2, description);
//        List<RecurringPayments> list = query.getResultList();
//        if (list.isEmpty()) {
//            return "No Recurring Payment Found";
//        }
//        RecurringPayments recurringPayments = list.get(0);
//        if (recurringPayments.isActive()) {
//            recurringPayments.setActive(false);
//            recurringPaymentsRepository.save(recurringPayments);
//            return "Recurring Payment Removed";
//        } else {
//            return "No Active Recurring Payment Found";
//        }
//    }

}
