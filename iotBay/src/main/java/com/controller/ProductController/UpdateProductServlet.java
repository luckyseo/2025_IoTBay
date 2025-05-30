package com.controller.ProductController;

import com.bean.Product;
import com.bean.Category;
import com.dao.CategoryDao;
import com.dao.DBManager;
import com.dao.ProductDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
@MultipartConfig
@WebServlet("/UpdateProductServlet")
public class UpdateProductServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        DBManager db = (DBManager) session.getAttribute("db");
        ProductDao productDao = db.getProductDao();

        Integer productId = Integer.parseInt(req.getParameter("productId"));
        try {
            Product product = productDao.getProductById(productId);
            req.setAttribute("product", product);
            req.getRequestDispatcher( "/views/AdminProductUpdate.jsp").forward(req, resp);
        } catch (SQLException e) {
            System.out.println("Cannot get the product");
        }
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            HttpSession session = req.getSession();
            DBManager db = (DBManager) session.getAttribute("db");
            ProductDao pd = db.getProductDao();
            CategoryDao cd = db.getCategoryDao();
            Product product = new Product();

            int productId = Integer.parseInt(req.getParameter("productId"));
            String productName = req.getParameter("productName");
            double price = Double.parseDouble(req.getParameter("price"));
            int quantity = Integer.parseInt(req.getParameter("quantity"));
            String description = req.getParameter("description");
            //String image = req.getParameter("image");
            String appPath=req.getServletContext().getRealPath("/");
            String uploadPath = appPath+ File.separator+"assets"+File.separator+"img";

            Part getFormImg = req.getPart("image");
            String imgName = Paths.get(getFormImg.getSubmittedFileName()).getFileName().toString();

            int categoryId = Integer.parseInt(req.getParameter("categoryId"));
            try{
                boolean categoryLenValidation = false;
                int maxCategoryLen = cd.lenCategory();
                if(categoryId>maxCategoryLen){
                    categoryLenValidation = true;
                    session.setAttribute("categoryLenValidation", categoryLenValidation);
                    resp.sendRedirect(req.getContextPath() + "/ProductManagementServlet");
                    return;
                }else{
                    categoryLenValidation =false;
                    session.setAttribute("categoryLenValidation", categoryLenValidation);
                    try{
                        Category category = cd.getCategoryById(categoryId);
                        product.setCategory(category);
                    }catch(SQLException e){
                        System.out.println("Category-dao error detected");
                        e.printStackTrace();
                    }
                }
            } catch (RuntimeException | SQLException Exception) {
                System.out.println("Error - categoryLenValidation from UpdateProductServlet");
                throw new RuntimeException(Exception);
            }
            product.setProductId(productId);
            product.setProductName(productName);
            product.setPrice(price);
            product.setQuantity(quantity);
            product.setDescription(description);
            //product.setImage(image);
            if(imgName != null && !imgName.isEmpty()){
                getFormImg.write(uploadPath+File.separator+imgName);
                product.setImage(imgName);
            }else{
                Product prevInfo = pd.getProductById(productId); //when staff update without new img -> keep current img
                product.setImage(prevInfo.getImage());
            }
            //product db stores categoryId but product class stores string(category name)
            //So get CategoryName using categoryId from category db.
            Category category = cd.getCategoryById(categoryId);
            product.setCategory(category);

            pd.updateProduct(product);

            resp.sendRedirect(req.getContextPath() + "/ProductManagementServlet");
        } catch (SQLException e) {
            System.out.println("Failed to update a Product");
            e.printStackTrace();
        }
    }
}
