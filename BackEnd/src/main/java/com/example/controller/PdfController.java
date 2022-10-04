package com.example.controller;


import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;


import com.example.auth.MyUserDetails;
import com.example.model.RecurringPayments;
import com.example.model.Transactions;
import com.example.pdf.StatementPDFExporter;
import com.example.pdf.UploadFile;
import com.example.services.UserService;
import com.lowagie.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

@RestController
public class PdfController {

    @Autowired
    private UserService userService;

    @Autowired
    private UploadFile uploadFile;


    @GetMapping("/viewStatements/pdf/{startDate}/{endDate}")                                                                     //pdf from client side
    public String exportToPDF(Authentication authentication, @PathVariable String startDate, @PathVariable String endDate, HttpServletResponse response) throws DocumentException, IOException {
        response.setContentType("application/pdf");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=users_" + currentDateTime + ".pdf";
        response.setHeader(headerKey, headerValue);
        //authenticated user details is saved in security context
        //with the help this security context we get the authenticated user  principal which is  details of authenticated user.
        MyUserDetails customUserDetail=(MyUserDetails) authentication.getPrincipal();  //get deatils of authhenticate user
        List<Transactions> listUsers = showYourStatement(customUserDetail,startDate,endDate);

        StatementPDFExporter exporter = new StatementPDFExporter(listUsers);//converting list to pdf
        exporter.export(response);
        return "success";

    }
    public List<Transactions> showYourStatement(MyUserDetails myUserDetail, String startDate, String endDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

        try {
            Date start = formatter.parse(startDate);
            Date end = formatter.parse(endDate);
            return userService.getStatement(myUserDetail.getId(),start,end);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

//    public String uploadData(Authentication authentication,@RequestParam("file") MultipartFile file) throws IOException{
//        return uploadFile.uploadFile(authentication,file);
//    }

    @PostMapping("/addBills/upload")
    public String upload(Authentication authentication,@RequestParam("file") MultipartFile file){
        MyUserDetails myUserDetail=(MyUserDetails) authentication.getPrincipal();
        if(UploadFile.checkExcelFormat(file)){
            userService.save(myUserDetail.getId(),file);

            return "Bills are added to DB";
        }
        return "Please Upload Excel FIle";
    }
}
