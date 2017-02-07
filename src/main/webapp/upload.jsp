<%-- 
    Document   : upload
    Created on : Jan 31, 2017, 10:23:44 PM
    Author     : miguel
--%>

<%@page import="org.apache.commons.fileupload.FileUploadException"%>
<%@page import="java.io.IOException"%>
<%@page import="emsa.webcoc.cleanup.core.CoCCleanUp"%>
<%@page import="java.util.Iterator"%>
<%@page import="org.apache.commons.fileupload.FileItem"%>
<%@page import="java.util.List"%>
<%@page import="java.io.File"%>
<%@page import="org.apache.commons.fileupload.disk.DiskFileItemFactory"%>
<%@page import="org.apache.commons.fileupload.servlet.ServletFileUpload"%>
<%@page import="org.apache.logging.log4j.Logger"%>
<%@page import="org.apache.logging.log4j.LogManager"%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>

        <link rel="stylesheet" href="stylesheets/main.css">
        <script type="text/javascript">
            
            function downloading(){
                var downloadBtn= document.getElementById("downloadForm");
                downloadBtn.hidden=true;
                
                var downloadSucc = document.getElementById("successForm");
                downloadSucc.hidden=false;
            }
            
        </script>
        
    </head>
    <body>
        <section class="row-alt">
            <div class="lead container">
                <%
                    Logger logger = LogManager.getLogger(getClass());

                    int MAXMEMSIZE;

                    //Repository for files over MAXMEMSIZE
                    String REPOSITORY;

                    String maxMemSize = (String) this.getServletContext().getAttribute("maxMemSize");
                    MAXMEMSIZE = Integer.parseInt(maxMemSize);
                    REPOSITORY = (String) this.getServletContext().getAttribute("Repository");

                    boolean isMultipart = ServletFileUpload.isMultipartContent(request);
                    response.setContentType("text/html");

                    if (!isMultipart) {
                %>
                <h1>An error occurred, the file was not clean</h1>
                <h2>Please try again</h2>
                <%
                    }

                    DiskFileItemFactory factory = new DiskFileItemFactory();

                    //Maximum size that will be stored into memory
                    factory.setSizeThreshold(MAXMEMSIZE);
                    //Path to save file if its size is bigger than MAXMEMSIZE
                    factory.setRepository(new File(REPOSITORY));
                    ServletFileUpload upload = new ServletFileUpload(factory);

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

                                    if (clean.cleanDocument(f.getInputStream()) == 0) {%>
                                        <h1><%=f.getName()%> was cleaned</h1>
                                        <h3><%=clean.printHTMLStatistics()%></h3><hr/>
                                        <form id="downloadForm" action="download?filename=<%=uniqueFileName%>" method="post"><input class="btn btn-alt" type="submit" value="Download" onclick="downloading()"/></form>
                                        <form hidden id="successForm" action="index.html" method="post">
                                            <h3>Your download has started...</h3>
                                            <input class="btn btn-alt" type="submit" value="Clean Another file"/>
                                        </form>
                                    <%} else {%>
                                        <h1><font color="red">Error</font></h1>
                                        <h3><font color="red"><%=clean.getErrorMessage()%></font></h3><hr/>
                                        <form action='index.html' method='post'>
                                            <input class="btn btn-alt" type='submit' value='Clean another file'/>
                                        </form>
                                    <%}
                                }else{%>
                                        <h1><font color="red">Error</font></h1>
                                        <h3><font color="red">The file <%=f.getName()%> is not an xml file</font></h3><hr/>
                                        <form action='index.html' method='post'><input class="btn btn-alt" type='submit' value='Clean another file'/></form>
                                    <%}
                            }            
                        }
                        File repository = factory.getRepository();
                        cleanTmpFiles(repository);

                    } catch (IOException | FileUploadException ex) {%>
                        <h3>Something went wrong</h3>
                        <form action='index.html' method='post'><input class="btn btn-alt" type='submit' value='Clean another file'/></form>
                    <%
                        logger.fatal(ex.getMessage());
                    }%>
                        
                    <%!
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
                %>
            </div>
        </section>
            
        <footer class="primary-footer container group">
            <small>EMSA &copy; 2017 </small>
        </footer>
            
    </body>
</html>
