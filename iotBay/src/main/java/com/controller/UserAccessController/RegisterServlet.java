package com.controller.UserAccessController;

import com.bean.Customer;
import com.bean.Staff;
import com.dao.CustomerDao;
import com.dao.DBManager;
import com.dao.StaffDao;
import com.dao.UserAccessLogDao;
import com.enums.Status;
import com.util.Utils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        DBManager db =  (DBManager) session.getAttribute("db");
        CustomerDao customerDao = db.getCustomerDao();
        StaffDao staffDao = db.getStaffDao();

        String firstName = req.getParameter("firstName");
        String lastName = req.getParameter("lastName");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("ConfirmPassword");
        long phoneNumber = -1;
        try {
            phoneNumber = Long.parseLong(req.getParameter("phoneNumber"));
        } catch (NumberFormatException e) {
            req.setAttribute("errorMessage", "Phone number must be a valid number");
        }
        String state = req.getParameter("state");
        String city = req.getParameter("city");
        int postalCode = -1;
        try {
            postalCode = Integer.parseInt(req.getParameter("postalCode"));
        } catch (NumberFormatException e) {
            req.setAttribute("errorMessage", "Postcode must be a valid number");
        }
        String country = req.getParameter("country");
        String address;
        if (!req.getParameter("unit").isEmpty()) {
            address = req.getParameter("unit") + " " + req.getParameter("street");
        } else {
            address = req.getParameter("street");
        }
        String status = "Active";

        // validate input and return errors
        Map<String, String> errors = Utils.validateUserInput(firstName, lastName, email, password, confirmPassword, phoneNumber,
                state, city, postalCode, country, address, false);

        // check if email is unique
        try {
            if (customerDao.emailExists(email) || staffDao.emailExists(email)) {
                errors.put("email", "An account with this email already exists");
            }
        } catch (SQLException e) {
            System.out.println("Error checking for duplicate email in registration");
            throw new RuntimeException(e);
        }

        // if any errors exist, display and don't register user
        if (!errors.isEmpty() || req.getAttribute("errorMessage") != null) {
            req.setAttribute("errors", errors);
            req.getRequestDispatcher("/views/register.jsp").forward(req, resp);
            return;
        }

        if (req.getParameter("userType").equalsIgnoreCase("customer")) {
            String username = email.split("@")[0];
            Customer customer = new Customer();
            customer.setUsername(username);
            customer.setPassword(password);
            customer.setFirstName(firstName);
            customer.setLastName(lastName);
            customer.setPhone(phoneNumber);
            customer.setEmail(email);
            customer.setStatus(status);
            customer.setAddress(address);
            customer.setCity(city);
            customer.setState(state);
            customer.setPostcode(postalCode);
            customer.setCountry(country);
            customer.setType("Individual");

            try {
                customerDao.addUser(customer);
                // retrieve customer with autogenerated userId from database
                Customer customerWithId = customerDao.getUser(email, password);
                session.setAttribute("loggedInUser", customerWithId);
                session.setAttribute("userType", "customer");
                session.removeAttribute("errorMessage");

                UserAccessLogDao userAccessLogDao = db.getUserAccessLogDao();
                int userAccessLogId = userAccessLogDao.logLogin(customerWithId.getUserId(), "customer");
                session.setAttribute("userAccessLogId", userAccessLogId);
                resp.sendRedirect(req.getContextPath()+"/views/welcome.jsp");
                return;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (req.getParameter("userType").equalsIgnoreCase("staff")) {
            String position = "staff";
            Staff staff = new Staff();
            staff.setStaffName(firstName +  " " + lastName);
            staff.setPassword(password);
            staff.setPhoneNum(Math.toIntExact(phoneNumber));
            staff.setEmail(email);
            staff.setPosition(position);
            staff.setStatus("Active");
            staff.setAddress(address);
            staff.setCity(city);
            staff.setPostcode(String.valueOf(postalCode));
            staff.setState(state);
            staff.setCountry(country);

            try {
                staffDao.addStaff(staff);
                // retrieve staff with autogenerated userId from database
                Staff staffWithId = staffDao.getStaffForLogin(email, password);
                session.setAttribute("loggedInUser", staffWithId);
                session.setAttribute("userType", "staff");
                session.removeAttribute("errorMessage");

                UserAccessLogDao userAccessLogDao = db.getUserAccessLogDao();
                int userAccessLogId = userAccessLogDao.logLogin(staffWithId.getStaffId(), "staff");
                session.setAttribute("userAccessLogId", userAccessLogId);
                resp.sendRedirect(req.getContextPath()+"/views/welcome.jsp");
                return;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }

        req.setAttribute("errorMessage", "Could not register account");
        req.getRequestDispatcher("/views/register.jsp").forward(req, resp);

    }
}
