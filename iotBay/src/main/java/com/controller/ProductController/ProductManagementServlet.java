package com.controller.ProductController;

import com.bean.Product;
import com.dao.CategoryDao;
import com.dao.DBManager;
import com.dao.ProductDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.util.List;
import java.sql.Connection;

@WebServlet("/ProductManagementServlet")
public class ProductManagementServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try{
          //  Connection connection = DriverManager.getConnection("jdbc:sqlite:/Users/yunseo/.SmartTomcat/IoTBay/IoTBay/IoTBayDB.db");
            //ProductDao productDao = new ProductDao(connection);

            HttpSession session = req.getSession();
            DBManager db = (DBManager) session.getAttribute("db");
            ProductDao productDao = db.getProductDao();
            CategoryDao categoryDao = db.getCategoryDao();
            List<Product> allProducts = productDao.getAllProducts();
            req.setAttribute("allProducts", allProducts); // set to request
            req.getRequestDispatcher("/views/ProductManagement.jsp").forward(req, resp); // forward to JSP
            req.setAttribute("CategoryLen", categoryDao.lenCategory());
            System.out.println("Category len: " + categoryDao.lenCategory());
            System.out.println("Fetched products: " + allProducts.size()); //To check whether this code runs

        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}