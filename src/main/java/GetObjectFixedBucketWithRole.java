import java.io.IOException;
import java.io.OutputStream;
 
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
 
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
 
@WebServlet(urlPatterns = { "/object-with-role/*" })
public class GetObjectFixedBucketWithRole extends HttpServlet {
    
        private static final long serialVersionUID = 1L;
 
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                resp.setContentType("image/jpeg");
                
                String pathInfo = req.getPathInfo(); 
                String[] pathParts = pathInfo.split("/");
                String key = pathParts[1]; 
                String bucketName = "p-acl";
                                
                S3Client s3Client = S3Client.builder()
                .credentialsProvider(InstanceProfileCredentialsProvider.create())
                .region(Region.US_EAST_1)
                .build();
                        
                
                GetObjectRequest request = GetObjectRequest.builder()
                                                .bucket(bucketName)
                                                .key(key)
                                                .build();
                
                ResponseInputStream<GetObjectResponse> response 
                                        = s3Client.getObject(request);
                OutputStream outputStream = resp.getOutputStream();
                
                byte[] buffer = new byte[4096];
                int bytesRead;
                
                while ((bytesRead = response.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
                
                response.close();
                outputStream.close();
        }
}