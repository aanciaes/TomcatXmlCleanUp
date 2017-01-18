package servlet;

import javaResources.CoCCleanUp;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author miguel
 */
@WebServlet(name = "CleanUp", urlPatterns = {"/cleanUp"})
public class UploadServet extends HttpServlet {

    private final Logger logger = LogManager.getLogger(UploadServet.class);
    
    private static int MAXMEMSIZE;

    //Repository for files over MAXMEMSIZE
    private static String REPOSITORY;

    private boolean isMultipart;

    @Override
    public void init() {
        String maxMemSize = (String) this.getServletContext().getAttribute("maxMemSize");
        MAXMEMSIZE = Integer.parseInt(maxMemSize);
        REPOSITORY = (String) this.getServletContext().getAttribute("Repository");
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        isMultipart = ServletFileUpload.isMultipartContent(request);
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        if (!isMultipart) {
            out.println("<html>");
            out.println("<head>");
            out.println("<title>XML file clean up</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<p>No file uploaded</p>");
            out.println("</body>");
            out.println("</html>");
            return;
        }

        DiskFileItemFactory factory = new DiskFileItemFactory();

        //Maximum size that will be stored into memory
        factory.setSizeThreshold(MAXMEMSIZE);
        //Path to save file if its size is bigger than MAXMEMSIZE
        factory.setRepository(new File(REPOSITORY));
        ServletFileUpload upload = new ServletFileUpload(factory);

        out.println("<html>");
        out.println("<head>");
        out.println("<title>XML file clean up</title>");
        out.println("</head>");
        out.println("<body>");

        try {
            List<FileItem> fileItems = upload.parseRequest(request);
            Iterator<FileItem> t = fileItems.iterator();

            while (t.hasNext()) {
                FileItem f = t.next();

                if (!f.isFormField()) {
                    if (f.getContentType().equals("text/xml")) {    //Check weather or not the uploaded file is an XML file
                        
                        String uniqueFileName = f.getName() + "-" + request.getSession().getId() + ".xml"; //Creates unique name
                        String location = (String) this.getServletContext().getAttribute("newFileLocation");
                        
                        CoCCleanUp clean = new CoCCleanUp(uniqueFileName, location);

                        if (clean.cleanDocument(f.getInputStream()) == 0) {
                            out.println("<h3>" + f.getName() + " was clean</h3>");
                            out.println(clean.printHTMLStatistics());
                            out.println("<br /><form action='download?filename=" + uniqueFileName + "' method='post'><input type='submit' value='Download'/></form></body></html>");
                        } else {
                            out.println("<h3>" + clean.getErrorMessage() + "</h3>");
                            out.println("<br /><form action='index.html' method='post'><input type='submit' value='Go Back'/></form></body></html>");
                        }
                    } else {
                        out.println("<h3>The file " + f.getName() + " is not an xml file</h3>");
                        out.println("<br /><form action='index.html' method='post'><input type='submit' value='Go Back'/></form></body></html>");
                        logger.warn("The file " + f.getName() + " is not an xml file: " + f.getContentType());
                    }
                }
            }

            File repository = factory.getRepository();
            cleanTmpFiles(repository);

        } catch (IOException | FileUploadException e) {
            out.println("<h3>Something went wrong</h3></br>");
            out.println("<br /><form action='index.html' method='post'><input type='submit' value='Go Back'/></form></body></html>");
        }
    }

    private void cleanTmpFiles(File repository) {
        if (repository.isDirectory()) {
            File[] files = repository.listFiles();
            for (File file : files) {
                if (file.getName().startsWith("upload") && file.getName().endsWith(".tmp")) {
                    file.delete();
                }
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
