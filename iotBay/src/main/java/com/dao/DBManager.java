package com.dao;

import com.bean.Category;

import java.sql.*;

public class DBManager {
    private final Connection connection;

    private CustomerDao customerDao;
    private ProductDao productDao;
    private StaffDao staffDao;
    private CategoryDao categoryDao;

    public DBManager(Connection connection) throws SQLException {
        this.connection = connection;
    }

    public CustomerDao getCustomerDao() {
        if (customerDao == null) {
            customerDao = new CustomerDao(connection);
        }
        return customerDao;
    }

    public ProductDao getProductDao() {
        if (productDao == null) {
            productDao = new ProductDao(connection);
        }
        return productDao;
    }

    public StaffDao getStaffDao() {
        if (staffDao == null) {
            staffDao = new StaffDao(connection);
        }
        return staffDao;
    }

    public CategoryDao getCategoryDao() {
        if (categoryDao == null) {
            categoryDao = new CategoryDao(connection);
        }
        return categoryDao;
    }

    public Connection getConnection() {
        return connection;
    }
}
